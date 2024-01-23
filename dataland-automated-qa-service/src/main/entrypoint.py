import infrastructure.properties as p
from infrastructure.messaging import qa_data, qa_document
from infrastructure.rabbitmq import RabbitMq
import pika.exceptions
import logging


def main() -> None:
    """This is the entrypoint function"""
    logging.basicConfig(format="%(asctime)s %(levelname)s: %(name)s: %(message)s", level=logging.INFO)
    logging.info("Starting service")
    while True:
        try:
            mq = RabbitMq(p.rabbit_mq_connection_parameters)
            mq.connect()
            mq.register_receiver(p.mq_receiving_exchange, p.mq_data_key, "dataStored", qa_data)
            mq.register_receiver(
                p.mq_receiving_exchange,
                p.mq_document_key,
                "documentStored",
                qa_document,
            )
            mq.consume_loop()
        except pika.exceptions.ConnectionClosedByBroker:  # noqa: PERF203
            continue
        except pika.exceptions.AMQPChannelError as err:
            logging.error(f"Caught a channel error: {err}, stopping...")
            break
        except pika.exceptions.AMQPConnectionError:
            logging.error("Connection was closed, retrying...")
            continue


if __name__ == "__main__":
    main()
