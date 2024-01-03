FROM python:3.11.7

WORKDIR /usr/src/app

COPY ./dataland-automated-qa-service/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./dataland-automated-qa-service/pyproject.toml ./

COPY ./dataland-automated-qa-service/src/main/ ./src/main/

COPY ./dataland-document-manager/documentManagerOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json
COPY ./dataland-backend/backendOpenApi.json ./dataland-backend/backendOpenApi.json
RUN openapi-python-client generate --path ./dataland-document-manager/documentManagerOpenApi.json
RUN openapi-python-client generate --path ./dataland-backend/backendOpenApi.json
RUN pip install --no-cache-dir ./dataland-backend-api-documentation-client
RUN pip install --no-cache-dir ./dataland-document-manager-api-documentation-client
RUN pip install -e .

ENTRYPOINT coverage run --source="./src/main" ./src/main/entrypoint.py

HEALTHCHECK --interval=30s --timeout=3s CMD exit 0
