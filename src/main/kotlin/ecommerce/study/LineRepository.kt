package ecommerce.study

import org.springframework.data.jpa.repository.JpaRepository

interface LineRepository: JpaRepository<Line, Long> {
    fun findByName(name: String): Line
}