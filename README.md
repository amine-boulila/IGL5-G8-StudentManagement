# IGL5-G8-StudentManagement

docker build -t jenkins-docker ./jenkins

docker run -d --name jenkins --user root --network devops-net -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock jenkins-docker

docker run -d --name sonarqube --network devops-net -p 9000:9000 -v sonarqube_data:/opt/sonarqube/data -v sonarqube_extensions:/opt/sonarqube/extensions -v sonarqube_logs:/opt/sonarqube/logs sonarqube:community

docker run -d --name nexus --network devops-net -p 8081:8081 -v nexus_data:/nexus-data sonatype/nexus3

# Monitoring (Prometheus + Grafana)

The application exposes its metrics on /student/actuator/prometheus (Spring Boot Actuator + Micrometer).

cd monitoring

docker compose -p studentmon up -d

Prometheus: http://localhost:9090 (Status > Targets, the student-management target must be UP)

Grafana: http://localhost:3000 (admin / admin), the Prometheus data source is already provisioned. Import dashboard 4701 (JVM Micrometer) to see the metrics.

docker compose -p studentmon down

# Automatic build on push (GitHub webhook + ngrok)

The `Jenkinsfile` declares `triggers { githubPush() }`, so Jenkins starts the pipeline when GitHub calls its webhook endpoint.
Jenkins runs on a local PC, so it is exposed to GitHub with ngrok.

ngrok http 8080

On GitHub (repository Settings > Webhooks > Add webhook):

- Payload URL: `https://<ngrok-domain>/github-webhook/`
- Content type: `application/json`
- Event: `Just the push event`

Every push to the branch built by the job now starts a build, which creates an EKS cluster: run the destroy job afterwards to protect the AWS lab budget.
