package com.Tms.TMS.repository

import com.Tms.TMS.model.Driver
import com.Tms.TMS.model.Transpotation
import com.Tms.TMS.model.Vehicles
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet

@Repository
class TransportationCompanyRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    private fun transportationCompanyRowMapper(rs: ResultSet): Transpotation {
        return Transpotation(
            id = rs.getString("id"),
            companyName = rs.getString("company_name"),
            pointOfContact = rs.getString("point_of_contact"),
            contactNumber = rs.getString("contact_number"),
            email = rs.getString("email"),
            addressLine1 = rs.getString("address_line_1"),
            addressLine2 = rs.getString("address_line_2"),
            state = rs.getString("state"),
            city = rs.getString("city"),
            pinCode = rs.getString("pin_code"),
            status = rs.getString("status"),
            created_at = rs.getLong("created_at"),
            updated_at = rs.getLong("updated_at"),
            vehicles = emptyList(),
            drivers = emptyList()
        )
    }

    private fun vehicleRowMapper(rs: ResultSet): Vehicles {
        return Vehicles(
            id = rs.getString("id"),
            vehicleNumber = rs.getString("vehicle_number"),
            type = rs.getString("vehicle_type"),
            rcBookUrl = rs.getString("rc_book_url")
        )
    }

    private fun driverRowMapper(rs: ResultSet): Driver {
        return Driver(
            id = rs.getString("id"),
            name = rs.getString("name"),
            contactNumber = rs.getString("contact_number"),
            drivingLicenseUrl = rs.getString("driving_license_url")
        )
    }


    // Get transportation company by id
    fun getTransportationCompanyById(id: String): Transpotation {
        return try {
            val companySql = """
                SELECT *
                FROM transportationcompany
                WHERE id = ?
            """.trimIndent()
            val transpotation = jdbcTemplate.queryForObject(companySql, { rs, _ -> transportationCompanyRowMapper(rs) }, id) ?: throw Exception("Transportation company not found")
            val vehicles = getVechiclesByCompanyId(id)
            val drivers = getDriversByCompanyId(id)
            transpotation.copy(vehicles = vehicles, drivers = drivers)
        } catch (ex: Exception) {
            throw ex
        }
    }

    @Transactional
    // Create transportation company
    fun createTransportationCompany(transpotation: Transpotation): Transpotation {

        if(transpotation.id == null) {
            throw IllegalArgumentException("Transportation company id cannot be null")
        }
        return try {
            val companySql = """
                    INSERT INTO transportationcompany (
                        id, company_name, point_of_contact, contact_number, email, address_line_1, address_line_2, state, city, pin_code, status, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            jdbcTemplate.update(
                companySql,
                transpotation.id,
                transpotation.companyName,
                transpotation.pointOfContact,
                transpotation.contactNumber,
                transpotation.email,
                transpotation.addressLine1,
                transpotation.addressLine2,
                transpotation.state,
                transpotation.city,
                transpotation.pinCode,
                transpotation.status,
                transpotation.created_at,
                transpotation.updated_at
            )

            transpotation.vehicles.forEach { vehicle -> createVehicle(vehicle, transpotation.id) }
            transpotation.drivers.forEach { driver -> createDriver(driver, transpotation.id) }

            getTransportationCompanyById(transpotation.id)
        } catch (ex: Exception) {
            throw ex
        }
    }

    @Transactional
    // Create vehicle
    fun createVehicle(vehicle: Vehicles, companyId: String): Vehicles {
        if(vehicle.id == null) {
            throw IllegalArgumentException("Vehicle id cannot be null")
        }
        return try {
            val vehicleSql = """
                    INSERT INTO vehicles (
                        id, vehicle_number, vehicle_type, rc_book_url, transportationcompanyid
                    ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent()
            jdbcTemplate.update(
                vehicleSql,
                vehicle.id,
                vehicle.vehicleNumber,
                vehicle.type,
                vehicle.rcBookUrl,
                companyId
            )
            getVehicleById(vehicle.id)
        } catch (ex: Exception) {
            throw ex
        }
    }

    // Get vehicle by id
    fun getVehicleById(id: String): Vehicles {
        return try {
            val vehicleSql = """
                SELECT *
                FROM vehicles
                WHERE id = ?
            """.trimIndent()
            jdbcTemplate.queryForObject(vehicleSql, { rs, _ -> vehicleRowMapper(rs) }, id) ?: throw Exception("Vehicle not found")
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun getVechiclesByCompanyId(companyId: String): List<Vehicles> {
        return try {
            val vehicleSql = """
                SELECT *
                FROM vehicles
                WHERE transportationcompanyid = ?
            """.trimIndent()
            jdbcTemplate.query(vehicleSql, { rs, _ -> vehicleRowMapper(rs) }, companyId)
        } catch (ex: Exception) {
            throw ex
        }
    }

    @Transactional
    // Create driver
    fun createDriver(driver: Driver, companyId: String): Driver {
        if(driver.id == null) {
            throw IllegalArgumentException("Driver id cannot be null")
        }
        return try {
            val driverSql = """
                    INSERT INTO drivers (
                        id, name, contact_number, driving_license_url, transportationcompanyid
                    ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent()
            jdbcTemplate.update(
                driverSql,
                driver.id,
                driver.name,
                driver.contactNumber,
                driver.drivingLicenseUrl,
                companyId
            )
            getDriverById(driver.id)
        } catch (ex: Exception) {
            throw ex
        }
    }

    // Get driver by id
    fun getDriverById(id: String): Driver {
        return try {
            val driverSql = """
                SELECT *
                FROM drivers
                WHERE id = ?
            """.trimIndent()
            jdbcTemplate.queryForObject(driverSql, { rs, _ -> driverRowMapper(rs) }, id) ?: throw Exception("Driver not found")
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun getDriversByCompanyId(companyId: String): List<Driver> {
        return try {
            val driverSql = """
                SELECT *
                FROM drivers
                WHERE transportationcompanyid = ?
            """.trimIndent()
            jdbcTemplate.query(driverSql, { rs, _ -> driverRowMapper(rs) }, companyId)
        } catch (ex: Exception) {
            throw ex
        }
    }

    // Update transportation company
    fun updateTransportationCompany(transpotation: Transpotation): Transpotation {
        if (transpotation.id == null) {
            throw IllegalArgumentException("Transportation company ID is required")
        }
        return try {
            val companySql = """
                UPDATE transportationcompany
                SET company_name = ?, point_of_contact = ?, contact_number = ?, email = ?, address_line_1 = ?, address_line_2 = ?, 
                    state = ?, city = ?, pin_code = ?, status = ?, updated_at = ?
                WHERE id = ?
            """.trimIndent()
            jdbcTemplate.update(
                companySql,
                transpotation.companyName,
                transpotation.pointOfContact,
                transpotation.contactNumber,
                transpotation.email,
                transpotation.addressLine1,
                transpotation.addressLine2,
                transpotation.state,
                transpotation.city,
                transpotation.pinCode,
                transpotation.status,
                transpotation.updated_at,
                transpotation.id
            )

            val existingVehicles = getVechiclesByCompanyId(transpotation.id)
            val existingDrivers = getDriversByCompanyId(transpotation.id)

            // 3. Get new vehicles and drivers from the updated transportation company
            val newVehicles = transpotation.vehicles
            val newDrivers = transpotation.drivers

            // 4. Categorize vehicles
            val existingVehicleIds = existingVehicles.mapNotNull { it.id }.toSet()
            val newVehicleIds = newVehicles.mapNotNull { it.id }.toSet()

            val vehiclesToCreate = newVehicles.filter { it.id == null || !existingVehicleIds.contains(it.id) }
            val vehiclesToUpdate = newVehicles.filter { it.id != null && existingVehicleIds.contains(it.id) }
            val vehiclesToDelete = existingVehicles.filter { it.id != null && !newVehicleIds.contains(it.id) }

            // 5. Delete removed vehicles
            if (vehiclesToDelete.isNotEmpty()) {
                val deleteVehicleSql = """
                    DELETE FROM vehicles
                    WHERE id = ?
                """.trimIndent()
                vehiclesToDelete.forEach { vehicle ->
                    jdbcTemplate.update(deleteVehicleSql, vehicle.id)
                }
            }

            // 6. Update existing vehicles
            val updateVehicleSql = """
                UPDATE vehicles
                SET
                    vehicle_number = ?,
                    vehicle_type = ?,
                    rc_book_url = ?
                WHERE id = ?
            """.trimIndent()

            vehiclesToUpdate.forEach { vehicle ->
                jdbcTemplate.update(
                    updateVehicleSql,
                    vehicle.vehicleNumber,
                    vehicle.type,
                    vehicle.rcBookUrl,
                    vehicle.id
                )
            }

            // 7. Create new vehicles
            vehiclesToCreate.forEach { vehicle -> createVehicle(vehicle, transpotation.id) }

            // 8. Categorize drivers
            val existingDriverIds = existingDrivers.mapNotNull { it.id }.toSet()
            val newDriverIds = newDrivers.mapNotNull { it.id }.toSet()

            val driversToCreate = newDrivers.filter { it.id == null || !existingDriverIds.contains(it.id) }
            val driversToUpdate = newDrivers.filter { it.id != null && existingDriverIds.contains(it.id) }
            val driversToDelete = existingDrivers.filter { it.id != null && !newDriverIds.contains(it.id) }

            // 9. Delete removed drivers
            if (driversToDelete.isNotEmpty()) {
                val deleteDriverSql = """
                    DELETE FROM drivers
                    WHERE id = ?
                """.trimIndent()
                driversToDelete.forEach { driver ->
                    jdbcTemplate.update(deleteDriverSql, driver.id)
                }
            }

            // 10. Update existing drivers
            val updateDriverSql = """
                UPDATE drivers
                SET
                    name = ?,
                    contact_number = ?,
                    driving_license_url = ?
                WHERE id = ?
            """.trimIndent()

            driversToUpdate.forEach { driver ->
                jdbcTemplate.update(
                    updateDriverSql,
                    driver.name,
                    driver.contactNumber,
                    driver.drivingLicenseUrl,
                    driver.id
                )
            }

            // 11. Create new drivers
            driversToCreate.forEach { driver ->createDriver(driver, transpotation.id)}
            return getTransportationCompanyById(transpotation.id)
        } catch (ex: Exception) {
            throw ex
        }
    }

    // List transportation companies
    fun listTransportationCompanies(
        search: String,
        status: List<String>,
        page: Int,
        size: Int,
        getAll: Boolean
    ): List<Transpotation> {
        return try {
            val sqlBuilder = StringBuilder("SELECT * FROM transportationcompany WHERE 1 = 1")
            if (search.isNotEmpty()) {
                sqlBuilder.append(" AND company_name ILIKE '%$search%'")
            }

            if (status.isNotEmpty()) {
                sqlBuilder.append(" AND status IN (${status.joinToString(",") { "'$it'" }})")
            }

            if (!getAll) {
                sqlBuilder.append("ORDER BY created_at DESC LIMIT $size OFFSET ${(page - 1) * size}")
            } else {
                sqlBuilder.append(" ORDER BY created_at DESC")
            }

            val companies = jdbcTemplate.query(sqlBuilder.toString(), { rs, _ -> transportationCompanyRowMapper(rs) })
            companies.map { company ->
                val vehicles = getVechiclesByCompanyId(company.id!!)
                val drivers = getDriversByCompanyId(company.id)
                company.copy(vehicles = vehicles, drivers = drivers)
            }
        } catch (ex: Exception) {
            throw ex
        }
    }
}