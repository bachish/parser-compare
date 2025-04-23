# ops/view/plot_score_comparison.py

import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import seaborn as sns
import sqlite3
from dagster import op, OpExecutionContext, Config, Out
from config.settings import DB_PATH
from ops.view.utils import get_parser_data

# Настраиваем Matplotlib backend для отображения окна
matplotlib.use('TkAgg')

class PlotConfig(Config):
    selected_parsers: list[str]
    fontsize: int = 19

@op(out=Out(None))
def plot_score_comparison(context: OpExecutionContext, config: PlotConfig):
    selected_parsers = config.selected_parsers
    fontsize = config.fontsize

    # Получаем данные парсеров
    parser_dict = get_parser_data(DB_PATH, selected_parsers)

    # Подключение к базе данных
    conn = sqlite3.connect(DB_PATH)

    # SQL-запрос для данных о скорах
    query = f"""
    SELECT 
        p.id AS parser_id,
        ers.score AS similarityScore
    FROM 
        error_recovery_score ers
    JOIN 
        parser p ON ers.id_parser = p.id
    JOIN 
        files f ON ers.id_file = f.id
    WHERE 
        p.title IN ({','.join('?' * len(selected_parsers))})
    """
    data = pd.read_sql_query(query, conn, params=selected_parsers)
    conn.close()

    # Проверяем наличие данных
    if data.empty:
        raise ValueError("No data found for selected parsers")

    # Вывод статистики в логи Dagster
    context.log.info("\nParser statistics:")
    context.log.info("-" * 50)
    for parser_id, parser_info in parser_dict.items():
        parser_name = parser_info['title']
        scores = data[data['parser_id'] == parser_id]['similarityScore']
        mean_score = scores.mean()
        median_score = scores.median()
        context.log.info(f"Parser: {parser_name}")
        context.log.info(f"Mean score: {mean_score:.4f}")
        context.log.info(f"Median score: {median_score:.4f}")
        context.log.info("-" * 50)

    # Настройка стиля графика
    plt.rcParams.update({'font.size': fontsize})
    plt.figure(figsize=(6, 6))

    # Рисуем KDE для каждого парсера
    for parser_id, parser_info in parser_dict.items():
        parser_name = parser_info['title']
        color = parser_info['color']
        scores = data[data['parser_id'] == parser_id]['similarityScore']
        sns.kdeplot(
            scores,
            color=color,
            label=parser_name,
            linewidth=1.5,
            fill=False,
            alpha=1,
            bw_adjust=0.1,
        )

    plt.title('Similarity Score', fontsize=fontsize)
    plt.xlabel('Similarity Score', fontsize=fontsize)
    plt.ylabel('Density Estimation', fontsize=fontsize)
    plt.legend(fontsize=fontsize, loc='upper left')
    plt.grid(True, alpha=0.3)
    plt.xlim(0, 1)
    plt.ylim(0, None)
    plt.tight_layout()

    # Показываем график
    context.log.info("Displaying score comparison plot")
    plt.show()