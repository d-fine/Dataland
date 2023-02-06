FROM rabbitmq:3.7-management
COPY ./rabbitmq/inject_user_and_pass_hash.sh ./inject_user_and_pass_hash.sh
COPY ./rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
ENTRYPOINT ["./custom_entrypoint.sh"]
CMD ["rabbitmq-server"]