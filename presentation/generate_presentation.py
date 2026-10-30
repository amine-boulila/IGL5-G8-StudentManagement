from __future__ import annotations

import re
from pathlib import Path
from typing import Iterable

from PIL import Image
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.util import Inches, Pt


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "presentation" / "presentation_devops.pptx"
SCREENSHOTS = ROOT / "presentation" / "screen shoots"
LOGOS = ROOT / "presentation" / "logos"

CREAM = RGBColor(0xF5, 0xF0, 0xE1)
SAGE = RGBColor(0x9C, 0xAF, 0x88)
DARK_SAGE = RGBColor(0x6B, 0x7F, 0x5E)
INK = RGBColor(0x2F, 0x3A, 0x2C)
CODE_BG = RGBColor(0x2B, 0x33, 0x28)
CODE_TEXT = RGBColor(0xF5, 0xF0, 0xE1)
MUTED = RGBColor(0x6D, 0x76, 0x68)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
blank = prs.slide_layouts[6]
slide_no = 0
placeholders: set[str] = set()
missing_screenshots: list[str] = []
PLACEHOLDER = "<" + chr(192) + " COMPL" + chr(201) + "TER>"


def sanitize(value: str) -> str:
    value = value.replace("devops.forjaw.com", "<LOAD_BALANCER_DNS>")
    value = value.replace("forjaw.com", "<LOAD_BALANCER_DNS>")
    value = value.replace("ExaHost", "<LOAD_BALANCER_DNS>")
    value = re.sub(r"(?i)(password|token|secret|authtoken|access[_-]?key)\s*[:=]\s*(['\"]?)[^,\s'\"]+\2", r"\1=********", value)
    value = re.sub(r"(?i)(smtp_auth_password\s*=\s*)([^,\s\"']+)", r"\1********", value)
    value = value.replace("change-me", "********")
    return value


def source(rel: str) -> str:
    return sanitize((ROOT / rel).read_text(encoding="utf-8", errors="replace"))


def add_box(slide, x, y, w, h, fill, line=None, radius=False):
    shape = slide.shapes.add_shape(
        MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE,
        Inches(x), Inches(y), Inches(w), Inches(h),
    )
    shape.fill.solid()
    shape.fill.fore_color.rgb = fill
    shape.line.color.rgb = line or fill
    return shape


def text(slide, value, x, y, w, h, size=18, color=INK, bold=False,
         font="Aptos", align=PP_ALIGN.LEFT, valign=MSO_ANCHOR.TOP):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.clear()
    tf.word_wrap = True
    tf.vertical_anchor = valign
    p = tf.paragraphs[0]
    p.alignment = align
    run = p.add_run()
    run.text = sanitize(value)
    run.font.name = font
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.color.rgb = color
    return box


def bullets(slide, items: Iterable[str], x, y, w, h, size=16, color=INK):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.clear()
    tf.word_wrap = True
    for i, item in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.text = sanitize(item)
        p.level = 0
        p.font.name = "Aptos"
        p.font.size = Pt(size)
        p.font.color.rgb = color
        p.space_after = Pt(7)
    return box


def code(slide, snippet: str, x=0.65, y=1.45, w=12.0, h=5.25, size=11):
    shape = add_box(slide, x, y, w, h, CODE_BG, radius=True)
    tf = shape.text_frame
    tf.clear()
    tf.margin_left = Inches(0.18)
    tf.margin_right = Inches(0.18)
    tf.margin_top = Inches(0.12)
    tf.word_wrap = False
    lines = sanitize(snippet).splitlines()
    for i, line in enumerate(lines[:18]):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.text = line
        p.font.name = "Cascadia Mono"
        p.font.size = Pt(size)
        p.font.color.rgb = CODE_TEXT
        p.space_after = Pt(1)
    return shape


def header(slide, chapter: str, title: str):
    add_box(slide, 0, 0, 13.333, 0.58, DARK_SAGE)
    text(slide, chapter.upper(), 0.55, 0.13, 3.2, 0.28, 11, CREAM, True)
    text(slide, title, 0.55, 0.82, 12.2, 0.48, 25, DARK_SAGE, True)


