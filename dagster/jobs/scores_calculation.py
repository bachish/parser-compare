# jobs/scores_calculation.py
from dagster import job
from assets.dataset import dataset
from ops.calculate_scores import calculate_scores
from ops.populate_files import populate_files
from ops.populate_scores import populate_scores

@job
def scores_calculation_job():
    dataset_path = dataset()
    db_path = populate_files(dataset_path)
    scores_csv_path = calculate_scores(dataset_path)
    populate_scores(db_path, scores_csv_path)