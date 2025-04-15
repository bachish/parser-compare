# jobs/init_database.py

from dagster import job
from ops.create_database import create_database

@job
def init_database_job():
    create_database()