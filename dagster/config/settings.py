# config/settings.py
import os

DAGSTER_HOME = os.getenv("DAGSTER_HOME", "C:/Users/huawei/IdeaProjects/antlr_test_2/dagster/dagster_home")
BASE_DIR = "C:/Users/huawei/IdeaProjects/antlr_test_2"
DATASETS_DIR = os.path.join(DAGSTER_HOME, "datasets")
OUTPUT_CSV_DIR = os.path.join(DAGSTER_HOME, "output")
OUTPUT_TIMES_DIR = os.path.join(OUTPUT_CSV_DIR, "times")  # Для замеров времени
OUTPUT_SCORES_DIR = os.path.join(OUTPUT_CSV_DIR, "scores")  # Для скоров
OUTPUT_ERRORS_DIR = os.path.join(OUTPUT_CSV_DIR, "errors")

DB_DIR = os.path.join(DAGSTER_HOME, "db")
DB_PATH = os.path.join(DB_DIR, "parser_results.db")

# Путь к JAR-файлу
JAR_PATH = f"{BASE_DIR}/build/libs/antlr_test_2-1.0-SNAPSHOT.jar"

# Путь к Java
JAVA_EXECUTABLE = '"C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.3\\jbr\\bin\\java.exe"'

# Путь к мапленному json файлу с парсерами
PARSERS_JSON_FILE = os.path.join(BASE_DIR, "dagster", "config", "parsers.json")