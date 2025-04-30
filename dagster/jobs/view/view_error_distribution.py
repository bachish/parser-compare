# jobs/view/view_error_distribution.py

from dagster import job
from ops.view.plot_error_distribution import plot_error_distribution


@job(name="view_error_distribution")
def view_error_distribution_job():
    plot_error_distribution()
