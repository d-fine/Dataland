FROM nginx:1.27.2@sha256:28402db69fec7c17e179ea87882667f1e054391138f77ffaf0c3eb388efc3ffb
COPY ./dataland-specification-service/custom_entrypoint.sh ./custom_entrypoint.sh
COPY ./dataland-specification-service/resources /usr/share/nginx/html
COPY ./dataland-specification-service/default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/securityHeaderMap.conf /etc/nginx/utils/securityHeaderMap.conf
COPY ./dataland-inbound-proxy/config/utils/security.conf /etc/nginx/utils/security.conf

RUN chmod +x ./custom_entrypoint.sh

CMD ["sh", "-c", "/custom_entrypoint.sh"]
