# ops/errors/preprocess_errors.py

import csv
import os
from dagster import op, OpExecutionContext, In, Out

@op(ins={"errors_csv_path": In(str)}, out=Out(str))
def preprocess_errors(context: OpExecutionContext, errors_csv_path: str) -> str:
    context.log.info(f"Preprocessing errors CSV at {errors_csv_path}")

    # Читаем CSV
    rows = []
    with open(errors_csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        fieldnames = reader.fieldnames
        for row in reader:
            new_row = row.copy()
            # Обрабатываем поле code
            code = row.get('code', '')
            if code and code != 'No Error':
                new_row['code'] = code.replace('compiler.err.', '')
            rows.append(new_row)

    # Переписываем CSV
    temp_csv_path = errors_csv_path + '.tmp'
    with open(temp_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    # Заменяем оригинальный файл
    os.replace(temp_csv_path, errors_csv_path)

    context.log.info(f"Preprocessed {len(rows)} rows in {errors_csv_path}")
    return errors_csv_path