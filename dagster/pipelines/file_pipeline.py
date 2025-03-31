import os
import shutil
from dagster import asset, OpExecutionContext, Config

# Базовая директория для хранения датасетов
DATASETS_DIR = "C:/Users/huawei/IdeaProjects/antlr_test_2/dagster/datasets"

# Класс для конфигурации актива
class DatasetConfig(Config):
    source_folder: str  # Путь к папке с исходными файлами
    dataset_name: str   # Имя датасета

def load_dataset(context: OpExecutionContext, config: DatasetConfig) -> str:
    """
    Загружает файлы из source_folder в datasets/<dataset_name>.
    Возвращает путь к папке датасета.
    """
    source_folder = config.source_folder
    dataset_name = config.dataset_name

    # Формируем путь для нового датасета
    dataset_path = os.path.join(DATASETS_DIR, dataset_name)

    # Удаляем старую папку, если она существует (опционально)
    if os.path.exists(dataset_path):
        shutil.rmtree(dataset_path)

    # Создаем новую папку
    os.makedirs(dataset_path, exist_ok=True)

    # Копируем все файлы из исходной папки
    file_count = 0
    for root, _, files in os.walk(source_folder):
        for file in files:
            src_path = os.path.join(root, file)
            dst_path = os.path.join(dataset_path, file)
            shutil.copy2(src_path, dst_path)  # copy2 сохраняет метаданные
            file_count += 1

    context.log.info(f"Copied {file_count} files from {source_folder} to {dataset_path}")
    return dataset_path

@asset
def dataset(context: OpExecutionContext, config: DatasetConfig) -> str:
    """
    Актив, представляющий датасет.
    Конфигурация передается через config.
    """
    return load_dataset(context, config)

# Пример запуска для тестирования
if __name__ == "__main__":
    from dagster import materialize

    materialize(
        [dataset],
        run_config={
            "ops": {
                "dataset": {
                    "config": {
                        "source_folder": "C:\\data\\junit",
                        "dataset_name": "junit"
                    }
                }
            }
        }
    )