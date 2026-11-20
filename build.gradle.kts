import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.localizedlaunchericon"
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
    // The plugin has no Settings pages, so there is nothing to index. Disabling it here (rather than
    // disabling the task) also skips prepareJarSearchableOptions, which fails on a clean build otherwise.
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null } // no upper bound: the plugin only uses stable platform APIs
        }
        changeNotes = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText
            .map { it.substringAfter("## ").substringBefore("\n## ").let { notes -> "<pre>## $notes</pre>" } }
    }
}

ktlint {
    version.set("1.5.0")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // The plugin runs on the Kotlin stdlib bundled with the IDE (see kotlin.stdlib.default.dependency),
        // and 2024.2 (sinceBuild 242) ships stdlib 1.9. Compiling against a newer API level makes the
        // generated code call stdlib classes that do not exist there (e.g. SpillingKt -> NoClassDefFoundError).
        apiVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
