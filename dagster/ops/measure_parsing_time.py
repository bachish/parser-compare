# ops/measure_parsing_time.py

import os
import subprocess
from dagster import op, OpExecutionContext, Config, Out
from config.settings import JAR_PATH, JAVA_EXECUTABLE, OUTPUT_CSV_DIR

class ParsingConfig(Config):
    analyzer_type: str
    warmup_files_count: int

@op(out=Out(str))
def measure_parsing_time(context: OpExecutionContext, dataset_path: str, config: ParsingConfig) -> str:
    dataset_name = os.path.basename(dataset_path)  # Имя датасета (например, RxJava)
    output_csv_filename = f"{config.analyzer_type}_measureParsingTime_{dataset_name}.csv"
    output_csv_path = os.path.join(OUTPUT_CSV_DIR, output_csv_filename)

    # Создаём директорию, если её нет
    os.makedirs(OUTPUT_CSV_DIR, exist_ok=True)

    cmd = [
        JAVA_EXECUTABLE,
        "-jar", JAR_PATH,
        "measure",
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