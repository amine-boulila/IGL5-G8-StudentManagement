# Validation Day Runbook: student-management on EKS

Deploy the app, then point `devops.forjaw.com` to the load balancer manually (ExaHost DNS).

> Start 30+ minutes before the demo. EKS cluster creation is slow.

## 1. Deploy

1. Run the **deploy pipeline** in Jenkins and wait for it to finish green.
2. Connect kubectl to the cluster (CloudShell or your machine):

   ```bash
   aws eks update-kubeconfig --region us-east-1 --name student-management-eks
   ```

3. Check nodes and pods:

   ```bash
   kubectl get nodes   # 2 nodes in Ready
   kubectl get pods    # mysql and student-management both 1/1 Running
   ```

   If a pod is Pending:

   ```bash
   kubectl describe pod <pod-name>   # read the Events section
   ```

## 2. Test through the load balancer (before DNS)

1. Get the ELB hostname (if empty, wait 1-2 minutes and retry):

   ```bash
   kubectl get svc student-management -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
   ```

2. Test the API directly:

   ```bash
   curl -i http://<ELB-HOSTNAME>/student/students/getAllStudents
   ```

   Expected: `HTTP/1.1 200` and the JSON list.

## 3. Update the DNS record at ExaHost

The ELB hostname changes after every fresh deploy, so the CNAME must be updated each time.

| Field | Value |
|---|---|
| Type | `CNAME` |
| Nom | `devops` |
| Contenu | `<ELB-HOSTNAME>` (no `http://`, no trailing `/`) |
| TTL | 10 minutes |

If a `devops` record already exists, **edit it** instead of adding a second one.

## 4. Verify DNS and the API

```bash
nslookup devops.forjaw.com 8.8.8.8     # must show the elb.amazonaws.com name
curl -i http://devops.forjaw.com/student/students/getAllStudents
```

Use `http://`, not `https://` (no certificate on the load balancer).

## 5. If your PC still resolves the old address

Stale router cache. Run in an **Administrator** terminal:

```bash
ipconfig /flushdns
netsh interface ip set dns name="Ethernet" static 8.8.8.8
```

Then retest. Afterwards, restore automatic DNS:

```bash
netsh interface ip set dns name="Ethernet" dhcp
netsh interface ip set dns name="Wi-Fi" dhcp
ipconfig /flushdns
```

In the browser, use a private window and type `http://` explicitly. If it still forces HTTPS, delete `devops.forjaw.com` at `chrome://net-internals/#hsts`.

**Fallback during the demo:** use the ELB hostname directly (section 2). It needs no DNS.

## Troubleshooting

| Symptom | Check |
|---|---|
| Pod Pending | `kubectl describe pod <name>`: look for `Insufficient memory`, then confirm 2 nodes |
| Pod crash-looping | `kubectl logs deploy/student-management --tail=50` |
| Pipeline "timed out waiting for the condition" | run the two commands above; the cause is in Events or logs |
| `kubectl` cannot resolve the endpoint | rerun `aws eks update-kubeconfig` (stale kubeconfig) |
| Domain redirects to Vercel | stale local DNS cache (section 5) |

Pod label for selectors: `app.kubernetes.io/name=student-management`.

## After validation

Run the **destroy pipeline** to stop the charges (EKS control plane + 2 nodes).

## Before the day

- Do a dry run a few days ahead so surprises show up while there is time to fix them.
- Confirm Terraform has `t3.medium` and `desired_size = 2` committed.
- Optional: move the MySQL password into a Kubernetes Secret.
