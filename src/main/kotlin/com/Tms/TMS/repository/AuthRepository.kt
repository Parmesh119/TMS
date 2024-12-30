    package com.Tms.TMS.repository

    import com.Tms.TMS.model.User
    import org.springframework.jdbc.core.JdbcTemplate
    import org.springframework.jdbc.core.RowMapper
    import org.springframework.stereotype.Repository

    @Repository
    class AuthRepository(private val jdbcTemplate: JdbcTemplate) {

        private val rowMapper = RowMapper<User> { rs, _ ->
            User(
                id = rs.getString("id"),
                username = rs.getString("username"),
                email = rs.getString("email"),
                passwordHash = rs.getString("passwordHash"),
                role = rs.getString("role"),
                refreshToken = rs.getString("refreshToken")
            )
        }

        fun findByUsername(username: String): User? {
            val sql = "SELECT * FROM users WHERE email = ?"
            return jdbcTemplate.queryForObject(sql, rowMapper, username)
        }

        fun save(user: User): User {
            try {
                val sql = "INSERT INTO users (id, username, email, passwordHash, role, refreshToken) VALUES (?, ?, ?, ?, ?, ?)"
                val answer = jdbcTemplate.update(
                    sql,
                    user.id,
                    user.username,
                    user.email,
                    user.passwordHash,
                    user.role,
                    user.refreshToken
                ) > 0
                return if (answer) {
                    findByUsername(user.username)!!
                } else {
                    throw Exception("Failed to retrieve created user")
                }
            } catch (ex: Exception) {
                print(ex.message)
                throw Exception(" User already exists ")
            }
        }

        fun update(user: User): User {
            try {
                val sql = """
                    UPDATE users
                    SET username = ?, email = ?, passwordHash = ?, role = ?, refreshToken = ?
                    WHERE id = ?
                """.trimIndent()
                val answer = jdbcTemplate.update(
                    sql,
                    user.username,
                    user.email,
                    user.passwordHash,
                    user.role,
                    user.refreshToken,
                    user.id
                ) > 0
                return if (answer) {
                    findByUsername(user.username)!!
                } else {
                    throw Exception("Failed to retrieve updated user")
                }
            } catch (ex: Exception) {
                throw Exception("User not found")
            }
        }
    }
