ARG DATALAND_AUTOMATED_QA_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_automated_qa_service_base:${DATALAND_AUTOMATED_QA_SERVICE_BASE_VERSION}

CMD ["sh", "-c", "python ./src/main/entrypoint.py"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
