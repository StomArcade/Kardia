# Kardia

⚠️ **Status: Under Development (Beta)**

Kardia is currently in active development and should be considered **beta software**.  
APIs, configuration formats, and internal behavior may change without notice.

---

**A server lifecycle manager built with Docker and Redis for Minecraft networks**  
Integrates with Velocity and provides automated server orchestration and state management.

--- 

Shoutout to [Sulaxan](https://github.com/Sulaxan) for creating the original implementation of this system for an old network we worked on together!

Checkout the original implementation [here](https://github.com/BitByLogics/Aero-Network-Core/tree/master/NetworkCore).

---

## 🚀 What is Kardia?

Kardia is an infrastructure tool designed to automate the full lifecycle of game server instances.

It handles:
- Server creation
- Health reconciliation
- Removal of stale or stopped containers
- Redis-backed state tracking
- Integration with proxy layers such as Velocity

This enables scalable, self-healing Minecraft network deployments.

---

## 📦 Key Features

### 🐳 Docker-Native Orchestration
- Builds and runs servers as Docker containers
- Removes unhealthy or stopped containers
- Ensures Docker state matches Redis state

### 🔄 Redis-Backed State Management
- Redis is the authoritative source of server data
- Fast lookups for proxies and external systems

### 📈 Package Caching & Auto-Scaling
- Package-defined cache requirements
- Automatically starts servers to maintain minimum availability

### 🔌 Proxy Integration
- Designed to integrate with Velocity
- Dynamic server pools without manual configuration

### ⚙️ Modern Java Design
- Java 21+
- Scheduled reconciliation loops
- Async and deterministic behavior

---

## 🗺 Architecture Overview
```
+──────────+     +──────────+     +────────────+
| Velocity | <-> |   Redis  | <-> |   Kardia   |
+──────────+     +──────────+     +─────┬──────+
                                        |
                                  +─────▼────+
                                  |  Docker  |
                                  |  Engine  |
                                  +──────────+
```

- Kardia manages server lifecycle and reconciliation
- Redis stores authoritative server state
- Proxies consume Redis state directly

---

## 🧠 How It Works

### Server Updater
A scheduled task that:
- Lists running Docker containers
- Removes stopped or unhealthy containers
- Invalidates stale Redis entries

### Package Manager
A scheduled task that:
- Checks package cache requirements
- Automatically starts new servers when needed

---

## 🛠 Getting Started

### Requirements
- Java 21+
- Docker
- Redis

---

## 📁 Configuration Example

```yaml
# Redis connection details
redis:
  host: localhost
  port: 6379
  password: "password"

# IP used when advertising servers
servers:
  ip: localhost

# Port allocation range
ports:
  range:
    min: 27000
    max: 30000
```

## 🧪 Development
### Build the project:

```bash
mvn clean package
```

### Run Kardia:

```bash
java -jar kardia-server/target/kardia-server-1.0.0.jar
```

## 🧬 Redis Schema
Kardia stores server state in a Redis hash:

```
SERVERS = {
  <server-id> : <json-server-data>
}
```

Each value represents a serialized server object.

## ✔ Recommended Usage
- Run alongside Docker and Redis
- Use with Velocity or another proxy
- Avoid manual container manipulation
