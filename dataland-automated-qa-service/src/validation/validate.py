import logging
from infrastructure.validation import AutomaticQaNotPossibleError
from infrastructure.resources import DataResource, DocumentResource


def validate_data(resource: DataResource, correlation_id: str):
    logging.info(
        f"Auto-forwarding data with id {resource.id} to manual Qa. (Correlation ID: {correlation_id})"
    )
    raise AutomaticQaNotPossibleError


def validate_document(resource: DocumentResource, correlation_id: str):
    logging.info(
        f"Auto-forwarding document with id {resource.id} to manual Qa. (Correlation ID: {correlation_id})"
    )
    raise AutomaticQaNotPossibleError
