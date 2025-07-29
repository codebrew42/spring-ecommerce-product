package ecommerce.repository

import ecommerce.model.Member
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository

/**
 * Data access layer for Member entity using Spring JDBC
 * Implements Step 2-2 requirement: member data persistence
 * Handles member storage, retrieval, and existence checks
 * Works with members table containing authentication data
 */
@Repository
class MemberRepository(private val jdbc: JdbcTemplate) {
    /**
     * RowMapper for converting SQL ResultSet rows to Member objects
     * Maps database columns (id, email, password, name, role) to Member data class
     * Password field contains hashed password with salt from PasswordService
     * Used across all member query operations for consistent object construction
     */
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

    /**
     * Inserts a new member into the database and returns it with generated ID
     * Uses GeneratedKeyHolder to capture auto-generated ID from H2 database
     * Stores hashed password received from PasswordService (not plain text)
     * Returns copy of input member with the database-generated ID
     * ID generation handled by H2's AUTO_INCREMENT column definition
     */
    fun save(member: Member): Member {
        val sql = "INSERT INTO members (email, password, name, role) VALUES (?, ?, ?, ?)"
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbc.update({
            it.prepareStatement(sql, arrayOf("id")).apply {
                setString(1, member.email)
                setString(2, member.password)
                setString(3, member.name)
                setString(4, member.role)
            }
        }, keyHolder)
        return member.copy(id = keyHolder.key!!.toLong())
    }

    /**
     * Finds a member by email address for authentication
     * Uses parameterized query to prevent SQL injection
     * Returns null instead of throwing exception when member not found
     * Handles Spring JDBC's EmptyResultDataAccessException gracefully
     * Used by MemberService.authenticate() for login validation
     */
    fun findByEmail(email: String): Member? {
        val sql = "SELECT * FROM members WHERE email = ?"
        return try {
            jdbc.queryForObject(sql, memberRowMapper, email)
        } catch (_: org.springframework.dao.EmptyResultDataAccessException) {
            null
        }
    }

    /**
     * Checks if a member with the given email already exists
     * Used by MemberService.register() to prevent duplicate email registrations
     * Executes COUNT query which is more efficient than SELECT for existence checks
     * Returns true if any members have the specified email, false otherwise
     * Enforces email uniqueness constraint at application level
     */
    fun existsByEmail(email: String): Boolean {
        val sql = "SELECT COUNT(*) FROM members WHERE email = ?"
        val count = jdbc.queryForObject(sql, Int::class.java, email) ?: 0
        return count > 0
    }
}
