# Publish Maven artifacts to Nexus

The local Nexus instance stores Maven releases and snapshots. The project's
Maven coordinates are `tn.esprit:student-management`; this repository does not
produce the separate Timesheet artifact mentioned in the coursework brief.

## Start Nexus

From the project root:

```powershell
docker compose -f compose.nexus.yaml up -d
docker compose -f compose.nexus.yaml ps
```

Open [http://localhost:8081](http://localhost:8081). On a new volume, the
initial administrator password is in `/nexus-data/admin.password` inside the
container. Retrieve it locally without committing it:

```powershell
docker exec student-management-nexus cat /nexus-data/admin.password
```

Sign in as `admin`, change the initial password, and finish the setup wizard.
Keep the password private.

Nexus normally provides `maven-releases` and `maven-snapshots` hosted
repositories. Verify they are present under **Browse > Repositories**. If the
setup wizard did not create them, create Maven2 (hosted) repositories with
these exact names and set their version policy to **Release** and **Snapshot**
respectively.

Create a dedicated deployment user and role for Jenkins. Grant only the
repository privileges needed for deployment:

- `nx-repository-view-maven2-maven-snapshots-add`
- `nx-repository-view-maven2-maven-snapshots-edit`
- `nx-repository-view-maven2-maven-releases-add`
- `nx-repository-view-maven2-maven-releases-edit`

Do not use the initial administrator credential in Jenkins.

## Add the Jenkins credential

In **Manage Jenkins > Credentials**, add a credential accessible to the
pipeline job:

- Kind: **Username with password**
- Username/password: the dedicated Nexus deployment user
- ID: `nexus-maven-deploy`

The Jenkins pipeline creates a temporary Maven settings file, uses the
credential only during publication, and removes that file afterward. Do not
put Nexus credentials in the POM, Jenkinsfile, or repository.

## Publish and verify

After Nexus has completed its first startup and the `nexus-maven-deploy`
credential is available to Jenkins, run the pipeline. It performs build/tests,
SonarQube analysis, then executes Maven `deploy`. As the current POM version
ends in `-SNAPSHOT`, the artifact is published to
`maven-snapshots`.

Find it in Nexus at **Browse > maven-snapshots** under
`tn/esprit/student-management/0.0.1-SNAPSHOT/`. For a release, update the
project version to a non-`SNAPSHOT` version in a reviewed change; Maven then
uses `maven-releases`.

The repository is exposed on port 8081 for local coursework. Do not expose the
Nexus admin interface publicly; restrict network access and use strong
credentials.

## Stop

```powershell
docker compose -f compose.nexus.yaml down
```

The `nexus_data` volume is retained when stopping. Removing it deletes all
local Nexus users, settings, and published artifacts.