def footer(slide):
    text(slide, "Student Management • Soutenance DevOps • 2026–2027",
         0.55, 7.18, 8.5, 0.2, 8, MUTED)
    text(slide, f"{slide_no:02d}", 12.25, 7.15, 0.5, 0.22, 9, DARK_SAGE, True, align=PP_ALIGN.RIGHT)


def new_slide(chapter: str, title_value: str, with_footer=True):
    global slide_no
    slide_no += 1
    slide = prs.slides.add_slide(blank)
    slide.background.fill.solid()
    slide.background.fill.fore_color.rgb = CREAM
    header(slide, chapter, title_value)
    if with_footer:
        footer(slide)
    return slide


def note_placeholder(label: str) -> str:
    placeholders.add(PLACEHOLDER)
    return PLACEHOLDER


def add_image(slide, filename: str, x, y, w, h, caption: str):
    path = SCREENSHOTS / filename
    if not path.exists():
        missing_screenshots.append(filename)
        add_box(slide, x, y, w, h, RGBColor(0xE7, 0xDF, 0xCC), line=SAGE, radius=True)
        text(slide, f"Capture absente\n{filename}", x + 0.2, y + h / 2 - 0.25, w - 0.4, 0.5, 14, MUTED, True, align=PP_ALIGN.CENTER)
    else:
        with Image.open(path) as image:
            image_ratio = image.width / image.height
        box_ratio = w / h
        if image_ratio >= box_ratio:
            draw_w = w
            draw_h = w / image_ratio
            draw_x = x
            draw_y = y + (h - draw_h) / 2
        else:
            draw_h = h
            draw_w = h * image_ratio
            draw_x = x + (w - draw_w) / 2
            draw_y = y
        slide.shapes.add_picture(str(path), Inches(draw_x), Inches(draw_y),
                                 width=Inches(draw_w), height=Inches(draw_h))
    text(slide, caption, x, y + h + 0.06, w, 0.22, 9, MUTED, align=PP_ALIGN.CENTER)


def add_picture_fit(slide, path: Path, x, y, w, h):
    with Image.open(path) as image:
        image_ratio = image.width / image.height
    box_ratio = w / h
    if image_ratio >= box_ratio:
        draw_w, draw_h = w, w / image_ratio
        draw_x, draw_y = x, y + (h - draw_h) / 2
    else:
        draw_w, draw_h = h * image_ratio, h
        draw_x, draw_y = x + (w - draw_w) / 2, y
    slide.shapes.add_picture(str(path), Inches(draw_x), Inches(draw_y),
                             width=Inches(draw_w), height=Inches(draw_h))


def two_columns(slide, left_title, left_items, right_title, right_items):
    add_box(slide, 0.65, 1.45, 5.85, 5.25, WHITE, line=SAGE, radius=True)
    add_box(slide, 6.85, 1.45, 5.85, 5.25, WHITE, line=SAGE, radius=True)
    text(slide, left_title, 0.95, 1.72, 5.2, 0.3, 17, DARK_SAGE, True)
    bullets(slide, left_items, 0.95, 2.15, 5.1, 4.2, 14)
    text(slide, right_title, 7.15, 1.72, 5.2, 0.3, 17, DARK_SAGE, True)
    bullets(slide, right_items, 7.15, 2.15, 5.1, 4.2, 14)


def flow_node(slide, label, x, y, w=1.45):
    add_box(slide, x, y, w, 0.72, WHITE, line=DARK_SAGE, radius=True)
    text(slide, label, x + 0.08, y + 0.16, w - 0.16, 0.36, 12, INK, True, align=PP_ALIGN.CENTER, valign=MSO_ANCHOR.MIDDLE)


