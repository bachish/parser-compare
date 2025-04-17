# jobs/errors_calculation.py

from dagster import job
from assets.dataset import dataset
from ops.errors.calculate_errors import calculate_errors
from ops.errors.preprocess_errors import preprocess_errors

@job
def errors_calculation_job():
    dataset_path = dataset()
    errors_csv_path = calculate_errors(dataset_path)
    preprocess_errors(errors_csv_path)