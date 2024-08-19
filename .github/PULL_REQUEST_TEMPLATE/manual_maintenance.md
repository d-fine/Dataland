# Manual Maintenance Sprint [NR]

Note: To create a PR using this template add the query parameter `template=manual_maintenance.md` to the merge request
creation URL (or simply copy this md file into the description)

# Maintenance tasks (to be completed by the assignee)

## Dataland repository

### Dependency updates

- [ ] Take a look at the dependency dashboard and see which updates are to be applied. For a detailed description of the process,
consult the internal Dataland Wiki.

### Dataland EuroDaT client

- [ ] Check on the https://eurodat.gitlab.io/trustee-platform/release_notes/ if there is a newer version available, if yes
  then update the version number used in docker-compose.
- [ ] If the version was changed: Check if the eurodatClientOpenApi.json in dataland-external-storage is in sync with 
  the currently used version of the client. 
  1. Edit the docker compose file by adding a `ports` section with the entry `"8080:8080"` to the "eurodat-client" and start it
  2. Open a shell and navigate to the `dataland-eurodat-client` subproject 
  3. Execute the following snippet of code (requires python): `curl http://localhost:8080/api/v1/client-controller/openapi | sed 's/\(example: \)\([^ ]*\)/\1"\2"/g' | python -c 'import sys, yaml, json; print(json.dumps(yaml.safe_load(sys.stdin.read()), indent=2))' > ./eurodatClientOpenApi.json`
  4. If there are changes to `eurodatClientOpenApi.json`, discuss with the team how to proceed

## Server updates

Note: Before applying any update to any server make sure that a backup exists. In case of prod, create a fresh backup just
before applying any changes and align with the team when to apply them.

Start the update with one of the dev servers (preferably dev2 or dev3) and deploy to it afterwards. If everything was
fine, proceed with other servers.

Execute `sudo apt-get update && sudo apt-get upgrade` on

- [ ] dev1.dataland.com
- [ ] dev2.dataland.com
- [ ] dev3.dataland.com
- [ ] test.dataland.com
- [ ] letsencrypt.dataland.com
- [ ] dataland.com (align beforehand)

If the updates require a reboot (for e.g. a kernel update), you can restart the machine with `sudo reboot`.
However, for dataland.com, you may want to avoid any interruption and schedule the reboot at the night with `sudo shutdown -r 02:00`.

## ssh-keys maintenance

- [ ] Make sure the ssh-keys file reflects the current team composition. Execute the update script as described in the 
  internal wiki.

## Check RabbitMQ dead letter queue and disk space

- [ ] RabbitMQ does need at least 768MB of free disk space to operate. `ssh` into all servers and check the available
  disk space with `df -h` command. If the open disk space is close to the minimum requirement, clear up disk space
  with `sudo docker image prune --all`.
- [ ] On all environments, no new messages should have been added to the dead letter queue since the last manual
  maintenance. If new messages have appeared this needs to be investigated. The dead letter queue can be accessed
  and messages on it read in the RabbitMQ GUI. Access it by port-forwarding port `6789` from the server and then
  accessing the GUI at `localhost:6789/rabbitmq`. After login, the dead letter queue can be found at Queues &rarr;
  deadLetterQueue &rarr; Get message.

## Check that the main branch has no sonar issues
- [ ] Go to the sonar report summary of the main branch and verify that there are no sonar findings. If there are sonar 
  findings, either fix them directly or bring them up for discussion with the team.

## Conclusion

- [ ] The new version is deployed to a dev server using this branch
    - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
    - [ ] It's verified that everything seems to be working fine by manually using the website
- [ ] This template and the internal wiki page have been updated to reflect the latest state of tasks required and known issues with upgrades
- [ ] The Merge Request commit message needs to contain 'manual maintenance' to satisfy the CI maintenance check in
  future commits

# Review (to be completed by the reviewer)

- [ ] The GitHub Actions (including Sonarqube Gateway and Lint Checks) are green. This is enforced by GitHub.
- [ ] The changes have been peer-reviewed by someone who did not implement them
- [ ] The PR actually implements what is described above
- [ ] Documentation is updated as required
- [ ] The automated deployment is updated if required
- [ ] The new version is deployed to the dev server "dev1" using this branch
  - [ ] Run with setting `Reset non-user related Docker Volumes & Re-populate` turned on
  - [ ] It's verified that this version actually is the one deployed (check gitinfo for branch name and commit id!)
  - [ ] It's verified that the CD run is green
  - [ ] It's verified that everything seems to be working fine by manually using the website
- [ ] The local Dev stack still works: execute `startDevelopmentStack.sh` and `npm run testcomponent` in dataland-frontend (a bunch of cypress frontend e2e tests fails locally without manually clicking away the cookie banner => meaningless to run testpipeline)
- [ ] After(!) the cypress tests have passed locally, execute the backend-e2e-tests `./gradlew dataland-e2etests:test`
- [ ] It is assured that deploying this feature branch over the current main does not break anything
  - [ ] A fresh clone of dataland.com is generated (see Wiki page on "OTC" for details)
  - [ ] Deploy the feature branch to clone with `Reset non-user related Docker Volumes & Re-populate` turned off
  - [ ] Verify that the CD run is green
  - [ ] Verify that everything seems to be working fine by manually using the website
- [ ] Merge using Squash Commit. The Merge Commit Message needs to contain "Manual Maintenance"
- [ ] After merge check SonarQube state of main branch at https://sonarcloud.io/summary/new_code?id=d-fine_Dataland. 
  The full scan might reveal new issues (e.g. deprecation) on old code which is generally not detected on the branch.
