import sys
import os
import logging

from infrastructure.validator import ValidatorHolder
from validation.registration import register_data_validators, register_document_validators
import infrastructure.rabbitmq as mq


def main():
    logging.basicConfig(
        format="%(asctime)s %(levelname)s: %(name)s: %(message)s",
        level=logging.INFO
    )
    data_validators = ValidatorHolder()
    document_validators = ValidatorHolder()
    register_data_validators(data_validators)
    register_document_validators(document_validators)
    mq.listen_to_message_queue(data_validators, document_validators)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
