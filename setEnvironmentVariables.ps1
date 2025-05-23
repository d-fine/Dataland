# USAGE: set github token and user and simply run this script in an ELEVATED! powershell window
# The Script can take quite some time (something like a minute) to complete
# SET to your own value! [Environment]::SetEnvironmentVariable("GITHUB_TOKEN", "", [System.EnvironmentVariableTarget]::User)
# SET to your own value! [Environment]::SetEnvironmentVariable("GITHUB_USER", "", [System.EnvironmentVariableTarget]::User)
# SET to the values provided in our internal Wiki! [Environment]::SetEnvironmentVariable("MAILJET_API_ID", "", [System.EnvironmentVariableTarget]::User)
# SET to the values provided in our internal Wiki! [Environment]::SetEnvironmentVariable("MAILJET_API_SECRET", "", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("BACKEND_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("EMAIL_SERVICE_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_EMAIL_SERVICE_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("API_KEY_MANAGER_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("INTERNAL_STORAGE_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DOCUMENT_MANAGER_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("QA_SERVICE_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("COMMUNITY_MANAGER_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("USER_SERVICE_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("PGADMIN_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("ELECTRON_EXTRA_LAUNCH_ARGS", "--ignore-connections-limit=local-dev.dataland.com", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("INITIALIZE_KEYCLOAK", "false", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_ADMIN", "admin", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_ADMIN_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_DB_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_FRONTEND_URL", "https://local-dev.dataland.com/keycloak", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_READER_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_READER_SALT", "K9nzg086pCNjq8dxl84GZg==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_READER_VALUE", "Dic1AEUG0nKnZ8me/3GMxgfyUmbwmJwDWNXkC7arLcij2BDB0xeOgk8ZpfZPKmFNikr9Is5I4+Nyk3MB9zs7mA==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_UPLOADER_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_UPLOADER_SALT", "K9nzg086pCNjq8dxl84GZg==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_UPLOADER_VALUE", "Dic1AEUG0nKnZ8me/3GMxgfyUmbwmJwDWNXkC7arLcij2BDB0xeOgk8ZpfZPKmFNikr9Is5I4+Nyk3MB9zs7mA==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_REVIEWER_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_REVIEWER_SALT", "K9nzg086pCNjq8dxl84GZg==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_REVIEWER_VALUE", "Dic1AEUG0nKnZ8me/3GMxgfyUmbwmJwDWNXkC7arLcij2BDB0xeOgk8ZpfZPKmFNikr9Is5I4+Nyk3MB9zs7mA==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_PREMIUM_USER_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_PREMIUM_USER_SALT", "K9nzg086pCNjq8dxl84GZg==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_PREMIUM_USER_VALUE", "Dic1AEUG0nKnZ8me/3GMxgfyUmbwmJwDWNXkC7arLcij2BDB0xeOgk8ZpfZPKmFNikr9Is5I4+Nyk3MB9zs7mA==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_DATALAND_ADMIN_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_DATALAND_ADMIN_SALT", "K9nzg086pCNjq8dxl84GZg==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("KEYCLOAK_DATALAND_ADMIN_VALUE", "Dic1AEUG0nKnZ8me/3GMxgfyUmbwmJwDWNXkC7arLcij2BDB0xeOgk8ZpfZPKmFNikr9Is5I4+Nyk3MB9zs7mA==", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_BATCH_MANAGER_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_COMMUNITY_MANAGER_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_QA_SERVICE_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_DOCUMENT_MANAGER_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_BACKEND_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_USER_SERVICE_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATALAND_DATA_EXPORTER_CLIENT_SECRET", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("EXPECT_STACKTRACE", "true", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("NOTIFICATION_INTERNAL_RECEIVERS", "dummy.mail@example.com", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("NOTIFICATION_INTERNAL_CC", "dummy.mail2@example.com", [System.EnvironmentVariableTarget]::User)
# If needed, set to your sonar token [Environment]::SetEnvironmentVariable("SONAR_TOKEN", "", [System.EnvironmentVariableTarget]::User)
# Only needed in CD.yaml - no need to set locally [Environment]::SetEnvironmentVariable("SSH_PRIVATE_KEY", "", [System.EnvironmentVariableTarget]::User)
# Only needed in CD.yaml - no need to set locally [Environment]::SetEnvironmentVariable("TARGETSERVER_HOST_KEYS", "", [System.EnvironmentVariableTarget]::User)
# Only needed in CD.yaml - except when you are working on Grafana alerts [Environment]::SetEnvironmentVariable("TARGETSERVER_URL", "", [System.EnvironmentVariableTarget]::User)
# If needed (to execute CI Tests locally) - set to Value that can be found in internal wiki [Environment]::SetEnvironmentVariable("KEYCLOAK_GOOGLE_ID", "", [System.EnvironmentVariableTarget]::User)
# If needed (to execute CI Tests locally) - set to Value that can be found in internal wiki [Environment]::SetEnvironmentVariable("KEYCLOAK_GOOGLE_SECRET", "", [System.EnvironmentVariableTarget]::User)
# If needed (to execute CI Tests locally) - set to Value that can be found in internal wiki [Environment]::SetEnvironmentVariable("KEYCLOAK_LINKEDIN_ID", "", [System.EnvironmentVariableTarget]::User)
# If needed (to execute CI Tests locally) - set to Value that can be found in internal wiki [Environment]::SetEnvironmentVariable("KEYCLOAK_LINKEDIN_SECRET", "", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("PROXY_PRIMARY_URL", "local-dev.dataland.com", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("PROXY_LETSENCRYPT_PATH", "/etc/letsencrypt/local-dev.dataland.com", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("RABBITMQ_USER", "admin", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("RABBITMQ_PASS", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("RABBITMQ_PASS_HASH", "0YAFHtIJtDTGbDqq7hsedvOYJEXBXgFr0Nzs1q9AnyDL1NVr", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("INTERNAL_BACKEND_URL", "http://host.docker.internal:8080/api", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("FRONTEND_LOCATION_CONFIG", "Test", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("BACKEND_LOCATION_CONFIG", "Dev", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("BACKEND_URL", "http://host.docker.internal:8080/api/", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("DATA_EXPORTER_OUTPUT_DIRECTORY", "./dataland-data-exporter", [System.EnvironmentVariableTarget]::User)

