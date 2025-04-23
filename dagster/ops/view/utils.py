# ops/view/utils.py

import sqlite3
import pandas as pd

def get_parser_data(db_path: str, selected_parsers: list[str]) -> dict[int, dict[str, str]]:
    """
    Получает данные парсеров из базы по их названиям (title).
    Возвращает словарь {id: {title, color}}.
    Выбрасывает ValueError, если какие-то парсеры не найдены.
    """
    conn = sqlite3.connect(db_path)
    query = f"""
    SELECT id, title, color FROM parser WHERE title IN ({','.join('?' * len(selected_parsers))})
    """
    parser_data = pd.read_sql_query(query, conn, params=selected_parsers)
    conn.close()

    if len(parser_data) != len(selected_parsers):
        missing_parsers = set(selected_parsers) - set(parser_data['title'])
        raise ValueError(f"Parsers with titles {missing_parsers} not found in parser table")

    return {
        row['id']: {'title': row['title'], 'color': row['color']}
        for _, row in parser_data.iterrows()
    }