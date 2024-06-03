# Dataland
Dataland is a platform to distribute ESG data. This repository contains the code for the Dataland Platform.

# Getting Started  
Clone the repository and set environmental variables by executing `setEnvironmentVariables.ps1`. Some of the required 
variables are left blank and have to be set by the user locally since they contain sensitiv information. To start the 
application locally you can run `resetDevelopmentStack.sh` or `startDevelopmentStack.sh`. Please note that you will have
to use your own certificates if you do not have access to the dataland infrastructure. The corresponding parts in the
scripts have to be adapted manually. 

# License
This project is free and open-source software licensed under the [GNU Affero General Public License v3](LICENSE)
(AGPL-3.0). Commercial use of this software is allowed. If derivative works are distributed, you need to be published
the derivative work under the same license. Here, derivative works includes web publications. That means, if you build
a web service using this software, you need to publish your source code under the same license (AGPL-3.0)

In case this does not work for you, please contact dataland@d-fine.de for individual license agreements.

# Contributions
Contributions are highly welcome. Please refer to our [contribution guideline](contribution/contribution.md).
To allow for individual licenses and eventual future license changes, we require a contributor license agreement from
any contributor that allows us to re-license the software including the contribution.

# Developer Remarks
In this section, you find information that might be useful for you as developer.

## Add scripts to git with the executable flag
Especially under Windows, it's unclear which file permissions a script will get. 
To explicitly mark a script executable, do:
`git update-index --chmod=+x script.sh`

## Git Hooks
To add the provided git pre-hooks to your local development environment execute:
`git config --local core.hookspath ./.githooks/`

## Environment Variables
Environmental variables can be set via the script `setEnvironmentVariables.ps1`.

## Local HTTPS testing
* Add A link for `local-dev.dataland.com` to `127.0.0.1` in the Hosts file (On Windows: `%windir%\system32\drivers\etc\hosts`, On Linux: `/etc/hosts`)
* The `startDevelopmentStack.sh` script will automatically retrieve signed SSL-Certificates for this domain.
* Access the development stack at https://local-dev.dataland.com

## API Documentation
Links to the interactive swagger API documentation are available on all running instances of dataland 
(e.g. [test](https://test.dataland.com)) in the footer. For example, the swagger UI of the test instance backend is 
located [here](https://test.dataland.com/api/swagger-ui/index.html). Requests can be authorized via two different methods:
- Option A: Manually obtain a bearer token from KeyCloak and enter it in the `default-bearer-auth` field.
- Option B: Automatically obtain a bearer token from KeyCloak by entering `dataland-public` for `client_id` and 
  leaving `client_secret`empty (in the `default-oauth` section). Swagger will then redirect you two the KeyCloak Login
  form for authentication.

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