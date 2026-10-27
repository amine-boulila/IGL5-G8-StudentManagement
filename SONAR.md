# SonarQube local setup

This setup runs SonarQube Community locally for project validation. It uses
SonarQube's bundled database and is intended for local coursework, not for a
production SonarQube server.

## Start SonarQube

From the repository root, start the service:

```powershell
docker compose -f compose.sonar.yaml up -d
docker compose -f compose.sonar.yaml ps
```

Open [http://localhost:9000](http://localhost:9000). On first login, use
`admin` for both the initial username and password, then immediately change
the password.

In SonarQube, create a user token from **My Account > Security > Generate
Tokens**. Revoke any token accidentally pasted into a file, terminal, or chat.
Copy the newly generated token to the clipboard, then run this block in
PowerShell. It reads the token from the clipboard without displaying it:

```powershell
$token = $null
try {
    $clipboardToken = Get-Clipboard -Raw
    if ($null -eq $clipboardToken) {
        throw "Clipboard is empty. Copy only the SonarQube token, then run this block again."
    }

    $token = $clipboardToken.Trim()
    if ([string]::IsNullOrWhiteSpace($token) -or $token -match '[\x00-\x1F\x7F]') {
        throw "Clipboard contains no valid token. Copy only the SonarQube token and try again."
    }

    $env:SONAR_TOKEN = $token
    $authHeaders = @{ Authorization = "Bearer $env:SONAR_TOKEN" }
    $auth = Invoke-RestMethod -Uri "http://localhost:9000/api/authentication/validate" -Headers $authHeaders
    if (-not $auth.valid) {
        throw "SonarQube rejected the token. Generate a new user token and try again."
    }
    Remove-Variable authHeaders, auth

    mvn clean verify sonar:sonar "-Dsonar.host.url=http://localhost:9000"
    if ($LASTEXITCODE -ne 0) {
        throw "SonarQube analysis failed with exit code $LASTEXITCODE"
    }
}
finally {
    Remove-Item Env:\SONAR_TOKEN -ErrorAction SilentlyContinue
    Remove-Variable token, clipboardToken -ErrorAction SilentlyContinue
}
```

This runs the tests, creates the JaCoCo XML report, and submits the analysis.
The report is also available locally at `target/site/jacoco/index.html`.
Review the project dashboard at [http://localhost:9000](http://localhost:9000).

## Stop SonarQube

```powershell
docker compose -f compose.sonar.yaml down
```

To also delete the local SonarQube database and stored settings, use
`docker compose -f compose.sonar.yaml down -v`.
