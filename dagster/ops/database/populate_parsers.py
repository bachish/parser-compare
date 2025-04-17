import json
import sqlite3
from dagster import op, OpExecutionContext, In, Out
from config.settings import DB_PATH
from pathlib import Path

@op(ins={"db_path": In(str)}, out=Out(str))
def populate_parsers(context: OpExecutionContext, db_path: str) -> str:
    # Путь к файлу parsers.json
    parsers_file = Path(__file__).parent.parent / "config" / "parsers.json"

    # Читаем данные парсеров
    with open(parsers_file, "r", encoding="utf-8") as f:
        parsers = json.load(f)

    # Подключаемся к базе данных
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    added_count = 0
    skipped_count = 0

    # Проверяем и вставляем каждый парсер
    for parser in parsers:
        title = parser["title"]
        color = parser["color"]

        # Проверяем, существует ли парсер с таким title
        cursor.execute(
            """
            SELECT id FROM parser WHERE title = ?
            """,
            (title,)
        )
        result = cursor.fetchone()

        if result:
            # Парсер уже существует, пропускаем
            context.log.info(f"Parser '{title}' already exists with id {result[0]}, skipping")
            skipped_count += 1
        else:
            # Вставляем новый парсер
            cursor.execute(
                """
                INSERT INTO parser (title, color)
                VALUES (?, ?)
                """,
                (title, color)
            )
            context.log.info(f"Added parser '{title}'")
            added_count += 1

    # Сохраняем изменения
    conn.commit()
    conn.close()

    context.log.info(f"Populated parsers: {added_count} added, {skipped_count} skipped")
    return db_path