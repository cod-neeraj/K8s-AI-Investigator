# 📖 How to Use

## K8s AI Investigator — Complete Setup & Usage Guide

---

## ⚙️ Prerequisites

Make sure the following are installed:

| Requirement        | Version      | Check Command              |
| ------------------ | ------------ | -------------------------- |
| Java (JDK)         | 21+          | `java -version`            |
| Maven              | 3.9+         | `./mvnw -v`                |
| kubectl            | Any          | `kubectl version --client` |
| OpenRouter API Key | Free tier OK | Sign up at openrouter.ai   |
| Kubernetes Cluster | Any          | `kubectl get nodes`        |

💡 Tip: No cluster? Run:

```bash
minikube start
# OR
kind create cluster
```

---

## 🔑 Step 1 — Get OpenRouter API Key

1. Go to https://openrouter.ai
2. Create account
3. Go to **Keys → Create Key**
4. Copy key (`sk-or-v1-...`)

⚠️ **Never commit API keys. Use environment variables.**

---

## 📦 Step 2 — Get the Project

```bash
git clone https://github.com/YOUR_USERNAME/k8s-ai-investigator.git
cd k8s-ai-investigator
```

OR:

```bash
unzip k8s-ai-investigator.zip
cd k8s-ai-investigator
```

---

## 🌍 Step 3 — Set Environment Variable

### Windows (CMD)

```bash
set OPENROUTER_API_KEY=your-key
```

### PowerShell

```bash
$env:OPENROUTER_API_KEY="your-key"
```

### Linux / Mac

```bash
export OPENROUTER_API_KEY=your-key
```

---

## 🚀 Step 4 — Start Application

```bash
./mvnw quarkus:dev
```

You should see:

```
Listening on: http://0.0.0.0:8080
```

---

## 🌐 Step 5 — Open Dashboard

Open:

```
http://localhost:8080
```

---

## 🔍 Step 6 — Run Investigation

1. Select cluster (auto-loaded from kubeconfig)
2. Click **Investigate**

### What happens:

* Checks pods health
* Checks nodes status
* Verifies deployments
* Collects logs
* Collects warning events
* Sends data to AI

⏱ Takes ~5–15 seconds

---

## 📊 Step 6.3 — Tabs Overview

| Tab         | Description             |
| ----------- | ----------------------- |
| Pods        | All pods + health       |
| Nodes       | Node status, CPU/memory |
| Deployments | Replica status          |
| Events      | Warning events          |
| Logs        | Unhealthy pod logs      |
| AI Analysis | Root cause + fixes      |

---

## 🤖 Step 7 — AI Analysis

### 🟢 Healthy

No issues found

### 🔴 Degraded

Issues detected

### Issue Card Fields

* **Affected Resource** → pod/deployment
* **Severity** → low / medium / high
* **Confidence** → % certainty
* **Root Cause** → explanation
* **Suggested Fix** → kubectl command

💡 You can directly run suggested commands.

---

## 📦 Build for Production

### Standard JAR

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Fat JAR

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

---

## 🐳 Docker

```bash
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t k8s-ai-investigator .
```

Run:

```bash
docker run -e OPENROUTER_API_KEY=your-key \
 -v ~/.kube:/root/.kube:ro \
 -p 8080:8080 k8s-ai-investigator
```

---

## 🔄 Change AI Model

```bash
export OPENROUTER_MODEL=anthropic/claude-3.5-sonnet
```

Free option:

```bash
export OPENROUTER_MODEL=mistralai/mistral-7b-instruct:free
```

---

## 🛠️ Troubleshooting

| Problem              | Solution                      |
| -------------------- | ----------------------------- |
| No kubectl contexts  | `kubectl config get-contexts` |
| Investigation failed | Check terminal logs           |
| API key missing      | Set env variable              |
| JSON parsing error   | Change model                  |
| kubectl not found    | Install kubectl               |
| Port 8080 in use     | Change port                   |

---

## ⚡ Quick Start

```bash
# Set key
export OPENROUTER_API_KEY=...

# Run app
./mvnw quarkus:dev

# Open
http://localhost:8080
```

---

## 🎯 Workflow

1. Select cluster
2. Click Investigate
3. Open AI Analysis tab
4. Apply suggested fix

---

## 📌 Notes

* First run downloads dependencies (~100MB)
* Subsequent runs start fast
* Works with any Kubernetes cluster

---
