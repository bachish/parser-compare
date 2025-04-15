# config/settings.py
import os

# Получаем DAGSTER_HOME из переменной окружения
DAGSTER_HOME = os.getenv("DAGSTER_HOME", "C:/Users/huawei/IdeaProjects/antlr_test_2/dagster/dagster_home")
BASE_DIR = "C:/Users/huawei/IdeaProjects/antlr_test_2"
DATASETS_DIR = os.path.join(DAGSTER_HOME, "datasets")
OUTPUT_CSV_DIR = os.path.join(DAGSTER_HOME, "output")  # Теперь CSV будут сохраняться в dagster_home/output

# Путь к JAR-файлу
JAR_PATH = f"{BASE_DIR}/build/libs/antlr_test_2-1.0-SNAPSHOT.jar"

# Путь к Java
JAVA_EXECUTABLE = '"C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.3\\jbr\\bin\\java.exe"'