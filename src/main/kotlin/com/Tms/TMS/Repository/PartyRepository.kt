package com.Tms.TMS.Repository

import com.Tms.TMS.Model.Location
import com.Tms.TMS.Model.Party
import jakarta.servlet.http.Part
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component

@Component
class PartyRepository(private val jdbcTemplate: JdbcTemplate) {
    private val rowMapper = RowMapper { rs, _ ->
        Party(
            id = rs.getString("id"),
            name = rs.getString("name"),
            point_of_contact = rs.getString("point_of_contact"),
            phone = rs.getInt("phone"),
            email = rs.getString("email"),
            address1 = rs.getString("address1"),
            address2 = rs.getString("address2"),
            state = rs.getString("state"),
            district = rs.getString("district"),
            taluka = rs.getString("taluka"),
            city = rs.getString("city"),
            zipcode = rs.getInt("zipcode"),
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
            "INSERT INTO party_location (id, name, point_of_contact, phone, email, address1, address2, state, district, taluka, city, zipcode) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val answer = jdbcTemplate.update(
            sql,
            party.id,
            party.name,
            party.point_of_contact,
            party.phone,
            party.email,
            party.address1,
            party.address2,
            party.state,
            party.district,
            party.taluka,
            party.city,
            party.zipcode
        ) > 0
        if (answer) {
            return true
        }
        return false
    }

    fun updateLocation(id: String): Party? {
        val sql = "UPDATE location SET name = ?, point_of_contact = ?, phone = ?, email = ?, address1 = ?, address2 = ?, state = ?, district = ?, taluka = ?, city = ?, zipcode = ? WHERE id = ?"
        return jdbcTemplate.queryForObject(sql, rowMapper, id)
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