# 1 — cover
slide_no += 1
slide = prs.slides.add_slide(blank)
slide.background.fill.solid()
slide.background.fill.fore_color.rgb = CREAM
add_box(slide, 0, 0, 13.333, 0.22, DARK_SAGE)
for logo, x in [("fst.png", 0.55), ("utm.png", 11.55)]:
    path = LOGOS / logo
    if path.exists():
        add_picture_fit(slide, path, x, 0.45, 1.2, 0.9)
text(slide, "SOUTENANCE DEVOPS", 0.75, 1.55, 11.8, 0.35, 15, DARK_SAGE, True, align=PP_ALIGN.CENTER)
text(slide, "Mise en place d'un pipeline CI/CD DevOps complet sur AWS EKS",
     1.0, 2.05, 11.3, 0.85, 30, INK, True, align=PP_ALIGN.CENTER)
text(slide, "(Jenkins, SonarQube, Nexus, Terraform, Monitoring)",
     1.0, 2.95, 11.3, 0.4, 19, DARK_SAGE, False, align=PP_ALIGN.CENTER)
add_box(slide, 3.15, 3.7, 7.05, 0.04, SAGE)
text(slide, "Encadrante : Pr. Thouraya Louati\nRéalisé par : Amine Boulila\nClasse : IGL4 – Faculté des Sciences de Tunis, Université Tunis El Manar\nAnnée universitaire : 2026-2027",
     1.2, 4.15, 10.9, 1.35, 15, INK, False, align=PP_ALIGN.CENTER)
footer(slide)

# 2 — overview
slide = new_slide("Vue d'ensemble", "Sommaire et architecture globale")
text(slide, "Chaîne CI/CD : du commit à l'application monitorée", 0.72, 1.25, 11.8, 0.3, 14, MUTED, align=PP_ALIGN.CENTER)
nodes = [("GitHub", 0.65), ("Jenkins", 2.2), ("Maven\n+ tests", 3.75), ("SonarQube", 5.3), ("Nexus", 6.85), ("Docker Hub", 8.4), ("AWS EKS", 9.95), ("Monitoring", 11.5)]
for label, x in nodes:
    flow_node(slide, label, x, 2.0, 1.28)
for x in [1.93, 3.48, 5.03, 6.58, 8.13, 9.68, 11.23]:
    text(slide, "→", x, 2.2, 0.25, 0.25, 20, DARK_SAGE, True, align=PP_ALIGN.CENTER)
add_box(slide, 1.2, 3.2, 10.95, 1.0, WHITE, line=SAGE, radius=True)
text(slide, "Webhook GitHub  →  Jenkins  →  build / qualité / artefact  →  image Docker  →  Terraform / EKS  →  Kubernetes  →  monitoring",
     1.5, 3.48, 10.35, 0.42, 16, INK, True, align=PP_ALIGN.CENTER, valign=MSO_ANCHOR.MIDDLE)
add_box(slide, 2.15, 4.75, 4.25, 1.1, WHITE, line=SAGE, radius=True)
add_box(slide, 6.95, 4.75, 4.25, 1.1, WHITE, line=SAGE, radius=True)
text(slide, "Entrées\nGitHub • branche feature/amine • webhook", 2.4, 5.0, 3.75, 0.6, 14, DARK_SAGE, True, align=PP_ALIGN.CENTER)
text(slide, "Sorties\nLoadBalancer AWS • Grafana • Alertmanager", 7.2, 5.0, 3.75, 0.6, 14, DARK_SAGE, True, align=PP_ALIGN.CENTER)

# 3 — table of contents
slide = new_slide("Vue d'ensemble", "Sommaire de la soutenance")
two_columns(slide, "Technologies", ["Git / GitHub", "Jenkins", "Maven & tests", "SonarQube", "Nexus", "Docker / Docker Hub", "AWS + Terraform"], "Déploiement et bilan", ["Kubernetes / EKS", "Monitoring", "Webhook & ngrok", "Pipeline Destroy", "Résultat final", "Difficultés et améliorations", "Conclusion"])

