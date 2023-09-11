plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.github.osp54"
version = "1.0"

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register("release", MavenPublication::class) {
            from(components["java"])
            artifactId = "sock"
        }
    }
}

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
}

dependencies {
    compileOnly("com.github.anuken.arc:arc-core:v146")
    compileOnly("com.github.anuken.arc:arcnet:v146")

    implementation("com.alibaba:fastjson:2.0.40")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}