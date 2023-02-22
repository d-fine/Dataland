FROM rabbitmq:3.11-management
COPY ./dataland-rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./dataland-rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]
