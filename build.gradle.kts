plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    `maven-publish`
    signing
}

group = "com.shopsavvy"
version = "1.0.0"
description = "Official Kotlin/JVM SDK for ShopSavvy Data API - Access product data, pricing, and price history"

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Test Dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("ShopSavvy Data API Kotlin SDK")
                description.set("Official Kotlin SDK for ShopSavvy Data API - Access product data, pricing, and price history")
                url.set("https://shopsavvy.com/data")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        name.set("ShopSavvy Team")
                        email.set("business@shopsavvy.com")
                        organization.set("ShopSavvy by Monolith Technologies, Inc.")
                        organizationUrl.set("https://shopsavvy.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/shopsavvy/sdk-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com:shopsavvy/sdk-kotlin.git")
                    url.set("https://github.com/shopsavvy/sdk-kotlin/tree/main")
                }
            }
        }
    }
    
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}

// For manual upload to Central Portal, build the bundle
tasks.register<Zip>("createCentralBundle") {
    dependsOn("publishToMavenLocal")
    archiveFileName.set("central-bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("central-publishing"))
    
    from(layout.buildDirectory.dir("publications/maven")) {
        include("**/*")
    }
}