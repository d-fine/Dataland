# Dataland
Dataland is a platform to distribute ESG data. This repository contains the code for the Dataland Platform.

# Getting Started
This section will describe how to get started and get a local Dataland deployment up & running. As there's no code, nothing to be found here.

# License
This project is free and open-source software licensed under the [GNU Affero General Public License v3](LICENSE) (AGPL-3.0). Commercial use of this software is allowed. If derivative works are distributed, you need to be published the derivative work under the same license. Here, derivative works includes web publications. That means, if you build a web service using this software, you need to publish your source code under the same license (AGPL-3.0)

In case this does not work for you, please contact dataland@d-fine.de for individual license agreements.

# Contributions
Contributions are highly welcome. Please refer to our [contribution guideline](contribution/contribution.md).
To allow for individual licenses and eventual future license changes, we require a contributor license agreement from any contributor that allows us to re-license the software including the contribution.

# Developer Remarks
In this section, you find information that might be useful for you as a developer.
## add scripts to git with the executable flag
Especially under Windows, it's unclear which file permissions a script will get. 
To explicitly mark a script executable, do:
`git update-index --chmod=+x script.sh`
## Environment Variables
Some environment variables are used within the project. Find attached the variables and their meaning

| Variable name                     | Description                                                                                                                                         | example values                                                   |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| DATALAND_SKYMINDERCLIENT_TOKEN    | An Access Token to access the Github Skyminder Package registry                                                                                     |                                                                  |
| DATALAND_SKYMINDERCLIENT_USER     | The User corresponding to `DATALAND_SKYMINDERCLIENT_TOKEN`                                                                                          |                                                                  |
| DATALAND_EDC_TOKEN                | An Access Token to access the Github DatalandEDC Package registry                                                                                   |                                                                  |
| DATALAND_EDC_USER                 | The User corresponding to `DATALAND_EDC_TOKEN`                                                                                                      |                                                                  |
| SKYMINDER_URL                     | The base URL of the Skyminder API                                                                                                                   |                                                                  |
| SKYMINDER_USER                    | The username for the Skyminder API                                                                                                                  |                                                                  |
| SKYMINDER_PW                      | The password for the Skyminder API                                                                                                                  |                                                                  |
| FRONTEND_DOCKERFILE               | Defines the dockerfile to be used for the fronted container in the docker compose stack                                                             | `./dataland-frontend/DockerfileTest`                             |
| BACKEND_DOCKERFILE                | Defines the dockerfile to be used for the backend container in the docker compose stack                                                             | `./dataland-backend/DockerfileTest`                              |
| BACKEND_DB_PASSWORD               | Defines the password for the backend DB when keycloak is set up from scratch                                                                        |                                                                  |
| KEYCLOAK_DOCKERFILE               | Defines the dockerfile to be used for the keycloak container in the docker compose stack                                                            | `./dataland-keycloak/Dockerfile`                                 |
| KEYCLOAK_ADMIN                    | Defines the name of the admin user when keycloak is set up from scratch                                                                             |                                                                  |
| KEYCLOAK_ADMIN_PASSWORD           | Defines the password for the admin user when keycloak is set up from scratch                                                                        |                                                                  |
| KEYCLOAK_FRONTEND_URL             | Defines the frontend URL to be used when keycloak is set up from scratch                                                                            |                                                                  |
| KEYCLOAK_DB_PASSWORD              | Defines the password for the keycloak DB when keycloak is set up from scratch                                                                       |                                                                  |
| KEYCLOAK_READER/UPLOADER_PASSWORD | Defines the password for the technical users (data_reader and data_uploader) in keycloak for reading or uploading data                              |                                                                  |
| KEYCLOAK_READER/UPLOADER_VALUE    | Together with KEYCLOAK_(READER\UPLOADER)_SALT it defines the secret for the keycloak realm json file                                                |                                                                  |
| KEYCLOAK_READER/UPLOADER_SALT     | Together with KEYCLOAK_(READER\UPLOADER)_VALUE it defines the secret for the keycloak realm json file                                               |                                                                  |
| PROXY_PRIMARY_URL                 | The primary URL of the webservice. Requests to other URLS will get redirected to here (make sure this is consistent with the KEYCLOAK_FRONTEND_URL) | `dataland.com`                                                   |
| PROXY_LETSENCRYPT_PATH            | The LetsEncrypt path for the domain (usually /etc/letsencrypt/FIRST_DOMAIN/                                                                         | `/etc/letsencrypt/live/dataland.com`                             |
| PROXY_LETSENCRYPT_ARGS            | The LetsEncrypt Certbot arguments for the initial certificate request                                                                               | `--email dataland@d-fine.de -d dataland.com -d www.dataland.com` |
| PROXY_ENVIRONMENT                 | The environment of the proxy server (development or production). Used during Docker build process                                                   | `development` or `production`                                    |
| PGADMIN_PASSWORD                  | The password for the PGAdmin interface                                                                                                              | `password`                                                       |

Please note that the variables `KEYCLOAK_<USER>_PASSWORD`, `KEYCLOAK_<USER>_VALUE` and `KEYCLOAK_<USER>_SALT` need to be consistent.

## Local HTTPS testing
* Add A link for `dataland-local.duckdns.org` to `127.0.0.1` in the Hosts file (On Windows: `%windir%\system32\drivers\etc\hosts`, On Linux: `/etc/hosts`)
* The `startDevelopmentStack.sh` script will automatically retrieve signed SSL-Certificates for this domain.
* Access the development stack at https://dataland-local.duckdns.org

## API Documentation
The interactive backend API documentation is available via `[URL]/api/swagger-/api/swagger-ui/index.html`.
Requests can be authorized via two different methods:
- Option A: Manually obtain a bearer token from KeyCloak and enter it in the `default-bearer-auth` field.
- Option B: Automatically obtain a bearer token from KeyCloak by entering `dataland-public` for `client_id`and leaving `client_secret`empty (in the `default-oauth` section). Swagger will then redirect you two the KeyCloak Login form for authentication.

## Run Cypress Tests locally
* start the docker-compose stack with the "development" profile. Set the env-variables (see above). 
* start the backend - e.g. in IntelliJ or using gradle. Use the spring profile "development"
* start the frontend - using `npm run serve`. to be safe do an `npm install` and a `./gradlew generateAPIClientFrontend` beforehand.
* start the E2E cypress tests using `npm run teste2e`
