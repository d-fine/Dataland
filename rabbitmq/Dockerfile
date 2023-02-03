FROM rabbitmq:3.7-management
COPY ./rabbitmq/inject_user_and_pass_hash.sh ./inject_user_and_pass_hash.sh
COPY ./rabbitmq/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./rabbitmq/definitions.json /etc/rabbitmq/definitions.json
COPY ./rabbitmq/rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
ENV RABBITMQ_USER="admin"
ENV RABBITMQ_PASS_HASH="1s3+3kZqRTAgidTV6NE+adO3ja5NFNQZHa9MBwhJ0Kx7Fw6F"
ENTRYPOINT ./custom_entrypoint.sh
#CMD["rabbitmq-server"]