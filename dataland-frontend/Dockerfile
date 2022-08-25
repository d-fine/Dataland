FROM nginx:stable-alpine as production-stage
COPY ./dist /usr/share/nginx/html
COPY ./default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/defaultHeaders.conf /etc/nginx/utils/defaultHeaders.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]