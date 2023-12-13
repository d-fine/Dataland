import sys
import os
import logging

import infrastructure.rabbitmq as mq


def main():
    logging.basicConfig(
        format="%(asctime)s %(levelname)s: %(name)s: %(message)s",
        level=logging.INFO
    )
    mq.listen_to_message_queue()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
