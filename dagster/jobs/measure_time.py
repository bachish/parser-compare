# jobs/measure_time.py
from dagster import job
from assets.dataset import dataset
from ops.measure.measure_parsing_time import measure_parsing_time

@job
def measure_time_job():
    dataset_path = dataset()
    measure_parsing_time(dataset_path)