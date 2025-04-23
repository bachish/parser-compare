# jobs/view/view_score_comparison.py

from dagster import job
from ops.view.plot_score_comparison import plot_score_comparison


@job(name="view_score_comparison")
def view_score_comparison_job():
    plot_score_comparison()
