# ops/create_database.py

import os
import sqlite3
from dagster import op, OpExecutionContext, Out
from config.settings import DB_DIR, DB_PATH

@op(out=Out(str))
def create_database(context: OpExecutionContext) -> str:
    # Создаём директорию для базы данных, если её нет
    os.makedirs(DB_DIR, exist_ok=True)

    # Подключаемся к базе данных (создаст файл, если его нет)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Создаём таблицу parser
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS parser (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            color TEXT
        )
    ''')

    # Создаём таблицу files
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS files (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            file_name TEXT NOT NULL,
            number_of_characters INTEGER,
            number_of_tokens INTEGER
        )
    ''')

    # Создаём таблицу javac_error_codes
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS javac_error_codes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            error_code TEXT NOT NULL
        )
    ''')

    # Создаём таблицу javac_error_messages
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS javac_error_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            error_code_id INTEGER NOT NULL,
            error_message TEXT NOT NULL,
            FOREIGN KEY (error_code_id) REFERENCES javac_error_codes(id)
        )
    ''')

    # Создаём таблицу javac_file_errors
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS javac_file_errors (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_file INTEGER NOT NULL,
            id_error_message INTEGER NOT NULL,
            position INTEGER,
            end_position INTEGER,
            column_number INTEGER,
            line_number INTEGER,
            error_index INTEGER,
            FOREIGN KEY (id_file) REFERENCES files(id),
            FOREIGN KEY (id_error_message) REFERENCES javac_error_messages(id)
        )
    ''')

    # Создаём таблицу error_recovery_score
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS error_recovery_score (
            id_parser INTEGER NOT NULL,
            id_file INTEGER NOT NULL,
            score REAL NOT NULL,
            distance INTEGER,
            number_of_parsed_tokens INTEGER,
            parsing_time_nanos INTEGER,
            number_of_syntax_errors INTEGER,
            PRIMARY KEY (id_parser, id_file),
            FOREIGN KEY (id_parser) REFERENCES parser(id),
            FOREIGN KEY (id_file) REFERENCES files(id)
        )
    ''')

    # Сохраняем изменения
    conn.commit()
    conn.close()

    context.log.info(f"Database initialized at {DB_PATH}")
    return DB_PATH