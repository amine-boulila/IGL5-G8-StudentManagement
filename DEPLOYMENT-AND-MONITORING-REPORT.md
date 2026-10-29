# Deployment and Monitoring Report

## 1. Jenkins pipeline

The main [`Jenkinsfile`](Jenkinsfile) automates these steps:

1. Checkout the `feature/jenkins-pipeline` branch.
2. Run Maven clean, package the JAR, then run the unit tests and JaCoCo report.
3. Send the analysis to SonarQube.
4. Publish the JAR to Nexus.
5. Build the Docker image and optionally publish it to Docker Hub.
6. Check AWS credentials when infrastructure or Kubernetes deployment is selected.
7. Optionally apply Terraform infrastructure when `APPLY_INFRASTRUCTURE` is enabled.
8. Deploy the application when `DOCKERHUB_IMAGE` is provided.
9. In the separate `Monitoring` stage, apply `k8s/monitoring` and wait for Prometheus and Grafana.

The Kubernetes deployment and monitoring stages run when `DOCKERHUB_IMAGE` is
non-empty. Set it to the Docker Hub image name, for example
`yourusername/student-management`. `APPLY_INFRASTRUCTURE` defaults to `false`;
enable it only when the EKS infrastructure must be created or updated.

Jenkins must have the credentials referenced by the pipeline configured:

- `github-private-repo`
- `sonar-token`
- `nexus-credentials`
- `dockerhub-credentials`
- `aws-credentials`
- `aws-session-token`
- `student-management-db-password`

## 2. Refresh AWS CLI credentials and connect to EKS

The AWS Academy credentials are temporary. Start or resume the lab and use its
current access key, secret key, and session token. Do not commit or share them.
In PowerShell, clear any stale AWS environment credentials, configure the
default profile, and verify it:

```powershell
Remove-Item Env:AWS_ACCESS_KEY_ID,Env:AWS_SECRET_ACCESS_KEY,Env:AWS_SESSION_TOKEN -ErrorAction SilentlyContinue
aws configure --profile default
aws configure set aws_session_token "YOUR_FRESH_SESSION_TOKEN" --profile default
aws sts get-caller-identity --profile default
```

Enter the fresh access key and secret key when `aws configure` prompts for
them. Set the region to `us-east-1`. Once the identity check succeeds, configure
`kubectl` for EKS and inspect the deployment:

```powershell
aws eks update-kubeconfig --region us-east-1 --name student-management-eks --profile default
kubectl get nodes
kubectl get pods -n student-management
kubectl get services -n student-management
```

## 3. Access the application and generate metrics

The app Service is a Kubernetes `LoadBalancer` on port `8089`. Find its current
AWS hostname or IP:

```powershell
kubectl get service student-management -n student-management
```

Use the value in `EXTERNAL-IP` in the API URL. EKS commonly returns a load
balancer hostname rather than a numeric IP. A read-only GET request is:

```powershell
Invoke-RestMethod `
  -Uri "http://<EXTERNAL-IP>:8089/student/students/getAllStudents" `
  -Method Get
```

To create a sample student with a POST request:

```powershell
$body = @{
  firstName = "Ada"
  lastName = "Lovelace"
  email = "ada.lovelace@example.com"
  phone = "555-0100"
  dateOfBirth = "2000-01-01"
  address = "London"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://<EXTERNAL-IP>:8089/student/students/createStudent" `
  -ContentType "application/json" `
  -Body $body
```

The POST inserts a student record in MySQL. Both requests generate application
HTTP metrics; Prometheus scrapes the app's internal management endpoint every
10 seconds.

## 4. Open Grafana and Prometheus

Grafana and Prometheus use internal Kubernetes Services. Port-forward Grafana
from PowerShell and leave that terminal open:

```powershell
kubectl port-forward -n student-management service/grafana 3000:3000
```

Open <http://localhost:3000> in a browser and sign in with `admin` / `admin`.
Open the **Student Management** dashboard and refresh after about 10–20 seconds
for Prometheus to scrape recent request metrics.

To inspect Prometheus directly, run this in a second terminal and leave it open:

```powershell
kubectl port-forward -n student-management service/prometheus 9090:9090
```

Open <http://localhost:9090> and check **Status → Targets**. The
`student-management` target should show `UP`. The AWS load balancer for the app
exposes port `8089`; it does not expose Grafana on port `3000` or Prometheus on
port `9090`.

## 5. Remove the deployment

[`Jenkinsfile.destroy`](Jenkinsfile.destroy) requires the parameter
`CONFIRM_DESTROY` to equal `DESTROY`. It deletes the whole `student-management`
namespace, which removes the application, MySQL, Prometheus, Grafana, and their
Services, then runs Terraform destroy for the EKS infrastructure.

