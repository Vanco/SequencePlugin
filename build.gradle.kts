import org.jetbrains.changelog.markdownToHTML

plugins {
    java
    id("org.jetbrains.intellij") version "1.1.2"
    id("org.jetbrains.changelog") version "1.1.1"
}

group = "vanstudio"
version = "2.1.0"

repositories {
    jcenter()
}

intellij {
    version.set("2020.1")
    pluginName.set("SequenceDiagram")
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin"))
    updateSinceUntilBuild.set(true)
    sandboxDir.set("${project.rootDir}/.sandbox")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    patchPluginXml {
        sinceBuild.set("201")
        untilBuild.set("212.*")
        pluginDescription.set(
            File(projectDir, "README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )
        changeNotes.set(provider { changelog.getLatest().toHTML() })
    }

    runPluginVerifier {
        ideVersions.set(listOf("2020.1.4", "2020.2.4", "2020.3.4", "2021.1.1"))
    }


    publishPlugin {
        token.set(System.getenv("PUGLISH_TOKEN"))
    }
}