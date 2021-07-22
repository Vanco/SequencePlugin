import org.jetbrains.changelog.markdownToHTML

plugins {
    java
    id("org.jetbrains.intellij") version "1.1.2"
    id("org.jetbrains.changelog") version "1.1.1"
}

group = "vanstudio"
version = "1.4.1"

repositories {
    jcenter()
}

intellij {
    version.set("2018.2.8")
    pluginName.set("SequenceDiagram")
    plugins.set(listOf(/*"com.intellij.java", */"org.jetbrains.kotlin"))
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
        sinceBuild.set("182")
        untilBuild.set("191.*")
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
        ideVersions.set(listOf("2018.1.7","2018.2.8","2018.3.6","2019.1.4"/*,"2019.2.4","2019.3.5","2020.1.4", "2020.2.4", "2020.3.4", "2021.1.1"*/))
    }


    publishPlugin {
        token.set(System.getenv("PUGLISH_TOKEN"))
    }
}