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
## Git Hooks
To add the provided git pre-hooks to your local development environment execute:
`git config --local core.hooksPath ./githooks/`
## Environment Variables
Some environment variables are used within the project. Find attached the variables and their meaning

| Variable name                                      | Description                                                                                                                                         | example values                                                   |
|----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| BACKEND_DB_PASSWORD                                | Defines the password for the backend DB when keycloak is set up from scratch                                                                        |                                                                  |
| KEYCLOAK_ADMIN                                     | Defines the name of the admin user when keycloak is set up from scratch                                                                             |                                                                  |
| KEYCLOAK_ADMIN_PASSWORD                            | Defines the password for the admin user when keycloak is set up from scratch                                                                        |                                                                  |
| KEYCLOAK_FRONTEND_URL                              | Defines the frontend URL to be used when keycloak is set up from scratch                                                                            |                                                                  |
| KEYCLOAK_DB_PASSWORD                               | Defines the password for the keycloak DB when keycloak is set up from scratch                                                                       |                                                                  |
| KEYCLOAK_(READER\UPLOADER\DATALAND_ADMIN)_PASSWORD | Defines the password for the technical users (data_reader and data_uploader) in keycloak for reading or uploading data                              |                                                                  |
| KEYCLOAK_(READER\UPLOADER\DATALAND_ADMIN)_VALUE    | Together with KEYCLOAK_(READER\UPLOADER\DATALAND_ADMIN)_SALT it defines the secret for the keycloak realm json file                                 |                                                                  |
| KEYCLOAK_(READER\UPLOADER\DATALAND_ADMIN)_SALT     | Together with KEYCLOAK_(READER\UPLOADER\DATALAND_ADMIN)_VALUE it defines the secret for the keycloak realm json file                                |                                                                  |
| PROXY_PRIMARY_URL                                  | The primary URL of the webservice. Requests to other URLS will get redirected to here (make sure this is consistent with the KEYCLOAK_FRONTEND_URL) | `dataland.com`                                                   |
| PROXY_LETSENCRYPT_PATH                             | The LetsEncrypt path for the domain (usually /etc/letsencrypt/live/FIRST_DOMAIN                                                                     | `/etc/letsencrypt/live/dataland.com`                             |
| PROXY_LETSENCRYPT_ARGS                             | The LetsEncrypt Certbot arguments for the initial certificate request                                                                               | `--email dataland@d-fine.de -d dataland.com -d www.dataland.com` |
| PGADMIN_PASSWORD                                   | The password for the PGAdmin interface                                                                                                              | `password`                                                       |
| EXPECT_STACKTRACE                                  | Set to true if the e2etests should expect a stacktrace for malicious request to the backend. Should be true locally, else not set.                  | `true`                                                           |

Please note that the variables `KEYCLOAK_<USER>_PASSWORD`, `KEYCLOAK_<USER>_VALUE` and `KEYCLOAK_<USER>_SALT` need to be consistent.

## Local HTTPS testing
* Add A link for `local-dev.dataland.com` to `127.0.0.1` in the Hosts file (On Windows: `%windir%\system32\drivers\etc\hosts`, On Linux: `/etc/hosts`)
* The `startDevelopmentStack.sh` script will automatically retrieve signed SSL-Certificates for this domain.
* Access the development stack at https://local-dev.dataland.com

## API Documentation
The interactive backend API documentation is available via `[URL]/api/swagger-/api/swagger-ui/index.html`.
Requests can be authorized via two different methods:
- Option A: Manually obtain a bearer token from KeyCloak and enter it in the `default-bearer-auth` field.
- Option B: Automatically obtain a bearer token from KeyCloak by entering `dataland-public` for `client_id`and leaving `client_secret`empty (in the `default-oauth` section). Swagger will then redirect you two the KeyCloak Login form for authentication.

## Run Cypress Tests locally
* start the local stack using "startDevelopmentStack.sh". Set the env-variables (see above). 
* The backend will be started automatically. You can kill it and run it from the IDE if you like (e.g. for Debugging)
* Either use cypress while watching the browser:
  * start the cypress UI by using `npm run cypress`
  * Select `E2E Testing` or `Component Testing` and run the tests
* Or se cypress without visible browser (more robust):
  * run `npm run testpipeline -- --env EXECUTION_ENVIRONMENT=""` 

## Licenses
This project makes use of open source dependencies. To see a list gradle dependencies along with their 
licenses, run `\gradlew generateLicenseReport` 