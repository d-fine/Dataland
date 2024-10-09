FROM rabbitmq:3.13.7-management@sha256:b55a09baeef15e567a9403ce6c3c84ab06a97b48265954ae99876892c04fdfdf
COPY ./dataland-rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./dataland-rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
RUN chown root ./custom_entrypoint.sh
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]
