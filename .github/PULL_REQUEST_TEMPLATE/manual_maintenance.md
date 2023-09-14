# Manual Maintenance Sprint [NR]

Note: To create a PR using this template add the query parameter `template=manual_maintenance.md` to the merge request
creation URL (or simply copy this md file into the description)

# Maintenance tasks (to be completed by the assignee)

## Dataland

### Skipped updates

The following known issues need to be reviewed in case a compatible version is available. Add new known issues as they
appear.
- [ ] Update node.js to version 20.6.1 breaks the build
- [ ] Update e2etests/Dockerfile update breaks the build
- [ ] Update "@vue/tsconfig" to >=0.2.0 introduces major changes in typescript rules (~500 TS Errors throughout the
  project and unresolved imports that are hard to fix), skipped.
- [ ] Update Cypress to >= 12.12.0 introduces an issue with the usage of `$route` in component test 
  `DatasetOverview.cy.ts`. Issue with Cypress has been created to hopefully resolve this from the side of Cypress:
  https://github.com/cypress-io/cypress/issues/26902
- [ ] Update Ktlint to >= 49.0 breaks the ktlint tasks (issue described here: 
  https://github.com/JLLeitschuh/ktlint-gradle/issues/665 and possible fix here: 
  https://github.com/JLLeitschuh/ktlint-gradle/pull/667)
- [ ] Update @zxcvbn-ts/language-common to 3.0.3 is skipped due to issues in rebuilding keycloak Docker images
- [ ] Update @zxcvbn-ts/language-en to 3.0.1 is skipped due to issues in rebuilding keycloak Docker images
- [ ] Flyway version from 9.18.0 and above (until 9.22.1) seem to cause issues. Future versions should be tried
- [ ] The docker-compose-plugin v.2.19.1 causes connection issues:
  If running `sudo apt-get update && sudo apt-get upgrade` on the servers causes connection issues
  this can be possibly fixed by reverting the docker-compose-plugin version
### Gradle update

- [x] Execute `gradlew dependencyUpdates` to get a report on Dependencies with updates
- [x] Update `settings.gradle.kts` (for libraries), `build.gradle.kts` (for plugins) and `gradle.properties` (for jacoco
  and ktlint)
- [x] update the gradle wrapper: execute `gradle wrapper --gradle-version X.Y.Z`

### Dataland frontend

- [x] Update node version in `dataland-frontend/build.gradle.kts`
- [x] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in
  package.json
- [x] Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check
  for security issues

### Dataland keycloak theme

- [x] Update node version in `dataland-keycloak/dataland_theme/login/build.gradle.kts`
- [x] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in
  package.json
- [x] Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check
  for security issues

### Dockerfile updates

Update versions in the following dockerfiles

- [x] `./dataland-api-key-manager/Dockerfile`
- [x] `./dataland-api-key-manager/DockerfileBase`
- [x] `./dataland-api-key-manager/DockerfileTest`
- [x] `./dataland-backend/Dockerfile`
- [x] `./dataland-backend/DockerfileBase`
- [x] `./dataland-backend/DockerfileTest`
- [x] `./dataland-document-manager/Dockerfile`
- [x] `./dataland-document-manager/DockerfileBase`
- [x] `./dataland-document-manager/DockerfileTest`
- [x] `./dataland-e2etests/Dockerfile`
- [x] `./dataland-e2etests/DockerfileBase`
- [x] `./dataland-frontend/Dockerfile`
- [x] `./dataland-frontend/DockerfileTest`
- [x] `./dataland-internal-storage/Dockerfile`
- [x] `./dataland-internal-storage/DockerfileBase`
- [x] `./dataland-internal-storage/DockerfileTest`
- [x] `./dataland-qa-service/Dockerfile`
- [x] `./dataland-qa-service/DockerfileBase`
- [x] `./dataland-qa-service/DockerfileTest`
- [x] `./dataland-rabbitmq/Dockerfile`
- [x] `./dataland-inbound-admin-proxy/Dockerfile`
- [x] `./dataland-inbound-proxy/Dockerfile`
- [x] `./dataland-pgadmin/Dockerfile`
- [x] `./dataland-keycloak/Dockerfile`  (also update realm json files with new version)
- [x] `./base-dockerfiles/DockerfileGradle`
- [x] Update the versions of the external images for api-key-manager-db, backend-db, keycloak-db, internal-storage-db,
  document-manager-db, qa-service-db and frontend-dev in `./docker-compose.yml`
- [x] Check if there are any services in the `docker-compose.yml` file that have not gotten an update yet (e.g. a new
  service that is not covered by the tasks above)

## Dataland Monitoring

- [x] Go to the monitoring repository and execute the tasks described in the manual maintenance template there

## Server updates

Execute `sudo apt-get update && sudo apt-get upgrade` on

- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] test.dataland.com
- [ ] letsencrypt.dataland.com
- [ ] monitoring.dataland.com
- [ ] (OPT) dataland.com

