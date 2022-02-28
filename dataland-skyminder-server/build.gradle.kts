// dataland-skyminder-server

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("com.gorylenko.gradle-git-properties") version "2.4.0"
    id("org.springframework.boot")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    implementation("org.dataland:skyminder-server:0.0.1-SNAPSHOT")
}
