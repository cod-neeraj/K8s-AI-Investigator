# K8s-AI-Investigator

**AI-Powered Kubernetes Cluster Health Analysis Tool**

---

## 🚀 Project Overview

K8s AI Investigator is an open-source developer tool that connects to a Kubernetes cluster via `kubectl`, collects health data across pods, nodes, deployments, and events, and forwards that evidence to an LLM via OpenRouter to generate structured root-cause analysis with actionable fixes.

The goal is to eliminate manual debugging (multiple `kubectl` commands, logs, and events) and replace it with a single-click AI-powered investigation.

---

## ✨ Key Highlights

* No Kubernetes operator or in-cluster agent required
* Works with any cluster (minikube, kind, GKE, EKS, AKS, bare-metal)
* Free AI tier via OpenRouter
* Single executable JAR — no Node.js or Python runtime needed
* Fully open-source (MIT License)

---

## 🛠️ Technology Stack

### Backend

* **Quarkus 3.x** — Fast, lightweight Java framework
* **Java 21 (LTS)**
* **JAX-RS (RESTEasy Reactive)** — REST APIs
* **Jackson** — JSON serialization
* **MicroProfile Config** — External configuration
* **Java HttpClient** — API calls to OpenRouter
* **Maven** — Build tool

### Frontend

* **Vanilla JavaScript (ES2022)** — SPA logic
* **HTML5 + CSS3** — UI + dark mode
* Served directly via Quarkus static resources

### AI / External Services

* **OpenRouter.ai** — LLM gateway
* **Default Model:** `google/gemma-3-27b-it:free`
* OpenAI-compatible API (configurable)

### DevOps / Infrastructure

* `kubectl` — Cluster interaction
* Docker — Container packaging
* GitHub Actions — CI pipeline
* GraalVM (optional) — Native build

---

## 🏗️ System Architecture

### High-Level Flow

```
Browser (index.html)
   |
   |-- GET /api/clusters
   |-- POST /api/investigate/{context}
   |-- POST /api/ai-summary
   ↓
Quarkus REST Layer
   ↓
Investigation Service
   ├── Pod Inspector
   ├── Node Inspector
   ├── Deployment Inspector
   ├── Logs Collector
   └── Events Analyzer
   ↓
AI Analysis Service → OpenRouter API
   ↓
Structured AI Response
```

---

## 📂 Package Structure

```
src/main/java/org/example/
├── InvestigationResource.java
├── AiSummaryResource.java
├── InvestigationService.java
├── PodInspector.java
├── NodeInspector.java
├── DeploymentInspector.java
├── LogsCollector.java
├── EventsAnalyzer.java
├── AiAnalysisService.java
├── ClusterService.java
├── KubectlExecutor.java
├── InvestigationResult.java
├── PodSummary.java
├── NodeSummary.java
├── DeploymentSummary.java
├── EventSummary.java
├── AiSummaryResult.java
└── AiIssue.java
```

---

## 🌐 REST API Reference

| Method | Endpoint                     | Description                     |
| ------ | ---------------------------- | ------------------------------- |
| GET    | `/api/clusters`              | List kubectl contexts           |
| POST   | `/api/investigate/{context}` | Full cluster investigation      |
| POST   | `/api/ai-summary`            | AI analysis from collected data |
| GET    | `/api/nodes/{context}`       | Node health snapshot            |
| GET    | `/api/deployments/{context}` | Deployment status               |

---

## 📊 Core Data Models

### InvestigationResult

* `pods` → Pod status and health
* `nodes` → Node conditions and resources
* `deployments` → Replica and rollout status
* `events` → Warning events
* `logs` → Logs from unhealthy pods

### AiSummaryResult

* `overallHealth` → `healthy` or `degraded`
* `issues` → List of detected problems

### AiIssue

* `affectedResource` → namespace/name
* `rootCause` → Explanation of issue
* `confidence` → 0–100 score
* `severity` → low / medium / high
* `suggestedFix` → kubectl command or config fix

---

## ⚙️ Configuration

| Property                  | Env Variable       | Default        |
| ------------------------- | ------------------ | -------------- |
| openrouter.api.key        | OPENROUTER_API_KEY | Required       |
| openrouter.model          | OPENROUTER_MODEL   | gemma-3-27b-it |
| quarkus.http.port         | —                  | 8080           |
| quarkus.http.cors.origins | —                  | *              |
| logging level             | —                  | DEBUG          |

---

## 🎯 Feature Overview

### Dashboard UI

* Summary cards (nodes, pods, deployments, events, AI status)
* 5 tabs: Pods / Nodes / Deployments / Events / Logs
* Dark mode UI
* Responsive design
* Loading + error states

### AI Analysis Panel

* Health banner (Healthy / Degraded)
* Issue cards with:

  * Root cause
  * Severity
  * Confidence
  * Suggested fix
* Structured JSON-based LLM output

---

## 🌍 Multi-Cluster Support

* Automatically detects kubectl contexts
* Switch clusters via dropdown
* Context passed dynamically to backend APIs

---

## 🛣️ Roadmap

* Helm chart for in-cluster deployment
* PostgreSQL for historical analysis
* Slack / PagerDuty alerts
* Namespace filtering
* Metrics visualization
* Export reports as PDF

---


## 🤝 Contributions

Open-source contributions are welcome!
Please open an issue before submitting large pull requests.
