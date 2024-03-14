# Manual Maintenance Sprint [NR]

Note: To create a PR using this template add the query parameter `template=manual_maintenance.md` to the merge request
creation URL (or simply copy this md file into the description)

# Maintenance tasks (to be completed by the assignee)

## Dataland

### Problematic updates

See the list of known issue on the internal wiki https://github.com/d-fine/DatalandInternal/wiki/Problematic-updates
Being present on this list does not mean that we simply skip the update, instead we are just aware that it may cause a problem
If an issue arises from a new update that cannot be solved in the scope of MM, add it to the wiki page and create a ticket in the backlog 

### Gradle update

- [ ] Execute `gradlew dependencyUpdates` to get a report on Dependencies with updates
- [ ] Execute `refreshVersions` in Gradle tasks or `gradlew refreshVersions` to generate version suggestions in `versions.properties`
- [ ] Update versions in `versions.properties`
- [ ] Update the gradle wrapper: execute `gradlew wrapper --gradle-version X.Y.Z`

### Dataland frontend

- [ ] Update node version in `dataland-frontend/build.gradle.kts`
- [ ] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in
  `package.json`
- [ ] Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check
  for security issues

### Dataland keycloak theme

- [ ] Update node version in `dataland-keycloak/dataland_theme/login/build.gradle.kts`
- [ ] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in
  `package.json`
- [ ] Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check
  for security issues

### Dataland automated QA service

- [ ] Update package versions in `dataland-automated-qa-service/requirements.txt`

### Dockerfile updates

Update versions in the following dockerfiles

- [ ] `./dataland-api-key-manager/Dockerfile`
- [ ] `./dataland-api-key-manager/DockerfileBase`
- [ ] `./dataland-api-key-manager/DockerfileTest`
- [ ] `./dataland-automated-qa-service/Dockerfile`
- [ ] `./dataland-automated-qa-service/DockerfileBase`
- [ ] `./dataland-automated-qa-service/DockerfileTest`
- [ ] `./dataland-backend/Dockerfile`
- [ ] `./dataland-backend/DockerfileBase`
- [ ] `./dataland-backend/DockerfileTest`
- [ ] `./dataland-batch-manager/Dockerfile`
- [ ] `./dataland-batch-manager/DockerfileBase`
- [ ] `./dataland-community-manager/Dockerfile`
- [ ] `./dataland-community-manager/DockerfileBase`
- [ ] `./dataland-community-manager/DockerfileTest`
- [ ] `./dataland-document-manager/Dockerfile`
- [ ] `./dataland-document-manager/DockerfileBase`
- [ ] `./dataland-document-manager/DockerfileTest`
- [ ] `./dataland-e2etests/Dockerfile`
- [ ] `./dataland-e2etests/DockerfileBase`
- [ ] `./dataland-frontend/Dockerfile`
- [ ] `./dataland-frontend/DockerfileTest`
- [ ] `./dataland-framework-toolbox/excel-to-csv/Dockerfile`
- [ ] `./dataland-inbound-admin-proxy/Dockerfile`
- [ ] `./dataland-inbound-proxy/Dockerfile`
- [ ] `./dataland-internal-storage/Dockerfile`
- [ ] `./dataland-internal-storage/DockerfileBase`
- [ ] `./dataland-internal-storage/DockerfileTest`
- [ ] `./dataland-keycloak/Dockerfile`  (also update realm json files with new version)
- [ ] `./dataland-pgadmin/Dockerfile`
- [ ] `./dataland-qa-service/Dockerfile`
- [ ] `./dataland-qa-service/DockerfileBase`
- [ ] `./dataland-qa-service/DockerfileTest`
- [ ] `./dataland-rabbitmq/Dockerfile`
- [ ] `./base-dockerfiles/DockerfileGradle`
- [ ] Update the versions of the external images for api-key-manager-db, backend-db, keycloak-db, internal-storage-db,
  document-manager-db, qa-service-db, community-manager-db and frontend-dev in `./docker-compose.yml`
- [ ] Check if there are any services in the `docker-compose.yml` file that have not gotten an update yet (e.g. a new
  service that is not covered by the tasks above)

## Server updates

Note: First manually create backups of the servers with a short retention duration.
Then start the update with one of the dev servers and deploy to it after the update.
If everything was fine, proceed with other servers. Double check for Prod.

Note: currently there seems to be an issue with the docker-compose plugin

Execute `sudo apt-get update && sudo apt-get upgrade` on

- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] dev3.dataland.com
- [ ] test.dataland.com
- [ ] letsencrypt.dataland.com
- [ ] (OPT) dataland.com

### ssh-keys maintenance

check that all ssh-keys are set and erased from people that have left

- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] dev3.dataland.com
- [ ] test.dataland.com
- [ ] clone.dataland.com
- [ ] letsencrypt.dataland.com
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

- [ ] Send an invitation (data) request from one of the dev servers and check if the e-mail to dataland@d-fine.com
  contains the right attachments and is displayed correctly.

## Check RabbitMQ dead letter queue and disk space

- [ ] RabbitMQ does need at least 768MB of free disk space to operate. `ssh` into all servers and check the available
  disk space with `df -h` command. If the open disk space is close to the minimum requirement, clear up disk space
  with `sudo docker image prune --all`.
- [ ] On all environments, no new messages should have been added to the dead letter queue since the last manual
  maintenance. If new messages have appeared this needs to be investigated. The dead letter queue can be accessed
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
    - [ ] All implemented Social Logins have been tested manually in the UI
- [ ] This template and the internal wiki page have been updated to reflect the latest state of tasks required and known issues with upgrades
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
  - [ ] The "mailto:*" buttons ("I am interested" and "Get in touch") on the landing page work.
  - [ ] Go to the swagger-UI, authorize, run a "GET" request to the companies endpoint and assure that your
    authorization has worked by assuring that you get a 200 response
- [ ] If any work on the UI is to be merged, those changes were also documented in the Figma
- [ ] The local Dev stack still works: execute `startDevelopmentStack.sh` and `npm run testcomponent` in dataland-frontend (a bunch of cypress frontend e2e tests fails locally without manually clicking away the cookie banner => meaningless to run testpipeline)
- [ ] After(!) the cypress tests have passed locally, execute the backend-e2e-tests `./gradlew dataland-e2etests:test`
- [ ] Locally: Go to the swagger-UI, authorize, run a "GET" request to the companies endpoint and assure that your
  authorization has worked by assuring that you get a 200 response
- [ ] It is assured that deploying this feature branch over the current main does not break anything
  - [ ] A fresh clone of dataland.com is generated (see Wiki page on "OTC" for details)
  - [ ] Deploy the feature branch to clone with `Reset non-user related Docker Volumes & Re-populate` turned off
  - [ ] Verify that the CD run is green
  - [ ] Verify that everything seems to be working fine by manually using the website
- [ ] Merge using Squash Commit. The Merge Commit Message needs to contain "Manual Maintenance"
- [ ] After merge check SonarQube state of main branch at https://sonarcloud.io/summary/new_code?id=d-fine_Dataland. 
  The full scan might reveal new issues (e.g. deprecation) on old code which is generally not detected on the branch.
