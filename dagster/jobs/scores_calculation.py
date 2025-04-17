# jobs/scores_calculation.py
from dagster import job
from assets.dataset import dataset
from ops.scores.calculate_scores import calculate_scores
from ops.scores.populate_files import populate_files
from ops.scores.populate_scores import populate_scores

@job
def scores_calculation_job():
    dataset_path = dataset()
    db_path = populate_files(dataset_path)
    scores_data = calculate_scores(dataset_path)
    populate_scores(db_path, scores_data)