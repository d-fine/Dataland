<#if internalStorage>
location /internal-storage/ {
  proxy_pass http://internal-storage:8080/internal-storage/;
  include utils/proxy.conf;
}

</#if>
<#if externalStorage>
location /external-storage/ {
  proxy_pass http://external-storage:8080/external-storage/;
  include utils/proxy.conf;
}
</#if>