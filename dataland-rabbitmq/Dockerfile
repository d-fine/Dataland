FROM rabbitmq:3.13.2-management@sha256:eee9afbc17c32424ba6309dfd2d9efc9b9b1863ffe231b3d2be2815758b0d649
COPY ./dataland-rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./dataland-rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
RUN chown root ./custom_entrypoint.sh
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]
