# main.py
from jobs.measure_time import measure_time_job

if __name__ == "__main__":
    result = measure_time_job.execute_in_process(
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
                        "analyzer_type": "AntlrJavaAnalyzer",
                        "warmup_files_count": 100
                    }
                }
            }
        }
    )