# 4 — Git
slide = new_slide("Git / GitHub", "Dépôt, branche et commandes")
two_columns(slide, "Référentiel", ["Dépôt : github.com/amine-boulila/IGL5-G8-StudentManagement", "Branche CI/CD : feature/amine", "Application Spring Boot + MySQL", "Webhook GitHub → Jenkins pour chaque commit"], "Commandes / installation", ["sudo apt-get update", "sudo apt-get install -y git", "git clone <URL>", "git checkout feature/amine", "git add .", "git commit -m \"...\"", "git push origin feature/amine"])

# 5 — Git evidence
slide = new_slide("Git / GitHub", "Preuve : dépôt et webhook")
add_image(slide, "01_github_repo.png", 0.65, 1.45, 5.85, 4.7, "Dépôt GitHub")
add_image(slide, "02_github_webhook.png", 6.85, 1.45, 5.85, 4.7, "Webhook GitHub")

# 6 — Jenkins configuration
slide = new_slide("Jenkins", "Installation, plugins, outils et credentials")
two_columns(slide, "Installation / outils", ["docker volume create jenkins_home", "docker run -d --name jenkins -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts-jdk21", "Plugins : Pipeline, Git, Credentials, JUnit, JaCoCo, SonarQube, Docker, AWS / Kubernetes", "Outils globaux : JDK21 et Maven3", "Agent : any"], "Credentials IDs", ["github-creds", "aws-lab-creds (fichier)", "sonarqube-token", "nexus-creds", "dockerhub-creds", "smtp-app-password"])
text(slide, "Variables environment : SONAR_URL, NEXUS_URL, SONAR_PROJECT_KEY, AWS_DEFAULT_REGION, EKS_CLUSTER_NAME, EKS_ROLE_NAME", 0.9, 6.2, 11.5, 0.35, 12, DARK_SAGE, True, align=PP_ALIGN.CENTER)

# 7 — Jenkins stages / proof
slide = new_slide("Jenkins", "Pipeline déclaratif : stages et preuves")
snippet = """pipeline {
  agent any
  tools { jdk 'JDK21'; maven 'Maven3' }
  stages {
    stage('Checkout Git') { ... }
    stage('MVN Clean') { sh 'mvn clean' }
    stage('Artifact Construction') { ... }
    stage('Unit Tests') { ... }
    stage('MVN SonarQube') { ... }
    stage('Publish to Nexus') { ... }
    stage('Building Our Image') { ... }
    stage('Deploy Our Image') { ... }
  }
}"""
code(slide, snippet, 0.65, 1.35, 6.1, 5.35, 11)
add_image(slide, "08_jenkins_dashboard.png", 7.05, 1.35, 5.6, 2.35, "Dashboard Jenkins")
add_image(slide, "09_jenkins_credentials.png", 7.05, 4.05, 5.6, 2.35, "Credentials Jenkins")

# 8 — Jenkins deploy proof
slide = new_slide("Jenkins", "Pipeline Deploy : déploiement réussi")
add_image(slide, "10_jenkins_deploy_pipeline.png", 0.65, 1.2, 12.0, 5.8, "Pipeline Jenkins Deploy — stages exécutés avec succès")

# 9 — Jenkins destroy proof
slide = new_slide("Jenkins", "Pipeline Destroy : nettoyage contrôlé")
add_image(slide, "11_jenkins_destroy_pipeline.png", 0.65, 1.2, 12.0, 5.8, "Pipeline Jenkins Destroy — suppression contrôlée")

# 10 — Maven
slide = new_slide("Maven & tests", "Installation, construction et qualité de test")
two_columns(slide, "Installation / commandes", ["sudo apt-get update", "sudo apt-get install -y openjdk-21-jdk maven", "mvn clean", "mvn package -DskipTests", "mvn verify", "test -s target/site/jacoco/jacoco.xml", "Rapports JUnit : **/target/surefire-reports/*.xml"], "Configuration", ["Java demandé dans Jenkins : JDK21", "Version projet dans pom.xml : Java 17", "JaCoCo : rapport en phase verify", "JUnit : publication Jenkins via junit()"])

