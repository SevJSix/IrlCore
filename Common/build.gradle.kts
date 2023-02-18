plugins {
    java
}

group = "me.txmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.txmc.me/releases") }
}

dependencies {
    compileOnly("io.papermc:paper-jar:1.8.8")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}
