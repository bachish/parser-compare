# assets/dataset.py
import os
import shutil
from dagster import asset, OpExecutionContext, Config
from config.settings import DATASETS_DIR

class DatasetConfig(Config):
    source_folder: str
    dataset_name: str

@asset
def dataset(context: OpExecutionContext, config: DatasetConfig) -> str:
    dataset_path = os.path.join(DATASETS_DIR, config.dataset_name)
    if os.path.exists(dataset_path):
        shutil.rmtree(dataset_path)
    os.makedirs(dataset_path, exist_ok=True)
    file_count = 0
    for root, _, files in os.walk(config.source_folder):
        for file in files:
            src_path = os.path.join(root, file)
            dst_path = os.path.join(dataset_path, file)
            shutil.copy2(src_path, dst_path)
            file_count += 1
    context.log.info(f"Copied {file_count} files to {dataset_path}")
    return dataset_path