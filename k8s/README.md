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
