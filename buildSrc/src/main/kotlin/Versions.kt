object Minecraft {
    const val version = "19w12a"
}

object Kotlin {
    const val version = "1.3.21"
}

object KotlinX {
    object Serialization {
        const val version = "0.10.0"
    }
}

object Fabric {
    const val version = "0.3.7.+"
    object Loom {
        const val version = "0.2.0-SNAPSHOT"
    }
    object Yarn {
        const val version = "+"
    }
    object FabricAPI {
        const val version = "0.2.5.+"
    }
    object LanguageKotlin {
        const val version = Kotlin.version + "-SNAPSHOT"
    }
}

object Fuel {
    const val version = "2.0.1"
}
