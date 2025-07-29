import os
import shutil
import subprocess
from dagster import asset, op, OpExecutionContext, Config, Out, job

DATASETS_DIR = "C:/Users/huawei/IdeaProjects/antlr_test_2/dagster/datasets"

class DatasetConfig(Config):
    source_folder: str
    dataset_name: str

class ParsingConfig(Config):
    analyzer_type: str
    warmup_files_count: int
    output_csv_dir: str

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

@op(out=Out(str))
def measure_parsing_time(context: OpExecutionContext, dataset_path: str, config: ParsingConfig) -> str:
    jar_path = "C:/Users/huawei/IdeaProjects/antlr_test_2/build/libs/antlr_test_2-1.0-SNAPSHOT.jar"
    dataset_name = os.path.basename(dataset_path)  # Берем только имя датасета (RxJava)
    output_csv_path = os.path.join(config.output_csv_dir, f"{config.analyzer_type}_measureParsingTime_{dataset_name}.csv")

    # Создаем директорию, если её нет
    os.makedirs(config.output_csv_dir, exist_ok=True)

    cmd = [
        '"C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.3\\jbr\\bin\\java.exe"',
        "-jar", jar_path,
        dataset_path,
        output_csv_path,
        config.analyzer_type,
        str(config.warmup_files_count)
    ]

    cmd_str = " ".join(cmd)
    context.log.info(f"Running command: {cmd_str}")
    result = subprocess.run(cmd_str, capture_output=True, text=True, shell=True)

    if result.returncode != 0:
        context.log.error(f"Error: {result.stderr}")
        raise RuntimeError(f"Failed to run parsing measurement: {result.stderr}")

    context.log.info(f"Output: {result.stdout}")
    return output_csv_path

@job
def dataset_parsing_job():
    dataset_path = dataset()
    measure_parsing_time(dataset_path)

if __name__ == "__main__":
    from dagster import materialize
    dataset_parsing_job.execute_in_process(
        run_config={
            "ops": {
                "dataset": {
                    "config": {
                        "source_folder": "C:/data/RxJava",
                        "dataset_name": "RxJava"
                    }
                },
                "measure_parsing_time": {
                    "config": {
                        "analyzer_type": "AntlrJava8Analyzer",
                        "warmup_files_count": 100,
                        "output_csv_dir": "C:/Users/huawei/IdeaProjects/antlr_test_2/dagster/output"
                    }
                }
            }
        }
    )