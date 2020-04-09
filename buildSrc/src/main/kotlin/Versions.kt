object Minecraft {
    const val version = "1.15.2"
}

object Kotlin {
    const val version = "1.3.71"
}

object KotlinX {
    object Serialization {
//        const val version = "0.11.0"
        const val version = "0.20.0"
    }
}

object Fabric {
    object Loader {
        const val version = "0.8.+"
    }
    object Loom {
        const val version = "0.2.7-SNAPSHOT"
    }
    object Yarn {
        const val version = "${Minecraft.version}+build.+"
    }
    object API {
        const val version = "0.5.1+build.+"
    }
    object LanguageKotlin {
        const val version = Kotlin.version + "+build.+"
    }
}

object Fuel {
//    const val version = "2.0.1"
    const val version = "2.2.2"
}