# 11 — Maven code
slide = new_slide("Maven & tests", "Extrait : stage Unit Tests et JaCoCo")
code(slide, source("Jenkinsfile")[source("Jenkinsfile").index("stage('Unit Tests')"):source("Jenkinsfile").index("stage('MVN SonarQube')")])
text(slide, "Le fichier jacoco.xml devient l'entrée de SonarQube ; les XML Surefire sont publiés par Jenkins.", 0.85, 6.78, 11.7, 0.25, 12, DARK_SAGE, True, align=PP_ALIGN.CENTER)

# 12 — Sonar
slide = new_slide("SonarQube", "Installation, token et analyse de couverture")
two_columns(slide, "Installation / configuration", ["docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community", "URL configurée : http://host.docker.internal:9000", "Projet : IGL5-G8-StudentManagement", "Credential Jenkins : sonarqube-token", "Secret transmis via SONAR_TOKEN"], "Commande du stage", ["mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.2.0.4988:sonar", "-Dsonar.host.url=\"$SONAR_URL\"", "-Dsonar.projectKey=\"$SONAR_PROJECT_KEY\"", "-Dsonar.coverage.jacoco.xmlReportPaths=\"target/site/jacoco/jacoco.xml\"", "-Dsonar.token=\"$SONAR_TOKEN\""])

# 13 — Sonar proof
slide = new_slide("SonarQube", "Preuve : dashboard de qualité")
add_image(slide, "22_sonarqube_dashboard.png", 1.0, 1.35, 11.3, 5.65, "Dashboard SonarQube du projet")

# 14 — Nexus
slide = new_slide("Nexus", "Repositories, credentials et publication Maven")
two_columns(slide, "Installation / repositories", ["docker run -d --name nexus -p 8081:8081 sonatype/nexus3", "URL : http://host.docker.internal:8081", "maven-snapshots", "maven-releases", "Credential Jenkins : nexus-creds"], "Commandes / sécurité", ["settings.xml est généré temporairement par le pipeline", "mvn -s jenkins-settings.xml deploy", "-DskipTests", "Publication vers maven-snapshots ou maven-releases", "Le fichier est supprimé dans finally"])

# 15 — Nexus proof/code
slide = new_slide("Nexus", "Preuve et extrait de publication")
snippet = """writeFile file: 'jenkins-settings.xml', text: \"\"\"<settings>
  <servers>
    <server><id>nexus-snapshots</id>
      <username>${xmlEscape(env.NEXUS_USER)}</username>
      <password>${xmlEscape(env.NEXUS_PASSWORD)}</password>
    </server>
  </servers>
</settings>\"\"\"
mvn -s jenkins-settings.xml deploy -DskipTests
rm -f jenkins-settings.xml"""
code(slide, snippet, 0.65, 1.35, 6.2, 5.35, 10)
add_image(slide, "23_nexus_repositories.png", 7.15, 1.55, 5.35, 4.65, "Repositories Nexus")

# 16 — Docker
slide = new_slide("Docker / Docker Hub", "Dockerfile, build, tags et push")
snippet = """FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]"""
code(slide, snippet, 0.65, 1.35, 5.7, 3.2, 13)
code(slide, """docker build -t student-management:${BUILD_NUMBER} .
docker tag student-management:${BUILD_NUMBER} $DOCKERHUB_USER/student-management:${BUILD_NUMBER}
docker tag student-management:${BUILD_NUMBER} $DOCKERHUB_USER/student-management:latest
echo \"$DOCKERHUB_TOKEN\" | docker login --username \"$DOCKERHUB_USER\" --password-stdin
docker push $DOCKERHUB_USER/student-management:${BUILD_NUMBER}
docker push $DOCKERHUB_USER/student-management:latest
docker logout""", 6.65, 1.35, 6.0, 3.2, 11)
text(slide, "Credential Jenkins : dockerhub-creds • Le token Docker Hub est masqué.", 0.9, 5.1, 11.6, 0.35, 14, DARK_SAGE, True, align=PP_ALIGN.CENTER)
text(slide, "Installation Docker : curl -fsSL https://get.docker.com | sh && sudo usermod -aG docker $USER", 0.9, 5.38, 11.6, 0.25, 11, MUTED, align=PP_ALIGN.CENTER)
add_image(slide, "03_dockerhub_repo.png", 3.9, 5.55, 5.55, 1.25, "Repository Docker Hub")

