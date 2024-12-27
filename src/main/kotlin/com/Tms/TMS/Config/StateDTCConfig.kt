package com.Tms.TMS.Config

import com.Tms.TMS.Model.StateDTC
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "locations")
class StateDTCConfig {
    var state: List<StateDTC.State> = listOf()
}