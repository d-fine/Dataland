FROM rabbitmq:4.2.9-management@sha256:9e9fd21d60f060acc9ecbd847cd1dcb87e6550bdb0297c27e79a40bfe1766ce4
COPY ./dataland-rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./dataland-rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
RUN chown root ./custom_entrypoint.sh
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]
