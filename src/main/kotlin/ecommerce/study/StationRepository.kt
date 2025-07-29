package ecommerce.study

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StationRepository : JpaRepository<Station, Long> {
    fun findByName(name: String): Optional<Station>
}
