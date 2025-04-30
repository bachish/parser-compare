# ops/view/plot_error_distribution.py

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
    fontsize: int = 22
    score: float = 1.0
    N: int = 12
    groupby: str = "errorMessage"
    error_black_list: list[str] = ["not error"]

@op(out=Out(None))
def plot_error_distribution(context: OpExecutionContext, config: PlotConfig):
    selected_parsers = config.selected_parsers
    fontsize = config.fontsize
    score = config.score
    N = config.N
    groupby = config.groupby
    error_black_list = config.error_black_list

    # Валидация groupby
    if groupby not in ["errorCode", "errorMessage"]:
        raise ValueError("groupby must be 'errorCode' or 'errorMessage'")

    # Получаем данные парсеров
    parser_dict = get_parser_data(DB_PATH, selected_parsers)

    # Подключение к базе данных
    conn = sqlite3.connect(DB_PATH)

    # Запрос для файлов с проблемами
    query_files_with_issues = f"""
    SELECT 
        f.file_name AS fileName,
        ers.score AS similarityScore,
        p.id AS parser_id
    FROM 
        error_recovery_score ers
    JOIN 
        parser p ON ers.id_parser = p.id
    JOIN 
        files f ON ers.id_file = f.id
    WHERE 
        ers.score = ? AND p.title IN ({','.join('?' * len(selected_parsers))})
    """

    # Запрос для ошибок
    query_parsed_errors = f"""
    SELECT 
        f.file_name AS fileName,
        jec.error_code AS errorCode,
        jem.error_message AS errorMessage
    FROM 
        javac_file_errors jfe
    JOIN 
        javac_error_messages jem ON jfe.id_error_message = jem.id
    JOIN 
        javac_error_codes jec ON jem.error_code_id = jec.id
    JOIN 
        files f ON jfe.id_file = f.id
    WHERE 
        jfe.error_index = 1
        AND jem.error_message NOT IN ({','.join('?' * len(error_black_list))})
    """

    # Чтение данных
    files_with_issues = pd.read_sql_query(query_files_with_issues, conn, params=[score] + selected_parsers)
    parsed_errors = pd.read_sql_query(query_parsed_errors, conn, params=error_black_list)
    conn.close()

    # Проверяем наличие данных
    if files_with_issues.empty or parsed_errors.empty:
        raise ValueError("No data found for selected parsers or errors")

    # Собираем данные по парсерам
    parser_errors = {}
    for parser_id, parser_info in parser_dict.items():
        parser_name = parser_info['title']
        parser_problems = files_with_issues[files_with_issues['parser_id'] == parser_id]
        file_names = parser_problems['fileName'].values
        errors = parsed_errors[parsed_errors['fileName'].isin(file_names)]
        error_counts = errors.groupby(groupby).size().reset_index(name='count')
        error_counts = error_counts.sort_values(by='count', ascending=False)
        error_counts['parser'] = parser_name
        parser_errors[parser_id] = error_counts

    # Объединяем данные всех парсеров
    all_errors = pd.concat(parser_errors.values())

    # Вычисляем топ-N ошибок
    error_totals = all_errors.groupby(groupby)['count'].max().sort_values(ascending=False)
    top_n_errors = error_totals.head(N).index

    # Определяем группы
    if len(error_totals) > N + 1:
        all_errors['group'] = all_errors[groupby].apply(
            lambda x: x if x in top_n_errors else 'Other')
        top_n_order = list(top_n_errors) + ['Other']
    else:
        all_errors['group'] = all_errors[groupby]
        top_n_order = list(top_n_errors)

    # Пересчитываем данные
    grouped_errors = all_errors.groupby(['group', 'parser'])['count'].sum().reset_index()
    grouped_errors['group'] = pd.Categorical(
        grouped_errors['group'], categories=top_n_order, ordered=True)
    grouped_errors = grouped_errors.sort_values('group')

    # Создаём цветовую палитру
    parser_colors = {parser_info['title']: parser_info['color'] for parser_info in parser_dict.values()}

    # Строим диаграмму
    plt.rcParams.update({'font.size': fontsize})
    plt.figure(figsize=(16, 8))
    plt.grid(True)
    sns.barplot(x='count', y='group', hue='parser', data=grouped_errors, dodge=True, palette=parser_colors)

    # Настраиваем график
    title = f"Missed error type distribution (score {score})"
    plt.title(title, fontsize=fontsize)
    plt.xlabel('File count', fontsize=fontsize)
    plt.ylabel('Error type', fontsize=fontsize)
    plt.legend(title='Parser')
    plt.tight_layout()

    # Показываем график
    context.log.info("Displaying error distribution plot")
    plt.show()
