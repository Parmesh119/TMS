package com.Tms.TMS.model

import org.springframework.data.annotation.Id
import java.util.UUID

data class Material (
    @Id val id: String? = UUID.randomUUID().toString(),
    val name: String
)