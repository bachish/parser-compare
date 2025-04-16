# jobs/errors_calculation.py

from dagster import job
from assets.dataset import dataset
from ops.calculate_errors import calculate_errors

@job
def errors_calculation_job():
    dataset_path = dataset()
    calculate_errors(dataset_path)