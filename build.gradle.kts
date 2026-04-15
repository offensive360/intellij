plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.offensive360"
version = "1.1.18"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}

intellij {
    version.set("2022.1")
    type.set("IC")
    plugins.set(listOf())
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("221.0")
        untilBuild.set("")
        changeNotes.set("""
            <h2>1.1.15</h2>
            <ul>
                <li>KeepInvisibleAndDeletePostScan: scans leave no trace on the dashboard</li>
                <li>Inline ExternalScan results (no post-scan fetch)</li>
                <li>Retry with exponential backoff for reliability</li>
                <li>4-hour timeout for very large projects</li>
                <li>Base64 decode for code snippets</li>
                <li>File exclusions aligned with VS and Android Studio plugins</li>
                <li>Tabbed detail view: Details / How to Fix / References</li>
                <li>CWE reference links for each vulnerability type</li>
                <li>Check for Updates action in the Tools menu</li>
            </ul>
        """.trimIndent())
    }
}
