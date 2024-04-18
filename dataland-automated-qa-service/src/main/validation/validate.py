import logging
from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError
from main.infrastructure.resources import DataResource, DocumentResource
from dataland_backend_api_documentation_client.models import QaStatus


def validate_data(resource: DataResource, correlation_id: str) -> QaStatus:
    """
    This function reviews a provided DataResource

    :param resource: the DataResource to be reviewed
    :param correlation_id: the correlation ID of the dataset journey
    :raises AutomaticQaNotPossibleError: when review could not be completed
    and the data should be manually reviewed
    :returns: the result of the review (must be either Accepted or Rejected)
    """
    logging.info(f"Auto-forwarding data with id {resource.id} to manual Qa. (Correlation ID: {correlation_id})")
    raise AutomaticQaNotPossibleError("dummy implementation")


def validate_document(resource: DocumentResource, correlation_id: str) -> QaStatus:
    """
    This function reviews a provided DocumentResource

    :param resource: the DocumentResource to be reviewed
    :param correlation_id: the correlation ID of the document journey
    :raises AutomaticQaNotPossibleError: when review could not be completed
    and the document should be manually reviewed
    :returns: the result of the review (must be either Accepted or Rejected)
    """
    logging.info(f"Auto-forwarding document with id {resource.id} to manual Qa. (Correlation ID: {correlation_id})")
    raise AutomaticQaNotPossibleError("dummy implementation")
