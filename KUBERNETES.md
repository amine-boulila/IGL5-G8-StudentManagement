# Deploy to Docker Desktop Kubernetes

These manifests deploy the application and a separate MySQL database into the
local Docker Desktop Kubernetes cluster. The Kubernetes database uses its own
persistent volume; it does not share the Docker Compose database.

## Prerequisites

- Docker Desktop Kubernetes is running and `kubectl` is configured to use the
  `docker-desktop` context.
- The project Docker image has been built locally:

  ```powershell
  docker compose build app
  ```

- A local `.env` exists with `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`,
  and `MYSQL_ROOT_PASSWORD`. `MYSQL_USER` must not be `root`.

## Create credentials and deploy

From the project root in PowerShell, create the namespace and Kubernetes
Secret directly from the ignored local `.env` file. The generated Secret YAML
is piped directly to Kubernetes and is not printed or saved in the repository:

```powershell
kubectl create namespace student-management --dry-run=client -o yaml | kubectl apply -f -
kubectl create secret generic mysql-credentials --namespace student-management --from-env-file=.env --dry-run=client -o yaml | kubectl apply -f -
```

Then apply the workload:

```powershell
kubectl apply -f k8s/student-management.yaml
kubectl rollout status statefulset/mysql --namespace student-management --timeout=5m
kubectl rollout status deployment/student-management --namespace student-management --timeout=5m
kubectl get pods,services,persistentvolumeclaims --namespace student-management
```

The app image uses `imagePullPolicy: Never` because it is expected to be
available in Docker Desktop's local image store. The MySQL StatefulSet stores
its data in a 2 GiB PVC. Keep the `.env` file private; Kubernetes Secrets are
not encrypted by default in a local cluster and this setup is for coursework,
not production.

## Validate

The service is exposed on NodePort 30089:

```powershell
Invoke-WebRequest -Uri "http://localhost:30089/student/students/getAllStudents" -UseBasicParsing
```

Swagger UI:
[http://localhost:30089/student/swagger-ui/index.html](http://localhost:30089/student/swagger-ui/index.html)

Useful troubleshooting commands:

```powershell
kubectl get pods --namespace student-management
kubectl describe pods --namespace student-management
kubectl logs deployment/student-management --namespace student-management
kubectl logs statefulset/mysql --namespace student-management
```

## Remove the Kubernetes deployment

This removes the namespace and its workloads, including the MySQL PVC and its
data:

```powershell
kubectl delete namespace student-management
```
