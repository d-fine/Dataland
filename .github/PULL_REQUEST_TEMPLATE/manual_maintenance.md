# Manual Maintenance Sprint [NR]
Note: To create a PR using this template add the query parameter `template=manual_maintenance.md` to the merge request creation URL (or simply copy this md file into the description)
# Maintenance tasks (to be completed by the assignee)
## Dataland
### Skipped updates
The following known issues need to be reviewed in case a compatible version is available. Add new known issues as they appear.
- [ ] io.gitlab.arturbosch.detekt:detekt-cli 1.22.0 (Failed to compile)
- [ ] Update to gradle 8.0.2 is skipped due to task dependency issues in the building process. Backlog: DALA-1295
- [ ] Update keycloak 21.0.1 is skipped due to issues in the realm.json, the new docker image, account console

### Gradle update
- [ ] Execute `gradlew dependencyUpdates` to get a report on Dependencies with updates
- [ ] Update `settings.gradle.kts` (for libraries), `build.gradle.kts` (for plugins) and `gradle.properties` (for jacoco and ktlint)
- [ ] update the gradle wrapper: execute `gradle wrapper --gradle-version X.Y.Z`

### Dataland frontend
- [ ] Update node version in `dataland-frontend/build.gradle.kts`
- [ ] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in package.json
- [ ] Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check for security issues

### Dataland keycloak theme
- [ ] Update node version in `dataland-keycloak/dataland_theme/login/build.gradle.kts`
- [ ] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in package.json
- [ ] Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check for security issues

### Dockerfile updates
Update versions in the following dockerfiles
- [ ] `./dataland-api-key-manager/Dockerfile`
- [ ] `./dataland-api-key-manager/DockerfileBase`
- [ ] `./dataland-api-key-manager/DockerfileTest`
- [ ] `./dataland-backend/Dockerfile`
- [ ] `./dataland-backend/DockerfileBase`
- [ ] `./dataland-backend/DockerfileTest`
- [ ] `./dataland-csvconverter/Dockerfile`
- [ ] `./dataland-e2etests/Dockerfile`
- [ ] `./dataland-e2etests/DockerfileBase`
- [ ] `./dataland-frontend/Dockerfile`
- [ ] `./dataland-frontend/DockerfileTest`
- [ ] `./dataland-internal-storage/Dockerfile`
- [ ] `./dataland-internal-storage/DockerfileTest`
- [ ] `./dataland-qa-service/Dockerfile`
- [ ] `./dataland-qa-service/DockerfileTest`
- [ ] `./dataland-rabbitmq/Dockerfile`
- [ ] `./dataland-inbound-admin-proxy/Dockerfile`
- [ ] `./dataland-inbound-proxy/DockerfileBase`
- [ ] `./dataland-inbound-proxy/Dockerfile`
- [ ] `./dataland-pgadmin/Dockerfile`
- [ ] `./dataland-keycloak/Dockerfile`  (also update realm json files with new version)
- [ ] `./base-dockerfiles/DockerfileGradle`
- [ ] Update the versions of the external images for api-key-manager-db, backend-db, keycloak-db, internal-storage-db and frontend-dev
- [ ] Check if there are any services in the `docker-compose.yml` file that have not gotten an update yet (e.g. a new service that is not covered by the tasks above)

## Dataland Monitoring
- [ ] Go to the monitoring repository and execute the tasks described in the manual maintenance template there

## Server updates
Execute `sudo apt-get update && sudo apt-get upgrade` on
- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] test.dataland.com
- [ ] tunnel.dataland.com
- [ ] letsencrypt.dataland.com
- [ ] monitoring.dataland.com 
- [ ] (OPT) dataland.com

## Check e-mails sent by keycloak
- [ ] Check that the verification e-mail is sent and displayed correctly
- [ ] Check that the reset password e-mail is sent and displayed correctly
- [ ] Check that the password can be reset
- [ ] Check that account linking via e-mail verification works correctly
- [ ] Check that account linking via username & password verification works correctly

## Check e-mails sent by backend
- [ ] Send an invitation request from one of the dev servers and check if the e-mail response contains the right attachments and is displayed correctly.

## Conclusion
- [ ] After updating all components check if everything still works
- [ ] The new version is deployed to the dev server using this branch and real data
  - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
  - [ ] It's verified that real data has been used
  - [ ] It's verified that everything seems to be working fine by manually using the website
- [ ] This template has been updated to reflect the latest state of tasks required and known issues with upgrades
- [ ] The Merge Request commit message needs to contain 'manual maintenance' to satisfy the CI maintenance check in future commits

# Review (to be completed by the reviewer)
- [x] The Github Actions (including Sonarqube Gateway and Lint Checks) are green. This is enforced by Github.
- [ ] A peer-review has been executed
  - [ ] The code has been manually inspected by someone who did not implement the feature
- [ ] The PR actually implements what is described above
- [ ] Documentation is updated as required
- [ ] The automated deployment is updated if required
- [ ] The new version is deployed to the dev server using this branch
  - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
  - [ ] It's verified that everything seems to be working fine by manually using the website
  - [ ] All implemented Social Logins have been tested manually in the UI
  - [ ] Go to the swagger-UI, authorize, run a "GET" request to the companies endpoint and assure that your authorization has worked by assuring that you get a 200 response

- [ ] If any work on the UI is to be merged, those changes were also documented in the Figma
- [ ] The local Dev stack still works: execute `startDevelopmentStack.sh` and execute `npm run testpipeline -- --env EXECUTION_ENVIRONMENT=""` in dataland-frontend
- [ ] After(!) the cypress tests have passed locally, execute the backend-e2e-tests `./gradlew dataland-e2etests:test`
- [ ] Locally: Go to the swagger-UI, authorize, run a "GET" request to the companies endpoint and assure that your authorization has worked by assuring that you get a 200 response
