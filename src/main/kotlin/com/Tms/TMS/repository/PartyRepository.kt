package com.Tms.TMS.repository

import com.Tms.TMS.model.Party
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
        )
    }

    fun getAlllocation(): List<Party> {
        val sql = "SELECT * FROM party_location"
        return jdbcTemplate.query(sql, rowMapper)
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
}