# EuroDaT credentials - if value is not hardcoded here, look them up in our wiki
[Environment]::SetEnvironmentVariable("EURODAT_BASE_URL", "https://app.int.eurodat.org", [System.EnvironmentVariableTarget]::User)
# [Environment]::SetEnvironmentVariable("EURODAT_CLIENT_TLS_CERT", "", [System.EnvironmentVariableTarget]::User)
# [Environment]::SetEnvironmentVariable("KEY_STORE_FILE_PASSWORD", "", [System.EnvironmentVariableTarget]::User)
# [Environment]::SetEnvironmentVariable("QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD", "", [System.EnvironmentVariableTarget]::User)
# [Environment]::SetEnvironmentVariable("QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_PASSWORD", "", [System.EnvironmentVariableTarget]::User)
# [Environment]::SetEnvironmentVariable("QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_STORE_PASSWORD", "", [System.EnvironmentVariableTarget]::User)

[Environment]::SetEnvironmentVariable("LOGGING_OPTIONS_MAX_SIZE", "100M", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("LOGGING_OPTIONS_MAX_FILE", "1", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("GRAFANA_ADMIN", "admin", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("GRAFANA_PASSWORD", "password", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("LOKI_VOLUME", "./dataland-loki/data", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("SLACK_ALERT_URL", "no_alerts_from_local_dev", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("SLACK_CRITICAL_ALERT_URL", "no_alerts_from_local_dev", [System.EnvironmentVariableTarget]::User)
