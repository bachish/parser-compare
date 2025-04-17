import os
import sqlite3
from dagster import op, OpExecutionContext, In, Out
from config.settings import DB_PATH
from pygments import lex
from pygments.lexers import JavaLexer

@op(ins={"dataset_path": In(str)}, out=Out(str))
def populate_files(context: OpExecutionContext, dataset_path: str) -> str:
    # Подключаемся к базе данных
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    added_count = 0
    skipped_count = 0

    # Инициализируем лексер для Java
    lexer = JavaLexer()

    # Обходим все файлы в датасете
    for root, _, files in os.walk(dataset_path):
        for file in files:
            file_path = os.path.join(root, file)
            file_name = os.path.relpath(file_path, dataset_path).replace("\\", "/")

            # Проверяем, существует ли файл в таблице files
            cursor.execute(
                """
                SELECT id FROM files WHERE file_name = ?
                """,
                (file_name,)
            )
            result = cursor.fetchone()

            if result:
                # context.log.info(f"File '{file_name}' already exists with id {result[0]}, skipping")
                skipped_count += 1
                continue

            # Читаем содержимое файла
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()
            except UnicodeDecodeError:
                context.log.warning(f"Failed to read '{file_name}' with UTF-8, skipping")
                continue

            # Считаем количество символов
            number_of_characters = len(content)

            # Считаем количество токенов
            tokens = list(lex(content, lexer))
            number_of_tokens = len([t for t in tokens if t[1].strip()])  # Считаем только непустые токены

            # Вставляем данные в таблицу files
            cursor.execute(
                """
                INSERT INTO files (file_name, number_of_characters, number_of_tokens)
                VALUES (?, ?, ?)
                """,
                (file_name, number_of_characters, number_of_tokens)
            )

            # context.log.info(f"Added file '{file_name}' with {number_of_characters} chars, {number_of_tokens} tokens")
            added_count += 1

    # Сохраняем изменения
    conn.commit()
    conn.close()

    context.log.info(f"Populated files: {added_count} added, {skipped_count} skipped")
    return DB_PATH