import pika
import properties as p


def tutorial():
    print("RabbitMQ tutorial")
    RabbitMq(p.rabbit_mq_connection_parameters).tutorial_send()


class RabbitMq:
    def __init__(self, connection_parameters: pika.connection.Parameters):
        self.connection_parameters = connection_parameters

    def tutorial_send(self):
        connection = pika.BlockingConnection(self.connection_parameters)
        channel = connection.channel()
        channel.queue_declare(queue="hello")
        channel.basic_publish(
            exchange="dieter",
            routing_key="hello",
            body="Hello World!"
        )
        connection.close()
