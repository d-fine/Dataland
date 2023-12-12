import sys
import os
import logging

import rabbitmq as mq


def main():
    mq.listen_to_message_queue()


if __name__ == "__main__":
    logging.basicConfig(
        format="%(asctime)s %(levelname)s: %(name)s: %(message)s", level=logging.INFO
    )
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
