package ecommerce.repository

import ecommerce.model.Member
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository

@Repository
class MemberRepository(private val jdbc: JdbcTemplate) {
    // for converting SQL res -> Member obj
    private val memberRowMapper =
        RowMapper<Member> { rs, _ ->
            Member(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("role"),
            )
        }

    // fun save
    fun save(member: Member): Member {
        val sql = "INSERT INTO members (email, password, name, role) VALUES (?, ?, ?, ?)"
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbc.update({
            it.prepareStatement(sql, arrayOf("id")).apply {
                setString(1, member.email)
                setString(2, member.password) // Already hashed
                setString(3, member.name)
                setString(4, member.role)
            }
        }, keyHolder)
        return member.copy(id = keyHolder.key!!.toLong())
    }

    fun findByEmail(email: String): Member? {
        val sql = "SELECT * FROM members WHERE email = ?"
        return try {
            jdbc.queryForObject(sql, memberRowMapper, email)
        } catch (_: org.springframework.dao.EmptyResultDataAccessException) {
            null
        }
    }

//    fun findById(id: Long): Member? {
//        val sql = "SELECT * FROM members WHERE id = ?"
//        return try {
//            jdbc.queryForObject(sql, memberRowMapper, id)
//        } catch (_: org.springframework.dao.EmptyResultDataAccessException) {
//            null
//        }
//    }

    fun existsByEmail(email: String): Boolean {
        val sql = "SELECT COUNT(*) FROM members WHERE email = ?"
        val count = jdbc.queryForObject(sql, Int::class.java, email) ?: 0
        return count > 0
    }
}
