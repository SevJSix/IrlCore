rootProject.name = "IrlCore"

include(":Common", ":Mixins", ":Plugin")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}