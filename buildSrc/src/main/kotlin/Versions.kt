object Minecraft {
    const val version = "1.14.2"
}

object Kotlin {
    const val version = "1.3.31"
}

object KotlinX {
    object Serialization {
        const val version = "0.11.0"
    }
}

object Fabric {
    object Loader {
        const val version = "0.4.+"
    }
    object Loom {
        const val version = "0.2.4-SNAPSHOT"
    }
    object Yarn {
        const val version = "${Minecraft.version}+build.+"
    }
    object API {
        const val version = "0.3.0+build.+"
    }
    object LanguageKotlin {
        const val version = Kotlin.version + "+build.+"
    }
}

object Fuel {
    const val version = "2.0.1"
}
