import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.localizedLauncherIcon"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // CI and contributors build against IntelliJ IDEA Community.
        // To run the plugin in your local Android Studio, pass its install dir:
        //   ./gradlew runIde -PlocalIdePath="C:\Program Files\Android\Android Studio"
        // (or put localIdePath=... into ~/.gradle/gradle.properties).
        val localIdePath = providers.gradleProperty("localIdePath").orNull
        if (localIdePath != null) {
            local(localIdePath)
        } else {
            intellijIdeaCommunity(providers.gradleProperty("platformVersion"))
        }
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null } // no upper bound: the plugin only uses stable platform APIs
        }
        changeNotes = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText
            .map { it.substringAfter("## ").substringBefore("\n## ").let { notes -> "<pre>## $notes</pre>" } }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
