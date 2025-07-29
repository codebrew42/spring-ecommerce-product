package ecommerce.study

import groovyjarjarantlr4.v4.parse.ANTLRParser
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.Test

@DataJpaTest
class StationRepositoryTest {

    @Autowired
    private lateinit var stations: StationRepository

    /*
    here `save` : insert + save to the cache, not to DB since it's not JDBC

     */
    @Test
    fun save() {
        val expected = Station(name = "pankow", price = 100)
        val actual = stations.save(expected)
        println("expected: $expected")
        assertThat(actual.id).isNotNull()
        assertThat(actual.name).isEqualTo(expected.name)
        assertThat(actual.price).isEqualTo(expected.price)
    }

    @Test
    fun findByName() {
        val expected = stations.save(Station(name = "pankow", price = 150))
        val actual = stations.findByName(expected.name)
        println("expected: $expected")

        // Better approach: Test Optional directly
        assertThat(actual).isPresent()
        assertThat(actual.get().name).isEqualTo(expected.name)
        assertThat(actual.get().price).isEqualTo(expected.price)
        assertThat(actual.get().id).isNotNull()
    }

    @Test
    fun findByName_notFound() {
        val actual = stations.findByName("nonexistent-station")
        
        assertThat(actual).isEmpty()
    }

    /*
 a crucial JPA concept: Persistence Context and Identity Management:

  🧠 What This Comment Means:

  "persistence context (entity manager) : @Id - entity"

  - Persistence Context = JPA's "first-level cache"
  - Maps @Id values → Entity instances
  - One entity per ID in the persistence context

  "since it doesn't handle name"

  - Persistence context only tracks by @Id
  - NOT by other fields like name, price, etc.
  - Only the primary key matters for identity

  "fetching entity from cache by key"

  - When you fetch by ID, JPA checks cache first
  - Returns same object instance if already loaded

     */
    //different from `fun findByName()`
    @Test
    fun identity() {
        // Save a station - it gets ID = 1
        val station1 = stations.save(Station(name = "berlin", price = 200))
        
        // Fetch by ID - JPA returns SAME OBJECT from persistence context
        val station2 = stations.findById(station1.id!!).get()
        
        // Fetch by name - JPA executes NEW QUERY, creates NEW OBJECT
        val station3 = stations.findByName("berlin").get()
        
        // Identity check: station1 and station2 are SAME OBJECT (cached)
        assertThat(station1 === station2).isTrue()
        
        // But station1 and station3 are DIFFERENT OBJECTS (not cached by name)
        assertThat(station1 === station3).isFalse()
        
        // However, they are equal in value
        assertThat(station1 == station3).isTrue()
    }

    @Test
    fun update() {
        val station1 = stations.save(Station(name = "berlin", price = 200)) //save to cache
        stations.flush()
        station1.changeName("oranienburger")    //change database -> sync again your cache
                                                        //update query

        //result: just insert, no select
    }

    @Test
    fun update2() {
        val station1 = stations.save(Station(name = "berlin", price = 200)) //save to cache
        stations.flush()
        station1.changeName("oranienburger")    //change database -> sync again your cache
        stations.flush()

    }

    @Test
    fun update2() {
        val station1 = stations.save(Station(name = "berlin", price = 200))
        station1.changeName("oranienburger")  //change once
        station1.changeName("pankow")           //change twice
        stations.flush()

        //is it update query or not?
        //only one insert or only one select?
        //two insert?
        //one save or one change?

        //res; only one insert
        //why? we have `snapshot` 
    }

    /*

    next topic: snapshot


     */
}
