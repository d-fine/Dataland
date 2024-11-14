import pika
import os

rabbit_mq_connection_parameters = pika.ConnectionParameters(
    host="rabbitmq",
    port=5672,
    virtual_host="vhost",
    credentials=pika.credentials.PlainCredentials(os.environ["RABBITMQ_USER"], os.environ["RABBITMQ_PASS"]),
    connection_attempts=100000,
    retry_delay=5,
)

backend_api_url = os.environ["INTERNAL_BACKEND_URL"]
document_manager_api_url = "http://document-manager:8080/documents"

mq_data_key = "data"
mq_document_key = "document"
mq_persist_automated_qa_result_key = "persistAutomatedQaResult"

mq_receiving_exchange = "itemStored"
mq_manual_qa_requested_exchange = "manualQaRequested"

mq_correlation_id_header = "cloudEvents:id"
mq_message_type_header = "cloudEvents:type"

mq_automated_qa_complete_type = "Automated QA complete"
mq_persist_automated_qa_result = "Persist automated QA result"
