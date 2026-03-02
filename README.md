# 🛡️ Aegis
**High-Performance Edge Delta Engine**  
_Sub-millisecond state differentiation and event squashing for high-scale retail ecosystems._

---

## 1. Executive Summary

**Aegis** is a specialized Java-based edge service built to solve the Event Storm problem in microservice architectures.

In high-velocity systems (flash sales, dynamic pricing, trading environments), upstream services constantly push full object updates — even when nothing meaningful changed.

Aegis sits at the edge, keeps an in-memory snapshot of global state, performs differential analysis, and converts massive raw streams into a small flow of actionable, field-level events.

---

## 2. The Problem — “The Update Avalanche”

Traditional systems broadcast full object state every time anything changes.

### This causes:

- 🔁 **Redundancy**  
  If price stays the same but gets sent 100 times → 100 useless operations downstream.

- 🌐 **Network Bloat**  
  Sending a 2KB object when only a 4-byte price changed = massive waste.

- 🗄 **Database Contention**  
  Downstream services must “read-before-write” just to check if change is real.

Result: Higher latency. DB overload. Slower systems.

---

## 3. High-Level Architecture

Aegis uses a **Sieve Architecture** — data gets refined layer by layer.

### 1️⃣ Ingress — The Disruptor
- Lock-free Ring Buffer
- Handles 1M+ events/sec
- Non-blocking network ingestion

### 2️⃣ State Comparator — The Engine
- Thread-safe in-memory snapshot store
- Bitmask-based delta detection
- O(1) field change evaluation

### 3️⃣ Egress — The Filter
- Emits event only if `deltaMask != 0`
- Suppresses redundant updates
- Outputs lightweight delta events

---

## 4. Technical Decision Log (TDL)

### TDL-001: Lock-Free Concurrency Model

**Decision:**  
Use `ConcurrentHashMap.compute()` instead of global `synchronized`.

**Reasoning:**
- Global locks create bottlenecks.
- `compute()` ensures atomic updates per ProductID.
- Different IDs update in parallel across CPU cores.

---

### TDL-002: Bitmask-Based Delta Detection

**Decision:**  
Track field changes using an integer bitmask.

Example:

```text
Price Changed = 0b01
Stock Changed = 0b10
Both Changed  = 0b11
```

Fast evaluation:

```java
if (deltaMask != 0) {
    emitEvent();
}
```

No string comparisons. Pure bitwise logic. Near nanosecond evaluation.

---

### TDL-003: Memory Over-Provisioning vs GC

**Decision:**  
Pre-allocate objects or use primitive arrays.

**Reasoning:**
- Avoid `new Object()` inside hot loops.
- Reduce garbage collection pressure.
- Prevent Stop-the-World pauses.
- Maintain stable P99 latency.

---

## 5. Downstream Protection Strategy

Aegis acts as a **Circuit Breaker for Data**.

### 🔹 Change Suppression
If incoming value matches cached state → event dropped.

### 🔹 Field-Level Targeting
Instead of emitting:

```text
UpdateProductRequest
```

Aegis emits:

```text
PriceChangedEvent
```

Downstream services can route intelligently without hitting the main database.

### 🔹 Temporal Collapsing
If 10 updates arrive within 1ms:
- Collapse into final state
- Emit only the final delta

Less noise. Same correctness.

---

## 6. Event Schema

Aegis emits **Actionable Deltas**, not full objects.

### Raw Inbound (Typical)

```json
{
  "id": "SKU-99",
  "name": "Mechanical Keyboard",
  "price": 15000,
  "stock": 45,
  "warehouse": "East-1",
  "lastUpdated": "2026-02-09T13:30:00Z"
}
```

### Aegis Outbound (Actionable)

```json
{
  "id": "SKU-99",
  "type": "PRICE_DECREASE",
  "delta": -500,
  "current": 14500
}
```

Small. Focused. Actionable.

---

## 7. Performance Benchmarks (Projected)

- 🚀 Throughput: > 1.2 million updates/sec (16-core JVM)
- ⚡ Latency (P99): < 0.5 ms (Ingress → Egress)
- 🧠 Memory: ~400 bytes per product  
  1M products ≈ 400MB RAM

---

## 8. Getting Started

### Prerequisites
- JDK 21
- Maven 3.9+

### Build

```bash
mvn clean install
```

### Run Benchmark

```bash
java -jar target/benchmarks.jar
```

---

## 9. Core Philosophy

> Only send what changed.  
> Only act when necessary.

In high-scale systems, silence is performance.