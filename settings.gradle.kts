pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.myket.ir/") }
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.myket.ir/") }
    }
}

rootProject.name = "Matharium"
include(":app")
