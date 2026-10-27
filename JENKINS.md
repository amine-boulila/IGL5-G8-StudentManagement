# Jenkins pipeline setup

The pipeline in [Jenkinsfile](./Jenkinsfile) runs the Maven wrapper, publishes
JUnit test results, uploads the JaCoCo report through SonarQube analysis, and
archives the packaged JAR.

## Configure Jenkins

The local Jenkins controller is available at [http://localhost:8080](http://localhost:8080).
The pipeline uses the controller's Java 21, Bash, and Git installations and
downloads the project's pinned Maven version through `mvnw`. This is suitable
for this local coursework setup; use a separate build agent rather than the
controller for a shared or production Jenkins instance.

1. In Jenkins, open **Manage Jenkins > Credentials** and add a credential to
   the global store:
   - Kind: **Secret text**
   - Secret: a newly generated SonarQube user token
   - ID: `sonarqube-token`
2. Revoke and replace any token previously pasted into a file, terminal, or
   chat. Never put a token in the Jenkinsfile or Git.
3. Create a **Pipeline** job. Under **Pipeline**, choose **Pipeline script from
   SCM**, select **Git**, enter this repository's clone URL and the branch that
   contains the Jenkinsfile, then set **Script Path** to `Jenkinsfile`.
4. If the repository is private, configure a Jenkins Git credential with
   read-only access. Do not put credentials in the clone URL.
5. Save the job and select **Build Now**. Review the build log, JUnit test
   results, and archived JAR from the build page.

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
