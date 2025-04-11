# jobs/dataset_parsing.py
from dagster import job
from assets.dataset import dataset
from ops.measure_parsing_time import measure_parsing_time

@job
def dataset_parsing_job():
    dataset_path = dataset()
    measure_parsing_time(dataset_path)