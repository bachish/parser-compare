# ops/calculate_errors.py

import os
import subprocess
from dagster import op, OpExecutionContext, Out
from config.settings import JAR_PATH, JAVA_EXECUTABLE, OUTPUT_ERRORS_DIR

@op(out=Out(str))
def calculate_errors(context: OpExecutionContext, dataset_path: str) -> str:
    dataset_name = os.path.basename(dataset_path)
    output_csv_filename = f"errors_{dataset_name}.csv"
    output_csv_path = os.path.join(OUTPUT_ERRORS_DIR, output_csv_filename)

    os.makedirs(OUTPUT_ERRORS_DIR, exist_ok=True)

    cmd = [
        JAVA_EXECUTABLE,
        "-jar", JAR_PATH,
        "errors",
        dataset_path,
        output_csv_path,
        "--progress"
    ]

    cmd_str = " ".join(cmd)
    context.log.info(f"Running command: {cmd_str}")
    result = subprocess.run(cmd_str, capture_output=True, text=True, shell=True)

    if result.returncode != 0:
        context.log.error(f"Error: {result.stderr}")
        raise RuntimeError(f"Failed to calculate errors: {result.stderr}")

    context.log.info(f"Output: {result.stdout}")
    context.log.info(f"Errors CSV saved to {output_csv_path}")
    return output_csv_path