# 14 — AWS
slide = new_slide("AWS", "AWS Academy, identité et état Terraform")
two_columns(slide, "Contexte AWS", ["Région : us-east-1", "Rôle : LabRole", "Cluster : student-management-eks", "Credential Jenkins : aws-lab-creds (fichier)", "EBS CSI driver activé pour les volumes gp3", "Installation AWS CLI : sudo apt-get update && sudo apt-get install -y awscli"], "Commandes", ["aws sts get-caller-identity", "aws eks update-kubeconfig --region \"$AWS_DEFAULT_REGION\" --name \"$EKS_CLUSTER_NAME\"", "Backend S3 : bucket student-management-tfstate-amine-2026", "Clé : student-management/terraform.tfstate", "Chiffrement : encrypt = true"])

# 15 — AWS proof
slide = new_slide("AWS", "Preuve : nœuds EC2")
add_image(slide, "04_aws_ec2_nodes.png", 0.75, 1.25, 11.85, 5.55, "Nœuds EC2 du cluster")
slide = new_slide("AWS", "Preuve : cluster EKS")
add_image(slide, "05_aws_eks_cluster.png", 0.75, 1.25, 11.85, 5.55, "Cluster EKS student-management-eks")
slide = new_slide("AWS", "Preuve : Load Balancer")
add_image(slide, "06_aws_loadbalancer.png", 0.75, 1.25, 11.85, 5.55, "Load Balancer AWS")

# 16 — Terraform
slide = new_slide("Terraform", "Installation, structure et cycle d'infrastructure")
two_columns(slide, "Installation / fichiers", ["Installation : wget -O- https://apt.releases.hashicorp.com/gpg | gpg --dearmor | sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg", "sudo apt-get update && sudo apt-get install -y terraform", "terraform/versions.tf : provider AWS ~> 5.0, Terraform >= 1.10.0", "terraform/backend.tf : état distant S3", "terraform/main.tf : VPC, subnets, EKS, node group, add-on", "terraform/variables.tf et outputs.tf"], "Commandes exactes", ["terraform init -reconfigure -input=false", "terraform validate", "terraform state list", "terraform plan -input=false ... -out=tfplan", "terraform show -no-color tfplan", "terraform apply -input=false tfplan", "terraform destroy -input=false -auto-approve ..."])

# 17 — Terraform code/proof
slide = new_slide("Terraform", "Extrait : EKS et état distant")
code(slide, source("terraform/backend.tf") + "\n\n" + "\n".join(source("terraform/main.tf").splitlines()[70:116]))
add_image(slide, "07_terraform_apply.png", 7.45, 4.75, 4.95, 1.45, "Application du plan Terraform")

# 18 — Kubernetes
slide = new_slide("Kubernetes / EKS", "Manifests, stockage et déploiement")
two_columns(slide, "Commandes", ["Installation kubectl : curl -LO https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl && sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl", "kubectl get nodes", "kubectl apply -f kubernetes/mysql.yaml", "kubectl rollout status deployment/mysql --timeout=300s", "kubectl apply -f kubernetes/deployment.yaml", "kubectl apply -f kubernetes/service.yaml", "kubectl get deployments / pods / services"], "Architecture", ["MySQL : Deployment + ClusterIP + PVC 5Gi", "StorageClass gp3 via ebs.csi.aws.com", "Application : image amineboulila/student-management:latest", "Service application : type LoadBalancer", "Accès public via DNS du Load Balancer AWS"])

