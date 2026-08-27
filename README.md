# Dataland
Dataland is a platform to distribute ESG data. This repository contains the code for the Dataland Platform.

# License
This project is free and open-source software licensed under the [GNU Affero General Public License v3](LICENSE)
(AGPL-3.0). Commercial use of this software is allowed. If derivative works are distributed, you need to publish
the derivative work under the same license. Here, derivative works include web publications. That means, if you build
a web service using this software, you need to publish your source code under the same license (AGPL-3.0)

In case this does not work for you, please contact dataland@d-fine.de for individual license agreements.

# Contributions
Contributions are highly welcome. Please refer to our [contribution guideline](contribution/contribution.md).
To allow for individual licenses and eventual future license changes, we require a contributor license agreement from
any contributor that allows us to re-license the software including the contribution.

# Quick Start Guide
Follow these steps to set up the dataland development stack on your computer.
1. Install Java (>= 21), Node.JS (>=24), and docker.
2. In your environment variables: Set `JAVA_HOME` to your java installation path and make sure java is in your `PATH` (On Windows, add `%JAVA_HOME%/bin` to `PATH`).
3. Add a link for `local-dev.dataland.com` and `dataland-admin` to `127.0.0.1` in the Hosts file (On Windows: `%windir%\system32\drivers\etc\hosts`, On Linux: `/etc/hosts`).
4. (If on Windows): Enable long paths in git (`git config --global core.longpaths true`) and [in Windows](https://learn.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation?tabs=registry).
5. Clone this repository to your computer.
6. Start the development stack by running `manageLocalStack.sh --reset --start`. You may omit the `--reset` on subsequent starts. Especially the first start can take a long time, make sure that Docker is running in the background. Note: on Windows run this command in bash terminal e.g. GitBash.
7. After the stack has booted, you may go to `https://local-dev.dataland.com` in your browser and experience dataland. You can login with the default credentials `data_admin:password`.
8. You can stop the development stack by running `manageLocalStack.sh --stop`.

# Developer Remarks
In this section, you find information that might be useful for you as a developer.

## Add scripts to git with the executable flag
Especially under Windows, it's unclear which file permissions a script will get. 
To explicitly mark a script executable, do:
`git update-index --chmod=+x script.sh`

## Git Hooks
To add the provided git pre-hooks to your local development environment execute:
`git config --local core.hookspath ./.githooks/`

## Environment Variables for Development
Environment variables for local development are defined in `environments/.env.dev`. CI loads `.env.dev` together
with `environments/.env.ci` for CI-specific overrides. Add new variables to `.env.dev` and, if they differ in CI,
to `.env.ci`.

## API Documentation
Links to the interactive swagger API documentation are available on all running instances of dataland 
(e.g. [test](https://test.dataland.com)) in the footer. For example, the swagger UI of the test instance backend is 
located [here](https://test.dataland.com/api/swagger-ui/index.html). Requests can be authorized via two different methods:
- Option A: Manually obtain a bearer token from KeyCloak and enter it in the `default-bearer-auth` field.
- Option B: Automatically obtain a bearer token from KeyCloak by entering `dataland-public` for `client_id` and 
  leaving `client_secret` empty (in the `default-oauth` section). Swagger will then redirect you to the KeyCloak Login
  form for authentication.

## Run Cypress Tests locally
* start the local stack using "manageLocalStack.sh --start". Set the env-variables (see above). 
* The backend will be started automatically. You can kill it and run it from the IDE if you like (e.g. for Debugging)
* Either use cypress while watching the browser:
  * start the cypress UI by using `npm run cypress`
  * Select `E2E Testing` or `Component Testing` and run the tests
* Or use cypress without visible browser (more robust):
  * run `npm run testpipeline -- --env EXECUTION_ENVIRONMENT=""` 

## Reclaiming WSL2 Disk Space
If you develop inside WSL2 (Windows), disk usage on your `C:` drive can keep growing over time even after you
delete files or Docker data inside WSL. This is because WSL2's virtual disks (`.vhdx` files) grow automatically but
do not always shrink automatically.

* Run `./manageLocalStack.sh --prune-docker` to free up Docker's disk usage (containers, images, unused volumes,
  and build cache) inside WSL. This does not touch persisted devcontainer data (e.g. opencode session/state
  volumes). On modern WSL versions, distro `.vhdx` files are often created as sparse NTFS files, meaning Windows
  automatically reclaims the freed space without any further action from you (you can check this yourself: right
  click the `.vhdx` file in Explorer and compare "Size" vs "Size on disk" - if "Size on disk" is meaningfully
  smaller, your disk is already sparse and self-managing).
* If your `.vhdx` is *not* sparse, `--prune-docker` alone will not shrink it on your `C:` drive. To reclaim that
  space, shut down WSL (`wsl --shutdown` in PowerShell) and compact the relevant `.vhdx` files via `diskpart` (see
  [Microsoft's guide on managing WSL disk space](https://learn.microsoft.com/en-us/windows/wsl/disk-space)). If you
  attempt to compact a vhdx that turns out to already be sparse, `diskpart` will refuse with an error stating the
  file "must not be sparse" - that's expected and simply means compaction isn't needed for that disk.
* If you use (or used to use) Docker Desktop with the WSL2 backend, note that it stores all images/containers/
  volumes/build cache in its own separate virtual disk (typically `%LOCALAPPDATA%\Docker\wsl\disk\docker_data.vhdx`),
  not in your regular WSL distro's disk. Compacting this disk is often ineffective unless you also trim the
  filesystem inside Docker Desktop's own WSL distro first (e.g. `wsl -d docker-desktop -u root fstrim -av`). If you
  have since uninstalled Docker Desktop but still find a `docker-desktop`/`docker-desktop-data` entry in
  `wsl --list --all -v` along with a leftover `docker_data.vhdx`, this is usually just an orphaned leftover that
  Docker Desktop's uninstaller failed to clean up - in that case, just unregister it
  (`wsl --unregister docker-desktop`) and delete the leftover `.vhdx` file manually to reclaim the space instantly,
  rather than trying to compact it.
* We deliberately do not recommend WSL's experimental `sparseVhd` setting (`.wslconfig`, `[experimental]` section)
  as a way to convert an *existing* distro: as of WSL 2.7.12.0, doing so requires
  `wsl --manage <Distro> --set-sparse true --allow-unsafe`, and WSL itself warns that sparse VHD support is
  disabled by default "due to potential data corruption". Given the risk to real development data, prefer the
  manual `diskpart` compaction approach above instead, or rely on newer distros being sparse by default.
* Also check `%TEMP%\wsl-crashes` (`$env:LOCALAPPDATA\Temp\wsl-crashes` in PowerShell) if your disk fills up
  unexpectedly. This folder holds crash dump files whenever a process inside WSL/a devcontainer crashes, and a
  single crash (e.g. a runaway Electron/Cypress process) can silently produce a dump file that is well over 100GB,
  unrelated to Docker or WSL disk growth entirely. It is safe to delete old dump files here once you no longer need
  them for debugging the crash that produced them.

## Licenses
This project makes use of open source dependencies. To see a list of gradle dependencies along with their 
licenses, run `./gradlew generateLicenseReport` 
