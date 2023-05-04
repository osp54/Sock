plugins {
    id("java")
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

    implementation("com.esotericsoftware:kryo:5.4.0")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}