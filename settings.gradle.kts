pluginManagement {
    repositories {
        maven(url="http://maven.fabricmc.net/") {
            name = "FabricMC"
        }
        jcenter()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if(requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
rootProject.name = Constants.name