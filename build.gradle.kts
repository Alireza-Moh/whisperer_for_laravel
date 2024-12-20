plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "at.alirezamoh.whisperer-for-laravel"
version = "1.1.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        phpstorm("2024.3")
        bundledPlugin("com.jetbrains.php")
        bundledPlugin("com.jetbrains.php.blade")

        instrumentationTools()
    }

    implementation("org.atteo:evo-inflector:1.3")
    implementation("org.freemarker:freemarker:2.3.33")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("243.*")
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
