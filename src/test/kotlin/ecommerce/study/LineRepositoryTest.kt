package ecommerce.study

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

/*DataJpaTest = Transaction
    why Transaction?
        to clean or roll back after test
        related to caching, persistent context
 */
@DataJpaTest
class LineRepositoryTest {
    @Autowired
    private lateinit var lines: LineRepository

    @Autowired
    private lateinit var stations: StationRepository

    @Test
    fun test() {
        val line = Line(name = "line1")
        val savedLine = lines.save(line)
        val station = Station(name = "pankow", line = savedLine)
        stations.save(station)
        assertThat(station.id).isNotZero()
        assertThat(station.name).isNotNull()
        assertThat(station.line?.id).isNotZero()
    }

    @Test
    fun test2() {
        val line = Line(name = "line1")
        val station = Station(name = "pankow", lines.save(line))
        val actual = stations.save(station)

        stations.save(station)
        assertThat(actual.id).isNotZero()
        assertThat(station.name).isNotNull()
        assertThat(station.line?.id).isNotZero()
    }


    @Test
    fun test3() {
        val line = Line(name = "line1")
        val station = Station(name = "pankow", lines.save(line))
        //acutal and new = same instance, it shares same id
        val actual = stations.save(station)
        val new = stations.findById(actual.id!!).get()

        station.line = lines.save(line)
        assertThat(new.line?.id).isNotZero()
        assertThat(new.line?.name).isNotNull()
    }

    @Test
    fun test4() {
        val line = Line(name = "line1")
        val station = Station(name = "pankow", lines.save(line))
        val actual = stations.save(station)
        //findByName works too
        val new = stations.findByName(actual.name).get()

        station.line = lines.save(line)
        assertThat(new.line?.id).isNotZero()
        assertThat(new.line?.name).isNotNull()
    }

    @Test
    fun test5_1() {
        val line = Line(name = "line1")
        val station = Station(name = "pankow", line)
        val actual = stations.save(station)
    }

    @Test
    fun test5_2() {
        val line = Line(name = "line1")
        val station = Station(name = "pankow", line)
        val actual = stations.save(station)
    }
}