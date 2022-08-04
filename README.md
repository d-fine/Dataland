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

| Variable name                     | Description                                                                                                            | example values                                                   |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| DATALAND_SKYMINDERCLIENT_TOKEN    | An Access Token to access the Github Skyminder Package registry                                                        |                                                                  |
| DATALAND_SKYMINDERCLIENT_USER     | The User corresponding to `DATALAND_SKYMINDERCLIENT_TOKEN`                                                             |                                                                  |
| DATALAND_EDC_TOKEN                | An Access Token to access the Github DatalandEDC Package registry                                                      |                                                                  |
| DATALAND_EDC_USER                 | The User corresponding to `DATALAND_EDC_TOKEN`                                                                         |                                                                  |
| SKYMINDER_URL                     | The base URL of the Skyminder API                                                                                      |                                                                  |
| SKYMINDER_USER                    | The username for the Skyminder API                                                                                     |                                                                  |
| SKYMINDER_PW                      | The password for the Skyminder API                                                                                     |                                                                  |
| FRONTEND_DOCKERFILE               | Defines the dockerfile to be used for the fronted container in the docker compose stack                                | `./dataland-frontend/DockerfileTest`                             |
| BACKEND_DOCKERFILE                | Defines the dockerfile to be used for the backend container in the docker compose stack                                | `./dataland-backend/DockerfileTest`                              |
| KEYCLOAK_DOCKERFILE               | Defines the dockerfile to be used for the keycloak container in the docker compose stack                               | `./dataland-keycloak/Dockerfile`                                 |
| KEYCLOAK_ADMIN                    | Defines the name of the admin user when keycloak is set up from scratch                                                |                                                                  |
| KEYCLOAK_ADMIN_PASSWORD           | Defines the password for the admin user when keycloak is set up from scratch                                           |                                                                  |
| KEYCLOAK_FRONTEND_URL             | Defines the frontend URL to be used when keycloak is set up from scratch                                               |                                                                  |
| KEYCLOAK_DB_PASSWORD              | Defines the password for the keycloak DB when keycloak is set up from scratch                                          |                                                                  |
| KEYCLOAK_READER/UPLOADER_PASSWORD | Defines the password for the technical users (data_reader and data_uploader) in keycloak for reading or uploading data |                                                                  |
| KEYCLOAK_READER/UPLOADER_VALUE    | Together with KEYCLOAK_(READER\UPLOADER)_SALT it defines the secret for the keycloak realm json file                   |                                                                  |
| KEYCLOAK_READER/UPLOADER_SALT     | Together with KEYCLOAK_(READER\UPLOADER)_VALUE it defines the secret for the keycloak realm json file                  |                                                                  |
| PROXY_NGINX_URLS                  | A list of URLS for the NGINX Server config (multiple domains separated by whitespace)                                  | `www.dataland.com dataland.com`                                  |
| PROXY_LETSENCRYPT_PATH            | The LetsEncrypt path for the domain (usually /etc/letsencrypt/FIRST_DOMAIN/                                            | `/etc/letsencrypt/dataland.com`                                  |
| PROXY_LETSENCRYPT_ARGS            | The LetsEncrypt Certbot arguments for the initial certificate request                                                  | `--email dataland@d-fine.de -d dataland.com -d www.dataland.com` |
| PROXY_ENVIRONMENT                 | The environment of the proxy server (development or production). Used during Docker build process                      | `development` or `production`                                    |


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

## Dependency Management
we try to keep our dependencies up to date. Therefore, every sprint we update dependency versions in a separate PR.
To do so:
* Execute `gradlew dependencyUpdates` to get a report on Dependencies with updates
* update `settings.gradle.kts` (for libraries), `build.gradle.kts` (for plugins) and `gradle.properties` (for jacoco)
* Note: fasterXML is managed by spring and ktlint by jlleitschuh, thus NO manual version update should be conducted
* update the gradle wrapper: execute `gradle wrapper --gradle-version X.Y.Z`
* * Update node version in `dataland-frontend/build.gradle.kts`
* Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in package.json  
  Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check for security issues 
  (Known issues appeared in the past with updating Jest, openApiGenerator and Eslint).
  Do this in the frontend as well as in the keycloak theme
* Update Docker images: run CypressImage and TemurinImage jobs in GitHub actions. Don't forget to update KeyCloak!
* Do the above also for the connected Repos (SkyminderClient, DatalandEDC). Publish new versions of artifacts if required. Use the new artifacts wherever relevant
* After updating all components check if everything is still working