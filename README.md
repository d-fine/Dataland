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

| Variable name                  | Description                                                                               | example values                           |
|--------------------------------|-------------------------------------------------------------------------------------------|------------------------------------------|
| DATALAND_SKYMINDERCLIENT_TOKEN | An Access Token to access the Github Skyminder Package registry                           |                                          |
| DATALAND_SKYMINDERCLIENT_USER  | The User corresponding to `DATALAND_SKYMINDERCLIENT_TOKEN`                                |                                          |
| DATALAND_EDC_TOKEN             | An Access Token to access the Github DatalandEDC Package registry                         |                                          |
| DATALAND_EDC_USER              | The User corresponding to `DATALAND_EDC_TOKEN`                                            |                                          |
| SKYMINDER_URL                  | The base URL of the Skyminder API                                                         |                                          |
| SKYMINDER_USER                 | The username for the Skyminder API                                                        |                                          |
| SKYMINDER_PW                   | The password for the Skyminder API                                                        |                                          |
| FRONTEND_DOCKERFILE            | Defines the dockerfile to be used for the fronted container in the docker compose stack   | `./dataland-frontend/DockerfileTest`     |
| BACKEND_DOCKERFILE             | Defines the dockerfile to be used for the backend container in the docker compose stack   | `./dataland-backend/DockerfileTest`      |

## Run Cypress Tests locally
* start the docker-compose stack with the "development" profile. Set the env-variables (see above). 
* start the backend - e.g. in IntelliJ or using gradle. Use the spring profile "development"
* start the frontend - using `npm run serve`. to be safe do an `npm install` and a `./gradlew generateAPIClientFrontend` beforehand.
* start the E2E cypress tests using `npm run teste2e`

## Dependency Management
we try to keep our dependencies up to date. Therefore, every two sprints we update dependency versions in a seperate PR.
To do so:
* Execute gradlew dependencyUpdates to get a report on Dependencies with updates
* update `settings.gradle.kts` (for libraries), `build.gradle.kts` (for plugins) and `gradle.properties` (for jacoco)
* update the gradle wrapper: execute `gradle wrapper --gradle-version X.Y.Z`
* Do so also for the connected Repos (SkyminderClient, DatalandEDC). Publish new versions of artifacts if required. Use the new artifacts wherever relevant
* Update Fronted packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in package.json  
  Run `npm update --save` to update `package-lock.json`
* Update Docker Images. Publish new versions of docker images in CI by running CypressImage and TemurinImage Jobs
* update node version in `dataland-frontend/build.gradle.kts`