# 19 — Kubernetes code/proof
slide = new_slide("Kubernetes / EKS", "Extraits des manifests et vérification")
code(slide, """apiVersion: v1
kind: Service
metadata:
  name: student-management
spec:
  type: LoadBalancer
  selector:
    app.kubernetes.io/name: student-management
  ports:
    - port: 80
      targetPort: http""", 0.65, 1.35, 5.8, 4.0, 12)
code(slide, """kubectl get nodes
kubectl get deployments
kubectl get pods
kubectl get services

aws eks create-addon \\
  --cluster-name \"$EKS_CLUSTER_NAME\" \\
  --addon-name aws-ebs-csi-driver \\
  --region \"$AWS_DEFAULT_REGION\"""", 6.75, 1.35, 5.9, 4.0, 11)
add_image(slide, "12_kubectl_nodes_pods.png", 0.8, 5.55, 5.5, 1.2, "Nœuds et pods")
add_image(slide, "13_kubectl_services.png", 7.0, 5.55, 5.5, 1.2, "Services Kubernetes")

# 20 — Monitoring
slide = new_slide("Monitoring", "Installation Helm et configuration des alertes")
code(slide, """helm repo add prometheus-community \\
  https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install monitoring \\
  prometheus-community/kube-prometheus-stack \\
  -n monitoring --create-namespace \\
  -f monitoring-values.yaml \\
  --set-string \"alertmanager.config.global.smtp_auth_password=$SMTP_PASS\" \\
  --timeout 10m
kubectl get pods -n monitoring
kubectl get services -n monitoring""", 0.65, 1.35, 7.1, 5.35, 11)
two_columns(slide, "Stack déployée", ["Prometheus : rétention 3 jours", "Grafana : dashboards", "Alertmanager : receiver email", "SMTP Gmail : port 587 / TLS", "Mot de passe SMTP : ********"], "Installation / accès", ["curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash", "helm repo add prometheus-community https://prometheus-community.github.io/helm-charts", "kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090", "kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80", "kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-alertmanager 9093:9093"])

# 21 — Monitoring proof
slide = new_slide("Monitoring", "Preuve : cibles Prometheus")
add_image(slide, "16_prometheus_targets.png", 0.75, 1.25, 11.85, 5.55, "Targets Prometheus")
slide = new_slide("Monitoring", "Preuve : dashboard Grafana")
add_image(slide, "17_grafana_dashboard.png", 0.75, 1.25, 11.85, 5.55, "Dashboard Grafana")
slide = new_slide("Monitoring", "Preuves : Alertmanager et email")
add_image(slide, "18_alertmanager.png", 0.75, 1.25, 5.75, 5.25, "Alertmanager")
add_image(slide, "19_alert_email.png", 6.85, 1.25, 5.75, 5.25, "Email d'alerte")

# 22 — webhook/ngrok
slide = new_slide("Webhook & ngrok", "Déclenchement automatique")
two_columns(slide, "Commande présente dans le dépôt", ["winget install Ngrok.Ngrok", "ngrok config add-authtoken ********", "ngrok http --url=yeasty-brainsickly-lela.ngrok-free.dev 8080", "Le tunnel expose Jenkins local", "URL de callback configurée dans GitHub"], "Flux", ["Commit sur feature/amine", "GitHub envoie push webhook", "ngrok relaie vers Jenkins", "Jenkins lance le pipeline", "Vérification : Build automatique à chaque commit"])

# 23 — webhook proof
slide = new_slide("Webhook & ngrok", "Preuves : tunnel et déclenchement Jenkins")
add_image(slide, "20_ngrok_dashboard.png", 0.65, 1.35, 5.85, 4.9, "Dashboard ngrok")
add_image(slide, "21_jenkins_webhook_trigger.png", 6.85, 1.35, 5.85, 4.9, "Déclenchement webhook dans Jenkins")

