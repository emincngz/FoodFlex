
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // KSP EKLENTİSİ İÇİN BU SATIR ŞART!
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "FoodFlex"
include(":app")