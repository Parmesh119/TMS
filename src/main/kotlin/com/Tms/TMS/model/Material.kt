package com.Tms.TMS.model

import java.util.UUID

data class Material (
    val id: String? = UUID.randomUUID().toString(),
    val name: String
)