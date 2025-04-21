# ops/scores/populate_scores.py

import csv
import json
import sqlite3
from dagster import op, OpExecutionContext, In, Out
from config.settings import PARSERS_JSON_FILE

@op(ins={"db_path": In(str), "scores_data": In(tuple)}, out=Out(str))
def populate_scores(context: OpExecutionContext, db_path: str, scores_data: tuple) -> str:
    # Распаковываем кортеж
    scores_csv_path, analyzer_type = scores_data

    # Читаем parsers.json для маппинга
    with open(PARSERS_JSON_FILE, "r", encoding="utf-8") as f:
        parsers = json.load(f)

    # Находим title для analyzer_type
    parser_title = None
    for parser in parsers:
        if parser["analyzer_type"] == analyzer_type:
            parser_title = parser["title"]
            break
    if not parser_title:
        raise ValueError(f"No parser found for analyzer_type '{analyzer_type}' in parsers.json")

    # Подключаемся к базе данных
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Находим id_parser по title
    cursor.execute("SELECT id FROM parser WHERE title = ?", (parser_title,))
    parser_id_result = cursor.fetchone()
    if not parser_id_result:
        conn.close()
        raise ValueError(f"Parser '{parser_title}' not found in parser table")
    parser_id = parser_id_result[0]

    added_count = 0
    skipped_count = 0

    # Читаем CSV
    with open(scores_csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        rows = list(reader)
        total_rows = len(rows)
        context.log.info(f"Starting to process {total_rows} scores from {scores_csv_path}")

        for i, row in enumerate(rows):
            file_name = row['fileName']
            try:
                similarity_score = float(row['similarityScore'])
            except (ValueError, KeyError):
                # context.log.warning(f"Invalid similarityScore for file '{file_name}', skipping")
                continue

            # Находим id_file
            cursor.execute("SELECT id FROM files WHERE file_name = ?", (file_name,))
            file_id_result = cursor.fetchone()
            if not file_id_result:
                # context.log.warning(f"File '{file_name}' not found in files table, skipping")
                continue
            file_id = file_id_result[0]

            # Проверяем, существует ли запись
            cursor.execute(
                """
                SELECT 1 FROM error_recovery_score WHERE id_parser = ? AND id_file = ?
                """,
                (parser_id, file_id)
            )
            if cursor.fetchone():
                # context.log.debug(f"Score for parser {parser_id}, file {file_id} already exists, skipping")
                skipped_count += 1
                continue

            # Вставляем скор
            cursor.execute(
                """
                INSERT INTO error_recovery_score (id_parser, id_file, score)
                VALUES (?, ?, ?)
                """,
                (parser_id, file_id, similarity_score)
            )

            added_count += 1
            # Логируем прогресс каждые 100 строк или в конце
            if (i + 1) % 100 == 0 or i + 1 == total_rows:
                progress_percent = ((i + 1) / total_rows) * 100
                context.log.info(f"Processed {i + 1}/{total_rows} scores ({progress_percent:.1f}%)")

    # Сохраняем изменения
    conn.commit()
    conn.close()

    context.log.info(f"Populated scores: {added_count} added, {skipped_count} skipped")
    return db_path