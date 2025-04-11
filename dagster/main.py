# main.py
from jobs.dataset_parsing import dataset_parsing_job

if __name__ == "__main__":
    result = dataset_parsing_job.execute_in_process(
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
                        "warmup_files_count": 100
                    }
                }
            }
        }
    )