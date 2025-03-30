import os
import pandas as pd
from dagster import job, op

# Шаг 1: Считываем информацию о файлах в папке


@op
def get_file_info() -> list:
    folder_path = "input_folder"  # Укажи путь к своей папке
    file_info = []

    # Проходим по всем файлам в папке
    for filename in os.listdir(folder_path):
        file_path = os.path.join(folder_path, filename)
        if os.path.isfile(file_path):  # Проверяем, что это файл, а не папка
            size = os.path.getsize(file_path)  # Размер в байтах
            file_info.append({"filename": filename, "size": size})

    return file_info


# Шаг 2: Сохраняем информацию в CSV


@op
def save_to_csv(file_info: list) -> None:
    output_path = "file_info.csv"
    df = pd.DataFrame(file_info)  # Преобразуем список в DataFrame
    df.to_csv(output_path, index=False)  # Сохраняем в CSV
    print(f"CSV file saved at: {output_path}")

# Определяем пайплайн


@job
def file_info_pipeline():
    file_info = get_file_info()
    save_to_csv(file_info)


# Для запуска через Dagit нужно указать, как запускать пайплайн
if __name__ == "__main__":
    result = file_info_pipeline.execute_in_process()
