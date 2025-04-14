import java.util.*

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}



plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.antlr:antlr4:4.13.2")  // Для поддержки TreeViewer
    implementation("me.tongfei:progressbar:0.9.5")

    implementation("org.slf4j:slf4j-simple:1.7.36") // Добавляем привязку SLF4J
    implementation("guru.nidi:graphviz-java:0.18.1")

    implementation("org.eclipse.jdt:org.eclipse.jdt.core:3.40.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        java {
            srcDir("src/main/java/gen")
        }
    }
}

application {
    mainClass.set("MainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"  // Указание на главный класс
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE


}



tasks.register("runDagsterWebserver") {
    description = "Runs dagster-webserver in a new PowerShell window"
    group = "dagster"

    doLast {
        // Получаем текущую директорию проекта и добавляем папку dagster
        val dagsterDir = File(project.projectDir, "dagster").absolutePath.replace("\\", "\\\\")

        exec {
            // Команда для запуска нового окна PowerShell и выполнения dagster-webserver
            commandLine = listOf(
                "powershell",
                "-Command",
                "Start-Process powershell -ArgumentList '-NoExit -Command cd $dagsterDir; .\\venv\\Scripts\\activate; dagster-webserver -w workspace.yaml -p 3001'"
            )
        }
    }
}

tasks.register("runDagsterDaemon") {
    description = "Runs dagster-daemon in a new PowerShell window"
    group = "dagster"

    doLast {
        // Получаем текущую директорию проекта и добавляем папку dagster
        val dagsterDir = File(project.projectDir, "dagster").absolutePath.replace("\\", "\\\\")

        exec {
            // Команда для запуска нового окна PowerShell и выполнения dagster-daemon
            commandLine = listOf(
                "powershell",
                "-Command",
                "Start-Process powershell -ArgumentList '-NoExit -Command cd $dagsterDir; .\\venv\\Scripts\\activate; dagster-daemon run -w workspace.yaml'"
            )
        }
    }
}

tasks.register("runDagsterAll") {
    description = "Runs both dagster-webserver and dagster-daemon in separate PowerShell windows"
    group = "dagster"
    dependsOn("runDagsterWebserver", "runDagsterDaemon")
}