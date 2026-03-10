package com.example.team_management_backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://ep-morning-block-a1njmnqb-pooler.ap-southeast-1.aws.neon.tech/neondb" +
                    "?sslmode=require" +
                    "&channelBinding=require" +
                    "&preferQueryMode=simple" +
                    "&prepareThreshold=0"
            username = "neondb_owner"
            password = "npg_mxeJcaQMK84v"
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000
        }
        return HikariDataSource(config)
    }
}