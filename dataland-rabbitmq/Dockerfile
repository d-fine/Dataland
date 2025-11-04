FROM rabbitmq:4.1.4-management@sha256:294b01e1796a8acede4619f32a1c394fae1f8021e57986ea01aad38dc2a4f502
COPY ./dataland-rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./dataland-rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
RUN chown root ./custom_entrypoint.sh
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]
