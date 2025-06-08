plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "at.alirezamoh.whisperer-for-laravel"
version = "1.3.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        phpstorm("2025.1")
        bundledPlugin("com.jetbrains.php")
        bundledPlugin("com.jetbrains.php.blade")
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("JavaScript")
        pluginVerifier()
    }

    implementation("org.freemarker:freemarker:2.3.33")
}

intellijPlatform {
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("251.*")
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
