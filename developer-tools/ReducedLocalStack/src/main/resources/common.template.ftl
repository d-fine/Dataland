location @errorPage {
  ssi on;
  ssi_types application/json;
  internal;
  root /var/www/html/error_documents;
  try_files /json_errors.json =500;
}

<#if apiKeyManager>
location /api-keys/ {
  proxy_pass http://api-key-manager:8080/api-keys/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

</#if>
location /api/internal/ {
  return 301 https://${r"${PROXY_PRIMARY_URL}"}/nocontent;
}

location /api/ {
  client_max_body_size ${r"${DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES}"}M;
  proxy_pass ${r"${BACKEND_URL}"};
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

<#if documentManager>
location /documents/ {
  client_max_body_size ${r"${DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES}"}M;
  proxy_pass http://document-manager:8080/documents/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

location /documents/internal/ {
  return 301 https://${r"${PROXY_PRIMARY_URL}"}/nocontent;
}

</#if>
<#if internalStorage>
location /internal-storage/actuator/health {
  proxy_pass http://internal-storage:8080/internal-storage/actuator/health;
  include utils/proxy.conf;
}

</#if>
<#if externalStorage>
location /external-storage/actuator/health {
  proxy_pass http://external-storage:8080/external-storage/actuator/health;
  include utils/proxy.conf;
}

</#if>
<#if qaService>
location /qa/ {
  proxy_pass http://qa-service:8080/qa/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

</#if>
<#if communityManager>
location /community/ {
  proxy_pass http://community-manager:8080/community/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

</#if>
<#if userService>
location /users/ {
  proxy_pass http://user-service:8080/users/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

</#if>
<#if emailService>
location /email/ {
  proxy_pass http://email-service:8080/email/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

</#if>
<#if specificationService>
location /specifications/ {
  proxy_pass http://specification-service:8489/specifications/;
  include utils/proxy.conf;
  include utils/errorsAsJson.conf;
}

</#if>
location /images/ {
  root /var/www;
}

location = /gitinfo {
  root /var/www/html;
  default_type application/json;
}

location = /health/proxy {
  return 200 'UP';
}
