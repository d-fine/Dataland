FROM rabbitmq:3.13.7-management@sha256:8fefc884ccb6448236ea8f064782740246f1cef7215535f75d8eeec485144ba1
COPY ./dataland-rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./dataland-rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
RUN chown root ./custom_entrypoint.sh
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]
