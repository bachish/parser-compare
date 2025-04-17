# jobs/init_database.py

from dagster import job
from ops.database.create_database import create_database
from ops.database.populate_parsers import populate_parsers

@job
def init_database_job():
    db_path = create_database()
    populate_parsers(db_path)