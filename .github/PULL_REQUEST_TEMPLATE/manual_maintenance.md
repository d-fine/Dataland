# Manual Maintenance Sprint [NR]

Note: To create a PR using this template add the query parameter `template=manual_maintenance.md` to the merge request
creation URL (or simply copy this md file into the description)

# Maintenance tasks (to be completed by the assignee)

## Dataland repository

### Dependency updates

- See 'Problematic Updates' in the wiki (evaluate if issues persist or if newer versions are available that fix them)

- [ ] Take a look at the dependency dashboard and see which updates are to be applied. Never press the "Create all rate-limited PRs at once" box. For a detailed description of the process,
  consult the internal Dataland Wiki.

- [ ] Update the wiki page (should display current issues why specific updates were not done)

### Dataland EuroDaT client (The EuroDaT section should be skipped)

- [ ] Check on the https://eurodat.gitlab.io/trustee-platform/release_notes/ if there is a newer version available, if yes
  then update the version number used in docker-compose.
- [ ] If the version was changed: Check if the eurodatClientOpenApi.json in dataland-external-storage is in sync with
  the currently used version of the client.
  1. Edit the docker compose file by adding a `ports` section with the entry `"8080:8080"` to the "eurodat-client" and start it
  2. Open a shell and navigate to the `dataland-eurodat-client` subproject
  3. Execute the following snippet of code (requires python): `curl http://localhost:8080/api/v1/client-controller/openapi | sed 's/\(example: \)\([^ ]*\)/\1"\2"/g' | python -c 'import sys, yaml, json; print(json.dumps(yaml.safe_load(sys.stdin.read()), indent=2))' > ./eurodatClientOpenApi.json`
  4. If there are changes to `eurodatClientOpenApi.json`, discuss with the team how to proceed

## Server maintenance

Note: Before applying any update to any server make sure that a backup exists for all dev servers, letsencrypt, clone and test. In case of prod, create a fresh backup just
before applying any changes and align with the team when to apply them.

For creating a backup please refer to the internal wiki.

On all servers to the following:
- Connect to the server (see internal wiki for details).
- Execute `sudo apt-get update && sudo apt-get upgrade` to update the server (if updates require a reboot it works better to start it manually with `sudo reboot` than from the opened message window. If a reboot is needed can be seem by a server message in the corresponding bash or terminal window.)
- Execute `sudo docker system prune -a` to clean up unused docker components and liberate disk space
- Check for new ubuntu releases. If there are new releases check with your team. If approved install them with `sudo do-release-upgrade` (see internal documentation for details, you might need to run `sudo apt update && sudo apt upgrade` first if packages are missing)

Start the process with one of the dev servers (preferably dev2 or dev3) and deploy to it afterwards and check if everything is working as intended.
Then update the remaining servers without deploying to them, since one deployment is enough for checking. If reboot is necessary, reboot after each update except for prod.
**Rebooting production should occur right before next roll out.**

List of all servers:
- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] dev3.dataland.com
- [ ] test.dataland.com
- [ ] clone.dataland.com
- [ ] letsencrypt.dataland.com
- [ ] dataland.com

## Cloud maintenance (OTC)

Check the cloud provider's dashboard for manually created backups and images. Delete them if they are not needed anymore.

- [ ] not needed images deleted (only latest relevant for clone)
- [ ] manually created backups deleted (older than one month)

## ssh-keys maintenance

- [ ] Make sure the ssh-keys file reflects the current team composition. Execute the update script as described in the
  internal wiki.

## Check RabbitMQ dead letter queue and disk space

- [ ] Clear up disk space on all servers by removing unused docker images.
- [ ] Connect to RabbitMQ on production and test (see internal wiki for instructions).
- [ ] No unexpected new messages should have been added to the dead letter queue since the last manual
  maintenance.

## Check the alerts and critical alerts for prod in the grafana dashboard

On the prod environment the grafana alerts should be investigated.
Go to the slack-alerts-channels and click on the link to the dashboard in one of the alerts (for instructions how to connect, consult the
internal wiki). Check the critical alerts and alerts on prod in the dashboard since the last manual maintenance
Time-boxed investigation (2h-3h) of errors:
- [ ] Are there any unresolved critical alerts for unhealthy containers? (That should not be the case and needs to be resolved immediately)
- [ ] Are there any unresolved critical alerts for internal server errors? (Should resolve automatically after 6h when the problem doesn't persist)
  Is there a persisting problem? Needs to be investigated.
- [ ] Are there a lot of alerts during the same time when no critical alert occurred? Those should be investigated.
- [ ] Investigate alerts (within last week/last two weeks) that happen often or with a specific pattern? Investigate

Discuss findings with the team.

## Check that the main branch has no sonar issues
- [ ] Go to the sonar report summary of the main branch and verify that there are no sonar findings. If there are sonar
  findings, either fix them directly or bring them up for discussion with the team.

## Conclusion

- [ ] The new version is deployed to a dev server using this branch
  - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
  - [ ] It's verified that everything seems to be working fine by manually using the website
- [ ] This template and the internal wiki page have been updated to reflect the latest state of tasks required and known issues with upgrades

# Review (to be completed by the reviewer)

- [ ] The GitHub Actions (including Sonarqube Gateway and Lint Checks) are green. This is enforced by GitHub.
- [ ] The changes have been peer-reviewed by someone who did not implement them
- [ ] The PR actually implements what is described above
- [ ] Documentation is updated as required
- [ ] The automated deployment is updated if required
- [ ] The new version is deployed to one of the dev servers using this branch
  - [ ] Run with setting `Reset non-user related Docker Volumes & Re-populate` turned on
  - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
  - [ ] It's verified that the CD run is green
  - [ ] It's verified that everything seems to be working fine by manually using the website
- [ ] The local Dev stack still works: execute `startDevelopmentStack.sh` and `npm run testcomponent` in dataland-frontend (a bunch of cypress frontend e2e tests fails locally without manually clicking away the cookie banner => meaningless to run testpipeline)
- [ ] After(!) the cypress tests have passed locally, execute the backend-e2e-tests `./gradlew dataland-e2etests:test`
- [ ] It is assured that deploying this feature branch over the current main does not break anything (depending on the actual changes this step may be skipped)
  - [ ] A fresh clone of dataland.com is generated (see Wiki page on "OTC" for details)
  - [ ] Deploy the feature branch to clone with `Reset non-user related Docker Volumes & Re-populate` turned off
  - [ ] Verify that the CD run is green
  - [ ] Verify that everything seems to be working fine by manually using the website
- [ ] Merge using Squash Commit. The Merge Commit Message needs to contain "Manual Maintenance"
- [ ] After merge check SonarQube state of main branch at https://sonarcloud.io/summary/new_code?id=d-fine_Dataland.
  The full scan might reveal new issues (e.g. deprecation) on old code which is generally not detected on the branch.
