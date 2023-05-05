plugins {
    id("java-library")
    id("maven-publish")
}

publishing {
    repositories {
        maven {
            name = "Sock"
            url = uri("https://maven.pkg.github.com/osp54/Sock")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

group = "com.ospx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
}

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:v143")
    compileOnly("com.github.Anuken.Arc:arcnet:v143")

    api("com.esotericsoftware:kryo:5.4.0")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}