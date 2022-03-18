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
## add scripts executable
Especially under Windows, it's unclear which file permissions a script will get. 
To explicitly mark a script executable, do:
`git update-index --chmod=+x script.sh`