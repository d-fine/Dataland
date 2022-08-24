# Manual Maintenance Sprint [NR]
# Maintenance tasks (to be completed by the assignee)
## Skyminder
- [ ] Complete manual maintenance in the Skyminder Repo
- [ ] Release a new version `x.x.x` (Please put new version number here)
## EDC
- [ ] Complete manual maintenance in the EDC Repo
- [ ] Release a new version `x.x.x` (Please put new version number here)
## Dataland
### Gradle update
- [ ] Execute `gradlew dependencyUpdates` to get a report on Dependencies with updates
- [ ] Update `settings.gradle.kts` (for libraries), `build.gradle.kts` (for plugins) and `gradle.properties` (for jacoco and ktlint)
Note: fasterXML is managed by spring and ktlint by jlleitschuh, thus NO manual version update should be conducted
- [ ] update the gradle wrapper: execute `gradle wrapper --gradle-version X.Y.Z`

### Dataland frontend
- [ ] Update node version in `dataland-frontend/build.gradle.kts`
- [ ] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in package.json
- [ ]   Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check for security issues
  (Known issues appeared in the past with updating Jest, openApiGenerator and Eslint).

### Dataland keycloak theme
- [ ] Update node version in `dataland-keycloak/dataland_theme/login/build.gradle.kts`
- [ ] Update node packages: run the `updatepackages` script, e.g. by  `npm run updatepackages` to update versions in package.json
- [ ]   Run the `updatepackagelock`, e.g. by  `npm run updatepackagelock` script to update `package-lock.json` and check for security issues
  (Known issues appeared in the past with updating Jest, openApiGenerator and Eslint).

### Dockerfile updates
Update versions in the following dockerfiles
- [ ] `./baseDockerfiles/cypressBaseImageDockerfile`
  - [ ] On any change run the corresponding job in GitHub
- [ ] `./baseDockerfiles/temurinBaseImageDockerfile`
  - [ ] On any change run the corresponding job in GitHub
- [ ] `./dataland-backend/Dockerfile`
- [ ] `./dataland-keycloak/Dockerfile`
- [ ] Update versions in the `docker-compose.yml` file

## Server updates
Execute `sudo apt-get update && sudo apt-get upgrade` on
- [ ] dev-dataland.duckdns.org
- [ ] dataland-tunnel.duckdns.org
- [ ] dataland-letsencrypt.duckdns.org
- [ ] (OPT) dataland.com

## Conclusion
- [ ] After updating all components check if everything still works
  - [ ] Document any conflicts and skipped update by placing comments on this PR
- [ ] Execute a deployment with real data

# Review (to be completed by the reviewer)
- [x] The Github Actions (including Sonarqube Gateway and Lint Checks) are green. This is enforced by Github.
- [ ] A peer-review has been executed
  - [ ] The code has been manually inspected by someone who did not implement the feature
- [ ] The PR actually implements what is described above
- [ ] Documentation is updated as required
- [ ] The automated deployment is updated if required
- [ ] The new version is deployed to the dev server using this branch
  - [ ] It's verified that this version actually is the one deployed (check actuator/info for branch name and commit id!)
  - [ ] It's verified that everything seems to be working fine by manually using the website
  - [ ] All implemented Social Logins have been tested manually in the UI
- [ ] The new version is deployed to the dev server using this branch and real data
  - [ ] It's verified that this version actually is the one deployed (check actuator/info for branch name and commit id!)
  - [ ] It's verified that real data has been used
  - [ ] It's verified that everything seems to be working fine by manually using the website
  - [ ] All implemented Social Logins have been tested manually in the UI
- [ ] If any work on the UI is to be merged, those changes were also documented in the Figma
- [ ] The local Dev stack still works: execute `startDevelopmentStack.sh`, npm serve, npm teste2e and execute Cypress Tests locally

