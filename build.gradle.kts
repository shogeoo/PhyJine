plugins {
    id("java")
    id("application")
}

group = "io.github.shogeo.phyjine"
version = "0.0.0"

application {
    mainClass.set("io.github.shogeo.phyjine.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
