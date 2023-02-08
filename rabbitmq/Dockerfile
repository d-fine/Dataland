FROM rabbitmq:3.11-management
COPY ./rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]

HEALTHCHECK --interval=30s --timeout=3s CMD true