# 24 — destroy
slide = new_slide("Pipeline Destroy", "Suppression contrôlée de l'infrastructure")
code(slide, """input(message: 'Delete the Kubernetes resources and all Terraform-managed AWS infrastructure?')
aws sts get-caller-identity
terraform -chdir=terraform init -reconfigure -input=false
aws eks update-kubeconfig --region \"$AWS_DEFAULT_REGION\" --name \"$EKS_CLUSTER_NAME\"
kubectl delete -f kubernetes/ --ignore-not-found=true --wait=true
aws eks delete-addon --cluster-name \"$EKS_CLUSTER_NAME\" \\
  --addon-name aws-ebs-csi-driver
terraform -chdir=terraform destroy -input=false -auto-approve \\
  -var=\"cluster_role_arn=$EKS_ROLE_ARN\" \\
  -var=\"node_role_arn=$EKS_ROLE_ARN\"""", 0.65, 1.35, 7.2, 5.35, 11)
text(slide, "Garde-fou : validation manuelle avant suppression • post : deleteDir()", 8.2, 2.1, 4.0, 1.1, 18, DARK_SAGE, True, align=PP_ALIGN.CENTER, valign=MSO_ANCHOR.MIDDLE)
add_image(slide, "11_jenkins_destroy_pipeline.png", 8.2, 3.65, 4.05, 2.3, "Pipeline Destroy Jenkins")

# 25 — result
slide = new_slide("Résultat final", "Application déployée et accessible")
add_image(slide, "24_url_result.png", 0.7, 1.3, 7.4, 5.2, "Résultat applicatif")
add_box(slide, 8.55, 1.55, 3.9, 3.0, WHITE, line=SAGE, radius=True)
text(slide, "Résultat", 8.9, 1.9, 3.2, 0.3, 19, DARK_SAGE, True, align=PP_ALIGN.CENTER)
bullets(slide, ["Build Maven + tests", "Qualité SonarQube", "Artefact Nexus", "Image Docker Hub", "EKS + MySQL", "Monitoring opérationnel"], 8.95, 2.45, 3.1, 1.75, 14)
text(slide, "<LOAD_BALANCER_DNS>", 8.75, 5.05, 3.5, 0.35, 15, INK, True, align=PP_ALIGN.CENTER)

# 26 — difficulties
slide = new_slide("Bilan", "Difficultés rencontrées et améliorations possibles")
two_columns(slide, "Difficultés", ["Coordination des credentials Jenkins et des secrets", "Création puis accès au cluster EKS", "Persistance MySQL via EBS CSI", "Exposition Jenkins local via ngrok", "Synchronisation pipeline / disponibilité du LoadBalancer"], "Améliorations", ["Secrets manager AWS ou Vault", "Domaine personnalisé + TLS", "Images versionnées et déploiement par digest", "Environnements dev / staging / prod", "Tests d'intégration et scans de vulnérabilités", "HPA, autoscaling et alertes SLO"])

# 27 — conclusion / traceability
slide = new_slide("Conclusion", "Conclusion et remerciements")
text(slide, "Une chaîne reproductible : du commit à l'application monitorée sur AWS EKS.", 0.95, 1.55, 11.4, 0.6, 24, DARK_SAGE, True, align=PP_ALIGN.CENTER)
text(slide, "Merci pour votre attention", 0.95, 2.55, 11.4, 0.55, 23, INK, True, align=PP_ALIGN.CENTER)
add_box(slide, 1.35, 3.65, 10.65, 2.0, WHITE, line=SAGE, radius=True)
text(slide, "Les commandes d'installation ont été ajoutées au support.\nLes secrets restent masqués et les preuves visuelles sont conservées à leur ratio d'origine.", 1.7, 4.05, 9.95, 0.95, 14, INK, align=PP_ALIGN.CENTER)
text(slide, "Captures manquantes : " + (", ".join(missing_screenshots) if missing_screenshots else "aucune"), 1.7, 5.35, 9.95, 0.3, 12, DARK_SAGE, True, align=PP_ALIGN.CENTER)

prs.save(OUT)
print(f"Generated {OUT}")
print("Placeholders:", ", ".join(sorted(placeholders)) or "none")
print("Missing screenshots:", ", ".join(missing_screenshots) or "none")
