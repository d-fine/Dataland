events {
    # Leaving empty events section because it is a required section
}

http {
  error_log /etc/nginx/error_log.log warn;
  client_max_body_size 20m;

  proxy_cache_path      /etc/nginx/cache keys_zone=one:500m max_size=1000m;
  proxy_set_header      Host $host;
  proxy_set_header      X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header      X-Forwarded-Host $server_name;
  proxy_set_header      Upgrade $http_upgrade;
  proxy_set_header      X-Real-IP $remote_addr;
  proxy_buffer_size     128k;
  proxy_buffers         4 256k;
  proxy_busy_buffers_size 256k;

  # This is required to proxy Grafana Live WebSocket connections.
  map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
  }

  server {
    listen 6789 default_server;
    server_name _;

<#if keycloak>
    location /keycloak {
      proxy_pass http://keycloak:8080/keycloak;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto http;
      proxy_set_header X-Forwarded-Host $host;
    }

</#if>
<#if pgadmin>
    location /pgadmin {
      proxy_set_header X-Script-Name /pgadmin;
      proxy_set_header X-Scheme $scheme;
      proxy_set_header Host "dataland-admin:6789";
      proxy_pass http://pgadmin;
      proxy_redirect off;
    }

</#if>
<#if rabbitmq>
    location /rabbitmq/ {
      proxy_pass http://rabbitmq:15672/;
    }

</#if>

    location /health/admin-proxy {
      return 200 'UP';
    }
<#if grafana>
    location /grafana/ {
       rewrite  ^/grafana/(.*)  /$1 break;
       proxy_set_header Host $http_host;
       proxy_pass http://grafana:3000;
    }

    # Proxy Grafana Live WebSocket connections.
    location /grafana/api/live/ {
      rewrite  ^/grafana/(.*)  /$1 break;
      proxy_http_version 1.1;
      proxy_set_header Upgrade $http_upgrade;
      proxy_set_header Connection $connection_upgrade;
      proxy_set_header Host $http_host;
      proxy_pass http://grafana:3000;
    }
</#if>
  }
}

stream {
<#if backendDb>
    # BACKEND
    upstream backend-db {
        server backend-db:5432;
    }

    server {
        listen 5433 so_keepalive=on;
        proxy_pass backend-db;
    }

</#if>
<#if keycloak>
    # KEYCLOAK
    upstream keycloak-db {
        server keycloak-db:5432;
    }

    server {
        listen 5434 so_keepalive=on;
        proxy_pass keycloak-db;
    }

</#if>
<#if apiKeyManager>
    # API-KEY-MANAGER
    upstream api-key-manager-db {
        server api-key-manager-db:5432;
    }

    server {
        listen 5435 so_keepalive=on;
        proxy_pass api-key-manager-db;
    }

</#if>
<#if documentManager>
    # DOCUMENT-MANAGER
    upstream document-manager-db {
        server document-manager-db:5432;
    }

    server {
        listen 5437 so_keepalive=on;
        proxy_pass document-manager-db;
    }

</#if>
<#if qaService>
    # QA Service
    upstream qa-service-db {
        server qa-service-db:5432;
    }

    server {
        listen 5438 so_keepalive=on;
        proxy_pass qa-service-db;
    }

</#if>
<#if communityManager>
    # COMMUNITY-MANAGER
    upstream community-manager-db {
        server community-manager-db:5432;
    }

    server {
        listen 5439 so_keepalive=on;
        proxy_pass community-manager-db;
    }

</#if>
<#if emailService>
    # EMAIL-SERVICE
    upstream email-service-db {
        server email-service-db:5432;
    }

    server {
        listen 5440 so_keepalive=on;
        proxy_pass email-service-db;
    }

</#if>
<#if userService>
    # USER-SERVICE
    upstream user-service-db {
        server user-service-db:5432;
    }

    server {
        listen 5441 so_keepalive=on;
        proxy_pass user-service-db;
    }

</#if>
<#if rabbitmq>
    # RabbitMQ
    upstream rabbitmq {
        server rabbitmq:5672;
    }

    server {
        listen 5672 so_keepalive=on;
        proxy_pass rabbitmq;
    }

</#if>
<#if internalStorage>
    # Internal-Storage
    upstream internal-storage-db {
        server internal-storage-db:5432;
    }

    server {
        listen 5436 so_keepalive=on;
        proxy_pass internal-storage-db;
    }
</#if>
}
