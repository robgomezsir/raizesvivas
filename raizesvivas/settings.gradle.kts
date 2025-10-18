pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Raizes Vivas"

include(":app")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:utils")
include(":feature:auth")
include(":feature:family")
include(":feature:member")
include(":feature:relationship")
include(":feature:tree")
include(":feature:gamification")
