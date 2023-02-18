group = "me.txmc"
version = "1.0-SNAPSHOT"
description = ""

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.txmc.me/releases") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven(File(projectDir.parent, "local-repo").toURI().toURL())
}

dependencies {
    implementation(project(":Common"))
    implementation(project(":Mixins"))
    implementation(files("${rootProject.projectDir}/libs/NoteBlockAPI-1.6.1-SNAPSHOT.jar"))
    compileOnly("io.papermc:paper-jar:1.8.8")
    compileOnly("org.projectlombok:lombok:1.18.22")
    compileOnly("com.github.fierioziy.particlenativeapi:ParticleNativeAPI-plugin:3.3.1")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:6.1.4-SNAPSHOT")
    compileOnly("com.onarandombox:multiverse-core:2.5")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    val resources = File("src/main/resources")
    from("META-INF").exclude("maven/*")
    from(resources).include("*")
}

tasks.shadowJar {
    includeEmptyDirs = false
    minimize()
}


tasks.register("debugcopyjar") {
    doFirst {
        val pluginfolder: String by project
        val plugins = File(pluginfolder)
        if (plugins.exists()) {
            val plugin = File(project.buildDir, "libs").listFiles()?.first { it.name.endsWith("-all.jar") }
            println("Found plugin at ${plugin?.absolutePath}")
            if (plugin != null) {
                if (plugin.exists()) {
                    val newFile = File(plugins, plugin.name)
                    if (newFile.exists()) {
                        newFile.delete()
                        println("Deleted old plugin ${newFile.absolutePath}")
                    }
                    plugin.copyTo(newFile, true)
                    println("Copied ${plugin.absolutePath} to ${newFile.absolutePath}")
                }
            } else println("Could not find built plugin jar")
        } else println("The plugins folder provided doesnt exits")
    }
}

tasks.register("normalcopy") {
    val plugin = File(project.buildDir, "libs").listFiles()?.first { it.name.endsWith("-all.jar") }
    println("Found build at ${plugin?.absolutePath}")
    if (plugin != null) {
        if (plugin.exists()) {
            val newFile = File(projectDir.parent, "${rootProject.name}-${project.version}.jar")
            if (newFile.exists()) {
                newFile.delete()
                println("Deleted old build ${newFile.absolutePath}")
            }
            plugin.copyTo(newFile, true)
            println("Copied ${plugin.absolutePath} to ${newFile.absolutePath}")
        }
    }
}

if (project.hasProperty("pluginfolder")) {
    tasks.shadowJar.get().finalizedBy(tasks.getByName("debugcopyjar"))
} else tasks.shadowJar.get().finalizedBy(tasks.getByName("normalcopy"))

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}