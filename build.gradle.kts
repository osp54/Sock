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
    implementation("com.github.Anuken.Arc:arc-core:v143")
    implementation("com.github.Anuken.Arc:arcnet:v143")

    implementation("com.esotericsoftware:kryo:5.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}