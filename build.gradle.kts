plugins {
    id("java-library")
    id("maven-publish")
}

publishing {
    repositories {
        maven {
            name = "sock"
            url = uri("https://maven.pkg.github.com/osp54/Sock")
            credentials {
                username = "osp54"
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
version = "0.9"

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
}

dependencies {
    compileOnly("com.github.anuken.arc:arc-core:v143")
    compileOnly("com.github.anuken.arc:arcnet:v143")

    api("com.esotericsoftware:kryo:5.4.0")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}