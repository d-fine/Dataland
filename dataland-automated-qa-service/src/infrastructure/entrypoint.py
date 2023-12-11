import sys
import os

import rabbitmq as mq


def main():
    mq.tutorial()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        sys.exit()
