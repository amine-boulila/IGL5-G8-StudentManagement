# Jenkins pipeline setup

The pipeline in [Jenkinsfile](./Jenkinsfile) checks out the private GitHub
repository with the Jenkins credential `github-repo-read`, runs the Maven
wrapper, publishes JUnit test results, uploads the JaCoCo report through
SonarQube analysis, and archives the packaged JAR.

## Configure Jenkins

The local Jenkins controller is available at [http://localhost:8080](http://localhost:8080).
The pipeline uses the controller's Java 21, Bash, and Git installations and
downloads the project's pinned Maven version through `mvnw`. This is suitable
for this local coursework setup; use a separate build agent rather than the
controller for a shared or production Jenkins instance.

1. In Jenkins, open **Manage Jenkins > Credentials** and add a SonarQube
   credential to the global store:
   - Kind: **Secret text**
   - Secret: a newly generated SonarQube user token
   - ID: `sonarqube-token`
2. Revoke and replace any token previously pasted into a file, terminal, or
   chat. Never put a token in the Jenkinsfile or Git.
3. If the GitHub repository is private, add a separate repository credential
   to the Jenkins global store. You do not need to own the repository, but
   your GitHub account must already have access to it:
   - Create a fine-grained personal access token for the repository owner's
     account or organization, select this repository, and grant
     **Contents: Read-only** permission. The token can only use repositories
     you can already access; it does not grant contributor access by itself.
   - If GitHub requires organization approval for the token, ask the
     repository or organization owner to approve it. If you cannot create or
     authorize such a token, ask the owner to configure a read-only deploy key
     or another repository credential for Jenkins.
   - In Jenkins, choose **Username with password**. Use your GitHub username
     as the username and the GitHub token as the password. Set the ID to
     `github-repo-read`.
   - Do not select the SonarQube `Secret text` credential for Git checkout,
     and never put a token in the repository URL.
4. Create a **Pipeline** job. Under **Pipeline**, choose **Pipeline script from
   SCM**, select **Git**, enter this repository's clone URL and the branch that
   contains the Jenkinsfile, select `github-repo-read` in **Credentials**, and
   set **Script Path** to `Jenkinsfile`. Jenkins needs this initial SCM
   connection to load the Jenkinsfile; the pipeline also uses the same
   credential for its explicit checkout.
5. Save the job, select **Build with Parameters**, and enter the branch to
   build in `GITHUB_BRANCH` (default: `feature/ahmed-student-management`).
   Review the build log, JUnit test results, and archived JAR from the build
   page.

If GitHub reports **Invalid username or token**, check that the selected
credential is the GitHub **Username with password** credential, that the
username is your GitHub username, and that the token has read access to this
repository and has not expired or been revoked. Confirm your collaborator
access is still active and ask the repository owner or organization admin
whether the token needs approval. Do not paste the token into chat or build
logs. For a public repository, leave **Credentials** as **none** instead of
supplying a token.

The Jenkins container must be able to reach SonarQube at
`http://host.docker.internal:9000`. This route was verified from the current
Jenkins container. The pipeline uses a Jenkins secret-text credential and
passes the token through the `SONAR_TOKEN` environment variable; it does not
print or store the token in the workspace.

## Automatic builds from GitHub

Configure a GitHub webhook after the manual pipeline succeeds. Jenkins must
have a publicly reachable webhook URL; if using ngrok for a class demo, update
the GitHub webhook whenever the ngrok URL changes. Do not depend on a
temporary URL for validation day. Until the webhook is configured, start
builds with **Build Now**.
