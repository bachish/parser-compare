# ops/scores/calculate_scores.py
import os
import subprocess
from dagster import op, OpExecutionContext, Config, Out
from config.settings import JAR_PATH, JAVA_EXECUTABLE, OUTPUT_SCORES_DIR

class ScoresConfig(Config):
    analyzer_type: str

@op(out=Out(tuple))
def calculate_scores(context: OpExecutionContext, dataset_path: str, config: ScoresConfig) -> tuple:
    dataset_name = os.path.basename(dataset_path)
    output_csv_filename = f"{config.analyzer_type}_scores_{dataset_name}.csv"
    output_csv_path = os.path.join(OUTPUT_SCORES_DIR, output_csv_filename)

    os.makedirs(OUTPUT_SCORES_DIR, exist_ok=True)

    cmd = [
        JAVA_EXECUTABLE,
        "-jar", JAR_PATH,
        "scores",
        dataset_path,
        output_csv_path,
        config.analyzer_type
    ]

    cmd_str = " ".join(cmd)
    context.log.info(f"Running command: {cmd_str}")
    result = subprocess.run(cmd_str, capture_output=True, text=True, shell=True)

    if result.returncode != 0:
        context.log.error(f"Error: {result.stderr}")
        raise RuntimeError(f"Failed to calculate scores: {result.stderr}")

    context.log.info(f"Output: {result.stdout}")
    return (output_csv_path, config.analyzer_type)