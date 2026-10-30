# Jenkins pipeline setup

The [Jenkinsfile](./Jenkinsfile) stages run in this order: checkout, Maven
clean, artifact construction, unit tests, SonarQube, Nexus publication, Docker
image build, Docker Hub push, AWS credential test, optional Terraform
provisioning, kubeconfig update, and Kubernetes deployment.
The wrapper is invoked as `bash ./mvnw` so it resolves `.mvn/wrapper` from the
workspace root in Jenkins.
By default, the pipeline provisions a separate EKS cluster named
`student-management` with Terraform and deploys to it. The manually created
`amazing-hiphop-rainbow` cluster is not changed. Before building, set
`AWS_CLUSTER_ADMIN_CIDR` to your current public IPv4 CIDR (for example
`203.0.113.25/32`). Terraform automatically applies its saved plan and creates
billable resources. Disable `PROVISION_EKS_WITH_TERRAFORM` to deploy to an
already-existing cluster instead. See [AWS.md](./AWS.md) for setup and cost
notes.

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
2. Add a separate **Username with password** credential for the dedicated
   Nexus deployment user:
   - ID: `nexus-maven-deploy`
   - The account must have permission to publish to the `maven-snapshots`
     and `maven-releases` repositories. See [NEXUS.md](./NEXUS.md) for Nexus
     setup and the required Nexus privileges.
3. Add a **Username with password** credential for Docker Hub:
   - ID: `dockerhub-credentials`
   - Use a Docker Hub access token as the password.
   - Create a public Docker Hub repository named `student-management`.
4. Add a **Secret file** credential for AWS:
   - ID: `awscredentials`
   - File format: AWS shared credentials INI with a `[default]` profile.
   - Keep access keys and session tokens private; do not print the file.
5. Add a **Secret file** credential for the AWS database:
   - ID: `aws-mysql-env`
   - The file must contain `MYSQL_DATABASE`, `MYSQL_USER`,
     `MYSQL_PASSWORD`, and `MYSQL_ROOT_PASSWORD` as `KEY=value` lines.
   - Use a non-root `MYSQL_USER`; never put the file in Git or pipeline logs.
6. Revoke and replace any token previously pasted into a file, terminal, or
   chat. Never put a token in the Jenkinsfile or Git.
7. Add a GitHub **Username with password** credential to the Jenkins global
   store. The GitHub error in the checkout log indicates Jenkins needs
   authentication. You do not need to own the repository, but
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
8. In the **Pipeline script from SCM** job configuration, choose **Git**, set
   the repository URL and branch to `feature/jenkins-pipeline`, and select
   `github-repo-read` for **Credentials**. Set **Script Path** to `Jenkinsfile`.
   The Jenkinsfile also uses this credential ID for its explicit checkout
   stage, so the ID must exist in the Jenkins global store.
9. Save the job, select **Build Now**, and review the build log, JUnit test
   results, and archived JAR from the build page.

If GitHub reports **Invalid username or token**, check that the selected
credential is the GitHub **Username with password** credential, that the
username is your GitHub username, and that the token has read access to this
repository and has not expired or been revoked. Confirm your collaborator
access is still active and ask the repository owner or organization admin
whether the token needs approval. Do not paste the token into chat or build
logs. If a valid credential exists but Jenkins still reports that it cannot
find it, verify the credential is stored in this Jenkins controller's global
store and that the ID matches exactly.

The Jenkins container must be able to reach SonarQube at
`http://host.docker.internal:9000`. This route was verified from the current
Jenkins container. It must also be able to reach Nexus at
`http://host.docker.internal:8081`. The pipeline uses Jenkins credentials for
SonarQube and Nexus and does not store them in the repository. Maven settings
for Nexus are created as a temporary, permission-restricted file and removed
after deployment.

## Docker-enabled Jenkins controller

The Docker build/push stages require Docker CLI and a Docker socket accessible
to the Jenkins user. The current controller was started with `docker run` and
does not have either configured. [Dockerfile.jenkins](./Dockerfile.jenkins)
adds Docker CLI 27.5.1 and Buildx from the official `docker:27.5.1-cli`
image, plus AWS CLI. Mounting the Docker socket grants Jenkins control
equivalent to root on the Docker host. Only do this for a trusted, isolated
coursework Jenkins controller; do not expose this Jenkins instance or permit
untrusted jobs/users.

To rebuild the controller while preserving its existing Jenkins home volume,
run these commands from the repository root in PowerShell. They stop and
replace the container but do not delete the named volume. Check that the
volume and socket group ID are non-empty before continuing:

```powershell
$jenkinsVolume = docker inspect jenkins --format '{{range .Mounts}}{{if eq .Destination "/var/jenkins_home"}}{{.Name}}{{end}}{{end}}'
$dockerGroupId = docker run --rm -v /var/run/docker.sock:/var/run/docker.sock alpine:3.21 stat -c '%g' /var/run/docker.sock
if ([string]::IsNullOrWhiteSpace($jenkinsVolume) -or [string]::IsNullOrWhiteSpace($dockerGroupId)) {
    throw "Could not resolve the Jenkins home volume or Docker socket group."
}
docker build -f Dockerfile.jenkins -t student-management-jenkins-docker:lts .
if ($LASTEXITCODE -ne 0) { throw "Jenkins image build failed." }
docker stop jenkins
if ($LASTEXITCODE -ne 0) { throw "Could not stop Jenkins." }
docker rm jenkins
if ($LASTEXITCODE -ne 0) { throw "Could not replace the Jenkins container." }
docker run -d --name jenkins `
  --group-add $dockerGroupId `
  -p 8080:8080 -p 50000:50000 `
  -v "${jenkinsVolume}:/var/jenkins_home" `
  -v /var/run/docker.sock:/var/run/docker.sock `
  student-management-jenkins-docker:lts
```

Do not add `-v` to `docker rm`; that would delete the persistent Jenkins
volume. After Jenkins starts, verify `docker --version`, `docker info`,
`aws --version`, and the saved Terraform binary from a Pipeline stage.

The AWS stages use Secret file credential `awscredentials` as the AWS shared
credentials file and Terraform from
`/var/jenkins_home/tools/terraform/1.17.0`. By default the pipeline provisions
the Terraform-defined EKS infrastructure in `us-east-1`, then deploys to
`EKS_CLUSTER_NAME` (`student-management` by default). Provisioning requires a
valid `AWS_CLUSTER_ADMIN_CIDR` and automatically applies the saved plan. The
AWS identity also needs EKS cluster access and Kubernetes permissions for
deployment. Terraform state is in the Jenkins workspace, which must remain
persistent and private. Never print credential files, access keys, session
tokens, database values, or kubeconfig contents.

## Automatic builds from GitHub

Configure a GitHub webhook after the manual pipeline succeeds. Jenkins must
have a publicly reachable webhook URL; if using ngrok for a class demo, update
the GitHub webhook whenever the ngrok URL changes. Do not depend on a
temporary URL for validation day. Until the webhook is configured, start
builds with **Build Now**.
