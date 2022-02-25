package org.dataland.datalandbackend.configuration

import org.dataland.skyminderClient.interfaces.DataConnectorInterface
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.dataland.skyminderClient.connector.SkyminderConnector
import org.springframework.context.annotation.Configuration


@Configuration
class Configuration {
    @Bean
    @Qualifier("DefaultConnector")
    fun getSkyminderConnector(): DataConnectorInterface {
        return SkyminderConnector(
            basePath=System.getenv("SKYMINDER_URL"),
            skyminderUser=System.getenv("SKYMINDER_USER"),
            skyminderPassword=System.getenv("SKYMINDER_PW")
        )
    }
}


