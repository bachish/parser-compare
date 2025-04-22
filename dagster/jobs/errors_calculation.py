# jobs/errors_calculation.py

from dagster import job
from assets.dataset import dataset
from ops.errors.calculate_errors import calculate_errors
from ops.errors.populate_file_errors import populate_file_errors
from ops.errors.preprocess_errors import preprocess_errors
from ops.errors.populate_error_codes_and_messages import populate_error_codes_and_messages
from ops.scores.populate_files import populate_files

@job
def errors_calculation_job():
    dataset_path = dataset()
    files_db_path = populate_files(dataset_path)
    errors_csv_path = calculate_errors(dataset_path)
    errors_csv_path = preprocess_errors(errors_csv_path)
    codes_db_path = populate_error_codes_and_messages(errors_csv_path)
    populate_file_errors(files_db_path, errors_csv_path, codes_db_path)
