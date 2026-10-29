# Kubernetes deployment

Create a local secret from the example file without committing the real password:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic student-management-db `
  --from-literal=DB_PASSWORD=YOUR_MYSQL_PASSWORD `
  --namespace=student-management
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/mysql-service.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml
kubectl get pods -n student-management
kubectl get service student-management -n student-management
```

If the EBS CSI addon is unavailable, you can run a temporary demonstration
without persistent MySQL storage. Do not apply `mysql-pvc.yaml`; apply the
ephemeral deployment instead:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic student-management-db `
  --from-literal=DB_PASSWORD=YOUR_MYSQL_PASSWORD `
  --namespace=student-management
kubectl apply -f k8s/mysql-deployment-ephemeral.yaml
kubectl apply -f k8s/mysql-service.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml
```

The ephemeral deployment uses `emptyDir`. MySQL data is lost when its pod is
deleted or recreated. Use `mysql-deployment.yaml` and `mysql-pvc.yaml` again
after the EBS CSI addon is configured.

The application image is `maherkh/student-management:latest`. Update the image in
`k8s/app.yaml` when publishing under a different Docker Hub account.

On EKS, wait for AWS to assign an external hostname:

```powershell
kubectl get service student-management -n student-management --watch
```

Then open the `EXTERNAL-IP` hostname followed by `/student`.

## Prometheus and Grafana

After deploying the application, install the monitoring stack:

```powershell
kubectl apply -k k8s/monitoring
kubectl get pods -n student-management
kubectl port-forward service/grafana 3000:3000 -n student-management
```

Open <http://localhost:3000> and sign in with `admin` / `admin`. Grafana is
preconfigured with the Prometheus data source and Student Management dashboard.
Prometheus scrapes the app's internal management port at
`/actuator/prometheus`; Kubernetes liveness/readiness checks use
`/actuator/health`. The management port is separate from the public application
port and is exposed only through an internal Kubernetes service.

To inspect Prometheus directly, use a second terminal:

```powershell
kubectl port-forward service/prometheus 9090:9090 -n student-management
```

Both monitoring services are internal Kubernetes services. Their data uses
temporary storage in this demo setup, so Prometheus history and Grafana's local
database are lost when their pods are recreated. Change the sample Grafana
credentials before exposing the service outside a local demonstration.

The Jenkins deployment stage also applies `k8s/monitoring` and waits for both
monitoring deployments to become ready.
