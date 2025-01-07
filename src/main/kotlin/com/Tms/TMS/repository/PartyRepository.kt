package com.Tms.TMS.repository

import com.Tms.TMS.model.Employee
import com.Tms.TMS.model.Party
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
@Repository
class PartyRepository(private val jdbcTemplate: JdbcTemplate) {
    private val rowMapper = RowMapper { rs, _ ->
        Party(
            id = rs.getString("id"),
            name = rs.getString("name"),
            pointOfContact = rs.getString("pointOfContact"),
            contactNumber = rs.getString("contactNumber"),
            email = rs.getString("email"),
            addressLine1 = rs.getString("addressLine1"),
            addressLine2 = rs.getString("addressLine2"),
            state = rs.getString("state"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            city = rs.getString("city"),
            pincode = rs.getInt("pincode"),
            status = rs.getString("status")
        )
    }

    fun listParties(
        search: String,
        page: Int,
        size: Int,
        statuses: List<String>,
        getAll: Boolean
    ): List<Party> {
        try {
            val sqlBuilder = StringBuilder("SELECT * FROM party_location WHERE 1=1")

            if (search.isNotBlank()) {
                sqlBuilder.append(" AND name ILIKE ?")
            }

            if (statuses.isNotEmpty()) {
                sqlBuilder.append(" AND status IN (${statuses.joinToString(",") { "'$it'" }})")
            }

            if (!getAll) {
                sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?")
            } else {
                sqlBuilder.append(" ORDER BY created_at DESC")
            }

            val sql = sqlBuilder.toString()
            val parties = if (search.isNotBlank()) {
                if (getAll) {
                    jdbcTemplate.query(sql, rowMapper, "%$search%")
                } else {
                    val offset = (page - 1) * size
                    jdbcTemplate.query(sql, rowMapper, "%$search%", size, offset)
                }
            } else {
                if (getAll) {
                    jdbcTemplate.query(sql, rowMapper)
                } else {
                    val offset = (page - 1) * size
                    jdbcTemplate.query(sql, rowMapper, size, offset)
                }
            }

            return parties
        } catch (e: Exception) {
            throw e
        }
    }

    fun getLocationById(id: String): Party? {
        return jdbcTemplate.queryForObject("SELECT * FROM party_location WHERE id = ?", rowMapper, id)
    }

    fun createLocation(party: Party): Boolean {
        val sql =
            "INSERT INTO party_location (id, name, pointOfContact, contactNumber, email, addressLine1, addressLine2, state, district, taluka, city, pincode) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val answer = jdbcTemplate.update(
            sql,
            party.id,
            party.name,
            party.pointOfContact,
            party.contactNumber,
            party.email,
            party.addressLine1,
            party.addressLine2,
            party.state,
            party.district,
            party.taluka,
            party.city,
            party.pincode
        ) > 0
        if (answer) {
            return true
        }
        return false
    }

    fun updateLocation(id: String, party: Party): Int {
        return try {
            val sql = """
        UPDATE party_location
        SET name = ?, pointOfContact = ?, contactNumber = ?, email = ?, addressLine1 = ?, addressLine2 = ?, 
            state = ?, district = ?, taluka = ?, city = ?, pincode = ?
        WHERE id = ?
    """.trimIndent()
            return jdbcTemplate.update(
                sql,
                party.name,
                party.pointOfContact,
                party.contactNumber.toString(),
                party.email,
                party.addressLine1,
                party.addressLine2,
                party.state,
                party.district,
                party.taluka,
                party.city,
                party.pincode.toString(),
                id // Ensure this is non-null
            )
        } catch (ex: Exception) {
            throw Exception("Location not found")
        }
    }

    fun deleteLocation(id: String): Boolean {
        val sql = "DELETE FROM party_location WHERE id = ?"
        val answer = jdbcTemplate.update(sql, id) > 0
        if (answer) {
            return true
        }
        return false
    }

    fun deactivateParty(id: String): Party {
        try {
            val updateCount = jdbcTemplate.update(
                "UPDATE party_location SET status = 'inactive' WHERE id = ?",
                id
            )

            if (updateCount == 0) {
                throw ChangeSetPersister.NotFoundException()
            }

            return getLocationById(id) ?: throw Exception("Failed to retrieve updated employee")
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun activateParty(id: String): Party {
        try {
            val updateCount = jdbcTemplate.update(
                "UPDATE party_location SET status = 'active' WHERE id = ?",
                id
            )

            if (updateCount == 0) {
                throw ChangeSetPersister.NotFoundException()
            }

            return getLocationById(id) ?: throw Exception("Failed to retrieve updated employee")
        } catch (ex: Exception) {
            throw ex
        }
    }
}