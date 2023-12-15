FROM python:3.11.7

WORKDIR /usr/src/app

COPY ./dataland-automated-qa-service/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./dataland-automated-qa-service/src/ ./

COPY ./dataland-document-manager/documentManagerOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json
COPY ./dataland-backend/backendOpenApi.json ./dataland-backend/backendOpenApi.json
RUN openapi-python-client generate --path ./dataland-document-manager/documentManagerOpenApi.json
RUN openapi-python-client generate --path ./dataland-backend/backendOpenApi.json
RUN pip install --no-cache-dir ./dataland-backend-api-documentation-client
RUN pip install --no-cache-dir ./dataland-document-manager-api-documentation-client

ENTRYPOINT python ./entrypoint.py

HEALTHCHECK --interval=30s --timeout=3s CMD top -n 1 | grep -c python > /dev/null || exit 1
