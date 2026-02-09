# Aegis
High-Performance Edge Delta Engine Sub-millisecond state differentiation and event squashing for high-scale retail ecosystems.

1. Executive SummaryAegis is a specialized Java-based edge service designed to solve the Event Storm problem in microservice architectures. In high-velocity environments (Flash sales, HFT, Dynamic Pricing), upstream data sources often emit full-state updates at frequencies that overwhelm downstream transactional systems like Order Management or Fulfillment.Aegis sits at the "Edge," maintains a high-speed, in-memory snapshot of global state, and performs Differential Analysis. It transforms massive streams of raw data into a trickle of actionable, field-level events.

2. The Problem: "The Update Avalanche"Traditional systems broadcast the entire object state every time a single attribute changes.Redundancy: If a product price is updated to the same value 100 times, downstream services process 100 redundant messages.Network Bloat: Sending a 2KB JSON object when only a 4-byte price integer changed is a $500 \times$ waste of bandwidth.Downstream Friction: The Order Service (PostgreSQL/Oracle) must perform a "Read-before-Write" for every message to see if it actually needs to act, causing massive DB contention.

3. High-Level ArchitectureAegis uses a "Sieve" architecture. Data flows through three distinct layers of refinement:Ingress (The Disruptor): Uses a lock-free Ring Buffer to ingest $10^6+$ events/sec without blocking the network thread.State Comparator (The Engine): A thread-safe, memory-mapped snapshot store that uses Bitmasking to identify deltas in $O(1)$ time.Egress (The Filter): An event-emitter that only fires if the DeltaMask != 0.

4. Technical Decision Log (TDL)TDL-001: Lock-Free Concurrency ModelDecision: Use java.util.concurrent.ConcurrentHashMap with the compute() function instead of global synchronized blocks.Reasoning: Global locks create a bottleneck at high core counts. compute() ensures that updates to the same ProductID are atomic and thread-safe while allowing updates to different IDs to happen in parallel across all CPU cores.TDL-002: Bitmask-based Delta DetectionDecision: Store field changes in a single int mask.Reasoning: To check if price and stock changed, we don't use string comparisons. We use bitwise OR operations.Price Changed = 0b01Stock Changed = 0b10Both Changed = 0b11This allows the CPU to evaluate "Should I send an event?" in roughly 1 nanosecond.TDL-003: Memory Over-Provisioning vs. GCDecision: Pre-allocate ProductState objects or use primitive arrays.Reasoning: We want to avoid new Object() inside the hot loop. By reusing objects, we keep the Young Generation of the Heap clean, preventing "Stop-the-World" Garbage Collection pauses that would spike our P99 latency.

5. Downstream Protection Strategy
Aegis acts as a Circuit Breaker for Data. It protects the Order Service via:Change Suppression: If a price update comes in but the value is identical to the cache, the event is dropped.Field-Level Targeting: Instead of sending UpdateProductRequest, Aegis sends PriceChangedEvent. The Order Service can then route this directly to a "Price Watcher" cache rather than hitting the main Database.Temporal Collapsing: If 10 updates for the same ID happen within 1ms, Aegis can be configured to "collapse" them into a single final state change before egress.

8. Event Schema (Lightweight): Instead of a heavy "Product" object, Aegis emits "Actionable Deltas."
 Raw Inbound (Typical):
 JSON{
  "id": "SKU-99",
  "name": "Mechanical Keyboard",
  "price": 15000,
  "stock": 45,
  "warehouse": "East-1",
  "lastUpdated": "2026-02-09T13:30:00Z"
}
Aegis Outbound (Actionable):
JSON{
  "id": "SKU-99",
  "type": "PRICE_DECREASE",
  "delta": -500,
  "current": 14500
}

9. Performance Benchmarks (Projected)
   Throughput: $> 1.2$ million updates per second on a 16-core JVM.
   Latency (P99): $< 0.5$ ms from ingress to egress.
   Memory Footprint: $\approx 400$ bytes per product. (1 million products $\approx 400$ MB RAM).

10. Getting Started
   Prerequisites: JDK 21 (for Virtual Threads support) and Maven 3.9+
   Build: mvn clean install
   Run Benchmark: java -jar target/benchmarks.jar
