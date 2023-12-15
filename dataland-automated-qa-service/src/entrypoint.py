import sys
import os
import logging
import pika.exceptions

from infrastructure.rabbitmq import RabbitMq
from infrastructure.messaging import qa_data, qa_document
import infrastructure.properties as p


def main():
    logging.basicConfig(
        format="%(asctime)s %(levelname)s: %(name)s: %(message)s",
        level=logging.INFO
    )
    while True:
        try:
            mq = RabbitMq(p.rabbit_mq_connection_parameters)
            mq.connect()
            mq.register_receiver(p.mq_receiving_exchange, p.mq_data_key, qa_data)
            mq.register_receiver(p.mq_receiving_exchange, p.mq_document_key, qa_document)
            mq.consume_loop()
        except pika.exceptions.ConnectionClosedByBroker:
            continue
        except pika.exceptions.AMQPChannelError as err:
            logging.error(f"Caught a channel error: {err}, stopping...")
            break
        except pika.exceptions.AMQPConnectionError:
            logging.error("Connection was closed, retrying...")
            continue


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
