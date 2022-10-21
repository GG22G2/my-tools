plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.9.0"
}

group = "hsb.idea.tool"
version = "1.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.2")
    type.set("IU") // Target IDE Platform

    plugins.set(
        listOf(
            "java",
            "IntelliLang",
            "Spring",
            "SpringBoot",
        )
    )
}

dependencies {
    implementation("net.java.dev.jna:jna:5.10.0")
    implementation("net.java.dev.jna:jna-platform:5.10.0")
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
        options.compilerArgs = listOf(
            "--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED",
        )
    }

    patchPluginXml {
        sinceBuild.set("212.4746.92")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}




