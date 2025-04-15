# jobs/scores_calculation.py
from dagster import job
from assets.dataset import dataset
from ops.calculate_scores import calculate_scores
from ops.populate_files import populate_files

@job
def scores_calculation_job():
    dataset_path = dataset()
    calculate_scores(dataset_path)
    db_path = populate_files(dataset_path)
