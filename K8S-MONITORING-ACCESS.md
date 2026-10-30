# Connect to EKS and open Grafana and Prometheus

These commands are for **Windows PowerShell**. Start a fresh AWS Academy lab session first. Keep your access key, secret key, and session token private; never commit them to the repository.

## 1. Configure your AWS CLI profile

Run this once, or again when AWS Academy gives you a new access key and secret key:

```powershell
aws configure --profile student-management
```

Enter the values from **AWS Details > AWS CLI**:

- AWS Access Key ID: your current access key
- AWS Secret Access Key: your current secret key
- Default region: `us-east-1`
- Default output format: `json`

## 2. Set the temporary session token

Run this in **each PowerShell window** where you will use AWS or `kubectl`. The token is temporary and is kept only in that window:

```powershell
$token = Read-Host "Paste the current AWS Academy session token" -AsSecureString
$env:AWS_SESSION_TOKEN = [System.Net.NetworkCredential]::new("", $token).Password
```

Check that AWS credentials work:

```powershell
aws sts get-caller-identity --profile student-management
```

If you get `ExpiredTokenException`, start or refresh the AWS Academy lab and repeat sections 1 and 2 with its current credentials.

## 3. Connect `kubectl` to the current EKS cluster

```powershell
aws eks update-kubeconfig --region us-east-1 --name student-management-eks --profile student-management
kubectl get nodes
```

Continue when the node status is `Ready`. Run `update-kubeconfig` again if the cluster is recreated or `kubectl` reports an old endpoint hostname.

Optional: check the EBS CSI controller if you are using the persistent MySQL PVC:

```powershell
kubectl get pods -n kube-system | Select-String ebs-csi-controller
```

Both controller pods should show `6/6` and `Running` before relying on EBS volumes.

## 4. Ensure Grafana and Prometheus are deployed

Change to the repository root, then apply the monitoring manifests:

```powershell
Set-Location 'C:\MSI\Devops-projet-fac\IGL5-G8-StudentManagement'
kubectl apply -f k8s/namespace.yaml
kubectl apply -k k8s/monitoring
kubectl get pods,services -n student-management
```

## 5. Open Grafana

In the first PowerShell window, start port forwarding and leave it running:

```powershell
kubectl port-forward service/grafana 3000:3000 -n student-management
```

Open <http://localhost:3000>. The project defaults are username `admin` and password `admin`.

## 6. Open Prometheus

Open a **second PowerShell window**. Repeat sections 2 and 3 to set a fresh session token and connect `kubectl`, then start port forwarding:

```powershell
kubectl port-forward service/prometheus 9090:9090 -n student-management
```

Open <http://localhost:9090>. Keep both port-forward windows open while using the dashboards.

## Quick check

```powershell
kubectl get pods,services -n student-management
```

The namespace should contain `grafana` and `prometheus` pods and services. If either service is missing, re-run `kubectl apply -k k8s/monitoring` from the repository root.
