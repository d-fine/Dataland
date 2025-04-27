package org.dataland.reducedstack

import freemarker.template.Configuration
import java.io.File
import java.io.FileWriter
import java.io.Writer

fun main() {
    val cfg =
        Configuration(Configuration.VERSION_2_3_31).apply {
            setClassLoaderForTemplateLoading(this::class.java.classLoader, "/")
        }

    val templates =
        mapOf(
            "nginx-conf.ftl" to "./dataland-inbound-admin-proxy/nginx.conf",
            "backendDev.conf.ftl" to "./dataland-inbound-proxy/config/utils/locations/backendDev.conf",
            "common.template.ftl" to "./dataland-inbound-proxy/templates/common.template",
        )

    val selectedServices =
        mapOf(
            "proxy" to true, // always required
            "adminProxy" to true, // always required
            "apiKeyManager" to false,
            "documentManager" to false,
            "backendDb" to true, // always required
            "internalStorage" to true, // always required
            "keycloak" to true, // always required
            "pgadmin" to false,
            "rabbitmq" to true, // always required
            "qaService" to false,
            "userService" to false,
            "batchManager" to false,
            "communityManager" to false,
            "specificationService" to false,
            "emailService" to false,
            "externalStorage" to false,
            "eurodatClient" to false,
            "dataExporter" to false,
            "grafana" to false,
            "loki" to false,
            "alloy" to false,
            "frontendDev" to false, // start the stack in reduced mode only works with a local frontend
        )

    val serviceNames =
        mapOf(
            "proxy" to "proxy",
            "adminProxy" to "admin-proxy",
            "apiKeyManager" to "apiKey-manager",
            "documentManager" to "document-manager",
            "backendDb" to "backend-db",
            "internalStorage" to "internal-storage",
            "keycloak" to "keycloak",
            "pgadmin" to "pgadmin",
            "rabbitmq" to "rabbitmq",
            "qaService" to "qa-service",
            "userService" to "user-service",
            "batchManager" to "batch-manager",
            "communityManager" to "community-manager",
            "specificationService" to "specification-service",
            "emailService" to "email-service",
            "externalStorage" to "external-storage",
            "eurodatClient" to "eurodat-client",
            "dataExporter" to "data-exporter",
            "grafana" to "grafana",
            "loki" to "loki",
            "alloy" to "alloy",
            "frontendDev" to "frontend-dev",
        )

    for ((key, value) in templates) {
        FileWriter(value).use { writer: Writer ->
            cfg.getTemplate(key).process(selectedServices, writer)
        }
    }

    val localContainerFile = File("./localContainer.conf")
    localContainerFile.bufferedWriter().use { writer ->
        selectedServices.filterValues { it }.keys.forEach { serviceKey ->
            writer.write("${serviceNames[serviceKey]}\n")
        }
    }

    println("Config files generated successfully!")
}
