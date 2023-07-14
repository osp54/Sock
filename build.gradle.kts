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
    implementation("com.github.anuken.arc:arc-core:v145")
    implementation("com.github.anuken.arc:arcnet:v145")

    implementation("com.alibaba:fastjson:2.0.32")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}

jar {
    doFirst {
        configurations.runtimeClasspath.each { file ->
            def dependencyNotation = dependencies.create(file.absolutePath)
            if (dependencyNotation.group == "com.github.anuken") {
                dependencies.remove(dependencyNotation)
                compileOnly dependencyNotation
            }
        }
    }
}