### ssh-keys maintenance

check that all ssh-keys are set and erased from people that have left

- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] test.dataland.com
- [ ] letsencrypt.dataland.com
- [ ] monitoring.dataland.com
- [ ] (OPT) dataland.com

## Check keycloak automatic logout if inactive

- [ ] Check that you are automatically logged out if you are idle for too long and also get notified about this by a
  pop-up. Timeouts are defined as `TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS`
  in `./dataland-frontend/src/utils/Constants.ts` and `ssoSessionIdleTimeout`
  in `./dataland-keycloak/realms/datalandsecurity-realm.json`.

## Check e-mails sent by keycloak

- [ ] Check that the verification e-mail is sent and displayed correctly
- [ ] Check that the reset password e-mail is sent and displayed correctly
- [ ] Check that the password can be reset
- [ ] Check that account linking via e-mail verification works correctly
- [ ] Check that account linking via username & password verification works correctly

## Check e-mails sent by backend

- [ ] Send an invitation (data) request from one of the dev servers and check if the e-mail to info@dataland.com
  contains the right attachments and is displayed correctly.

## Check RabbitMQ dead letter queue and disk space

- [ ] RabbitMQ does need at least 768MB of free disk space to operate. `ssh` into all servers and check the available
  disk space with `df` command. If the open disk space is close to the minimum requirement, clear up disk space
  with `sudo docker image prune --all`.
- [ ] On all environments, no new messages should have been added to the dead letter queue since the last manual
  maintenance. If new messages have appeared this does need to be investigated. The dead letter queue can be accessed
  and messages on it read in the RabbitMQ GUI. Access it by port-forwarding port `6789` from the server and then
  accessing the GUI at `localhost:6789/rabbitmq`. After login, the dead letter queue can be found at Queues &rarr;
  deadLetterQueue &rarr; Get message.

## Check that links to external webpages are working
- [ ] Go to the frameworks/upload page and check that the link to WWF Pathway to Paris is still working as intended.

## Check that the main branch has no sonar issues
- [ ] Go to the sonar report summary of the main branch and verify that there are no sonar findings. If there are sonar 
  findings fix them.

## Conclusion

- [ ] After updating all components check if everything still works
- [ ] The new version is deployed to a dev server using this branch
    - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
    - [ ] It's verified that everything seems to be working fine by manually using the website
- [ ] This template has been updated to reflect the latest state of tasks required and known issues with upgrades
- [ ] The Merge Request commit message needs to contain 'manual maintenance' to satisfy the CI maintenance check in
  future commits

# Review (to be completed by the reviewer)

- [ ] The Github Actions (including Sonarqube Gateway and Lint Checks) are green. This is enforced by Github.
- [ ] A peer-review has been executed
  - [ ] The code has been manually inspected by someone who did not implement the feature
- [ ] The PR actually implements what is described above
- [ ] Documentation is updated as required
- [ ] The automated deployment is updated if required
- [ ] The new version is deployed to the dev server "dev1" using this branch
  - [ ] Run with setting `Reset non-user related Docker Volumes & Re-populate` turned on
  - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
  - [ ] It's verified that the CD run is green
  - [ ] It's verified that everything seems to be working fine by manually using the website
  - [ ] All implemented Social Logins have been tested manually in the UI
  - [ ] Go to the swagger-UI, authorize, run a "GET" request to the companies endpoint and assure that your
    authorization has worked by assuring that you get a 200 response
- [ ] If any work on the UI is to be merged, those changes were also documented in the Figma
- [ ] The local Dev stack still works: execute `startDevelopmentStack.sh` and execute `npm run testpipeline`
  and `npm run testcomponent` in dataland-frontend
- [ ] After(!) the cypress tests have passed locally, execute the backend-e2e-tests `./gradlew dataland-e2etests:test`
- [ ] Locally: Go to the swagger-UI, authorize, run a "GET" request to the companies endpoint and assure that your
  authorization has worked by assuring that you get a 200 response
- [ ] Merge using Squash Commit. The Merge Commit Message needs to contain "Manual Maintenance"
- [ ] After merge check SonarQube state of main branch at https://sonarcloud.io/summary/new_code?id=d-fine_Dataland. 
  The full scan might reveal new issues (e.g. deprecation) on old code which is generally not detected on the branch.
