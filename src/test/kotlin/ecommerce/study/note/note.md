JPA Persistence Context (First-Level Cache):

// Persistence Context = Map<ID, Entity>
val persistenceContext = mutableMapOf<Long, Station>()

// When you save:
val station1 = stations.save(Station("berlin", 200))
    // persistenceContext[1L] = station1

// When you findById(1L):
val station2 = stations.findById(1L).get()
    // JPA checks: persistenceContext.containsKey(1L) → YES!  
    // Returns: persistenceContext[1L] → SAME OBJECT as station1

// When you findByName("berlin"):
val station3 = stations.findByName("berlin").get()
    // JPA executes SQL query, creates NEW object
    // Does NOT check persistence context (only works for @Id)

Visual Representation:

┌─────────────────────────────────────┐
│     JPA Persistence Context         │
│                                     │
│  ID: 1L  →  Station@12345           │ ← Same object reference
│                ↑                    │
│                └─── Cached by @Id   │
└─────────────────────────────────────┘
│
┌───────────────────────────────┐
│                               │
▼                               ▼
findById(1L)                 findByName("berlin")
│                               │
▼                               ▼
Returns cached                 Executes SQL,
Station@12345                  creates NEW
Station@67890

🔬 Why This Matters:

Performance Implications:

// FAST - No SQL query, returns cached object
val station2 = stations.findById(1L).get()

// SLOWER - Always executes SQL query  
val station3 = stations.findByName("berlin").get()

Identity vs Equality:

station1 === station2  // TRUE (same object reference)
station1 === station3  // FALSE (different object instances)
station1 == station3   // TRUE (same data/equals())

Memory Management:

- One entity per ID in persistence context
- Prevents duplicate objects for same database row
- Consistency - changes to one reference affect all

🎯 Summary:

The comment explains that JPA's persistence context:
1. Only caches by @Id (primary key)
2. findById() returns cached objects (same reference)
3. findByName() creates new objects (different reference)
4. This is why identity (===) differs but equality (==) is same

This is a fundamental JPA optimization - ensuring entity identity and reducing database queries for ID-based lookups! 🚀


🚨 The Comment is Partially Incorrect

What the comment says:

"here save : insert + save to the cache, not to DB since it's not JDBC"

What actually happens:

JPA DOES write to the database, but the timing is different than direct JDBC.

🔍 What Really Happens in JPA save():

JPA Transaction & Persistence Context Flow:

@Test  // ← @DataJpaTest wraps each test in a transaction
fun save() {
// 1. Transaction begins automatically
val expected = Station(name = "pankow", price = 100)

      // 2. JPA save() does TWO things:
      val actual = stations.save(expected)
      //   a) Stores entity in Persistence Context (cache)
      //   b) Marks it for INSERT (but doesn't execute SQL yet)

      // 3. Entity is in "PERSISTENT" state, cached with generated ID
      assertThat(actual.id).isNotNull()  // ID is generated NOW

      // 4. At test end: Transaction commits → SQL INSERT executes
}

🎯 Key Difference: JPA vs JDBC Timing

| Approach | When SQL Executes                    | Caching             |
  |----------|--------------------------------------|---------------------|
| JDBC     | Immediately on jdbcTemplate.update() | None                |
| JPA      | At transaction commit (or flush)     | Persistence Context |

Visual Timeline:

JPA save() Timeline:
┌─────────────────────────────────────────────────────────────┐
│  Transaction Start                           Transaction End │
│         │                                           │       │
│         ▼                                           ▼       │
│    save() called                               COMMIT       │
│         │                                           │       │
│         ├─ Add to cache                            ├─ SQL   │
│         ├─ Generate ID                             │  INSERT │
│         └─ Mark for INSERT                         │  executes│
│                                                    │         │
│    ← Entity in memory with ID →                   Database   │
└─────────────────────────────────────────────────────────────┘

🔬 Proof: Let's See the SQL Execution

Look at the test logs from our earlier run:
Hibernate:
insert
into
"station info"
(station_name, price, id)
values
(?, ?, default)

The INSERT SQL DID execute! It happened at transaction commit.

🎯 Why This Confusion Exists:

1. Deferred Execution:

// This doesn't immediately execute SQL:
val saved = stations.save(station)

// SQL executes here (transaction commit):
// (at end of @Test method)

2. Write-Behind Caching:

// JPA uses "write-behind" pattern:
// 1. Cache the change
// 2. Execute SQL later (at flush/commit)

// vs JDBC "write-through":
// 1. Execute SQL immediately

3. @DataJpaTest Behavior:

@DataJpaTest
class StationRepositoryTest {

      @Test  // ← Each test runs in its own transaction
      fun save() {
          stations.save(station)  // Cached + marked for INSERT
          // Transaction commits here → SQL INSERT executes
          // Transaction rolls back → No data persists between tests
      }
}

🎯 Corrected Understanding:

What JPA save() actually does:

1. Stores entity in Persistence Context (first-level cache)
2. Generates ID using database sequence/auto-increment
3. Marks entity for INSERT (schedules SQL)
4. Executes SQL at transaction commit/flush
5. Returns cached entity with generated ID

Key Insights:

- JPA IS using database (not just memory)
- SQL execution is deferred, not immediate
- Persistence Context acts as write-behind cache
- @DataJpaTest manages transactions automatically

🔧 Comparison Summary:

// Your JDBC approach (immediate):
val id = jdbcTemplate.update("INSERT...")  // SQL executes NOW
val product = Product(id, name, price)     // Manual object creation

// JPA approach (deferred):
val station = stations.save(Station(...))  // SQL scheduled, ID generated
// SQL executes at commit

The comment should read:
"here save: insert + save to cache, AND schedules DB write (executed at transaction commit)"

The key difference isn't whether it writes to DB, but when it writes! 🎯




Key concept
1. test:  findbyid vs. findbyname
2. compare: isEqualTo vs. isSameAs + findByNamd, findByIdOrNull