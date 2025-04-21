# ops/errors/populate_error_codes_and_messages.py

import csv
import sqlite3
from dagster import op, OpExecutionContext, In, Out
from config.settings import DB_PATH


@op(ins={"errors_csv_path": In(str)}, out=Out(str))
def populate_error_codes_and_messages(context: OpExecutionContext, errors_csv_path: str) -> str:
    context.log.info(f"Populating error codes and messages from {errors_csv_path}")

    # Подключаемся к базе данных
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Собираем уникальные пары (error_code, error_message)
    error_pairs = set()
    with open(errors_csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            error_code = row.get('code', '')
            error_message = row.get('message', '')
            if error_code and error_code != 'No Error' and error_code != 'bad.file.name':
                error_pairs.add((error_code, error_message))

    total_pairs = len(error_pairs)
    context.log.info(f"Found {total_pairs} unique error code-message pairs")

    added_codes = 0
    added_messages = 0
    processed_pairs = 0

    # Обрабатываем пары
    for error_code, error_message in error_pairs:
        # Проверяем, существует ли error_code в javac_error_codes
        cursor.execute("SELECT id FROM javac_error_codes WHERE error_code = ?", (error_code,))
        result = cursor.fetchone()

        if result:
            error_code_id = result[0]
        else:
            # Добавляем новый error_code, если не существует
            cursor.execute("INSERT OR IGNORE INTO javac_error_codes (error_code) VALUES (?)", (error_code,))
            # Получаем id (в любом случае — существует или только что добавлен)
            cursor.execute("SELECT id FROM javac_error_codes WHERE error_code = ?", (error_code,))
            error_code_id = cursor.fetchone()[0]
            added_codes += 1

        # Проверяем, существует ли сообщение для error_code
        cursor.execute(
            """
            SELECT id FROM javac_error_messages
            WHERE error_code_id = ? AND error_message = ?
            """,
            (error_code_id, error_message)
        )
        result = cursor.fetchone()

        if not result:
            # Добавляем новое сообщение, если не существует
            cursor.execute(
                """
                INSERT OR IGNORE INTO javac_error_messages (error_code_id, error_message)
                VALUES (?, ?)
                """,
                (error_code_id, error_message)
            )
            added_messages += 1

        processed_pairs += 1

        # # Логируем прогресс каждые 100 пар или в конце
        # if processed_pairs % 100 == 0 or processed_pairs == total_pairs:
        #     progress_percent = (processed_pairs / total_pairs) * 100
        #     context.log.info(f"Processed {processed_pairs}/{total_pairs} pairs ({progress_percent:.1f}%)")

    # Сохраняем изменения
    conn.commit()
    conn.close()

    context.log.info(f"Populated: {added_codes} error codes, {added_messages} error messages")
    return DB_PATH