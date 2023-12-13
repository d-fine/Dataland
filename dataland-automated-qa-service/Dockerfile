FROM python:3.11.7

WORKDIR /usr/src/app

COPY ./dataland-automated-qa-service/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./dataland-automated-qa-service/src/ ./
COPY ./dataland-internal-storage/internalStorageOpenApi.json ./dataland-internal-storage/internalStorageOpenApi.json
COPY ./dataland-backend/backendOpenApi.json ./dataland-backend/backendOpenApi.json
RUN openapi-python-client generate --path ./dataland-internal-storage/internalStorageOpenApi.json
RUN openapi-python-client generate --path ./dataland-backend/backendOpenApi.json

ENTRYPOINT python ./entrypoint.py
