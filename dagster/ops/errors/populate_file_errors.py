# ops/errors/populate_file_errors.py

import csv
import sqlite3
from dagster import op, OpExecutionContext, In, Out
from config.settings import DB_PATH

@op(ins={"files_db_path": In(str), "errors_csv_path": In(str), "codes_db_path": In(str)}, out=Out(str))
def populate_file_errors(context: OpExecutionContext, files_db_path: str, errors_csv_path: str, codes_db_path: str) -> str:
    context.log.info(f"Populating javac_file_errors from {errors_csv_path}")

    # Подключаемся к базе данных
    conn = sqlite3.connect(files_db_path)
    cursor = conn.cursor()

    added_count = 0
    skipped_count = 0
    current_file_name = None
    error_index = 0

    # Читаем CSV
    with open(errors_csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        rows = list(reader)
        total_rows = len(rows)
        context.log.info(f"Processing {total_rows} rows from {errors_csv_path}")

        for i, row in enumerate(rows):
            file_name = row.get('fileName', '')
            error_code = row.get('code', '')
            error_message = row.get('message', '')
            position = row.get('position', '')
            end_position = row.get('end_position', '')
            column_number = row.get('column_number', '')
            line_number = row.get('line_number', '')

            # Пропускаем строки с No Error или bad.file.name
            if error_code in ('No Error', 'bad.file.name'):
                skipped_count += 1
                continue

            # Обновляем error_index при смене file_name
            if file_name != current_file_name:
                current_file_name = file_name
                error_index = 1
            else:
                error_index += 1

            # Находим id_file
            cursor.execute("SELECT id FROM files WHERE file_name = ?", (file_name,))
            file_result = cursor.fetchone()
            if not file_result:
                context.log.debug(f"File '{file_name}' not found in files table, skipping")
                skipped_count += 1
                continue
            id_file = file_result[0]

            # Находим id_error_message
            cursor.execute(
                """
                SELECT em.id
                FROM javac_error_messages em
                JOIN javac_error_codes ec ON ec.id = em.error_code_id
                WHERE ec.error_code = ? AND em.error_message = ?
                """,
                (error_code, error_message)
            )
            message_result = cursor.fetchone()
            if not message_result:
                context.log.debug(f"Error message '{error_message}' for code '{error_code}' not found, skipping")
                skipped_count += 1
                continue
            id_error_message = message_result[0]

            # Проверяем, существует ли запись
            cursor.execute(
                """
                SELECT 1 FROM javac_file_errors
                WHERE id_file = ? AND id_error_message = ? AND position = ? AND error_index = ?
                """,
                (id_file, id_error_message, position, error_index)
            )
            if cursor.fetchone():
                context.log.debug(f"Error for file {id_file}, message {id_error_message}, position {position}, index {error_index} already exists, skipping")
                skipped_count += 1
                continue

            # Вставляем запись
            cursor.execute(
                """
                INSERT INTO javac_file_errors (id_file, id_error_message, position, end_position, column_number, line_number, error_index)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (id_file, id_error_message, position, end_position, column_number, line_number, error_index)
            )
            added_count += 1

            # if (i + 1) % 100 == 0 or i + 1 == total_rows:
            #     progress_percent = ((i + 1) / total_rows) * 100
            #     context.log.info(f"Processed {i + 1}/{total_rows} rows ({progress_percent:.1f}%)")

    # Сохраняем изменения
    conn.commit()
    conn.close()

    context.log.info(f"Populated javac_file_errors: {added_count} added, {skipped_count} skipped")
    return files_db_path