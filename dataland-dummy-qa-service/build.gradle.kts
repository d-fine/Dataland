// dataland-dummy-qa-service

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("org.springdoc.openapi-gradle-plugin")
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.jpa")
}

repositories {
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.testng:testng:7.1.0")
    implementation("junit:junit:4.13.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    implementation("io.cloudevents:cloudevents-api:2.4.1")
    implementation("io.cloudevents:cloudevents-json-jackson:2.4.1")
    implementation("io.cloudevents:cloudevents-core:2.3.0")
    implementation(project(":dataland-backend-utils"))

}
