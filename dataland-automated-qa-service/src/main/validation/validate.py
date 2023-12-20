import logging
from ..infrastructure.qa_exceptions import AutomaticQaNotPossibleError
from ..infrastructure.resources import DataResource, DocumentResource
from dataland_backend_api_documentation_client.models import QaStatus


def validate_data(resource: DataResource, correlation_id: str) -> QaStatus:
    logging.info(
        f"Auto-forwarding data with id {resource.id} to manual Qa. (Correlation ID: {correlation_id})"
    )
    raise AutomaticQaNotPossibleError("dummy implementation")


def validate_document(resource: DocumentResource, correlation_id: str) -> QaStatus:
    logging.info(
        f"Auto-forwarding document with id {resource.id} to manual Qa. (Correlation ID: {correlation_id})"
    )
    raise AutomaticQaNotPossibleError("dummy implementation")
