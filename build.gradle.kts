import java.util.*

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
//tasks.jar {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//}


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
    mainClass.set("MeasureParsingTimeMainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MeasureParsingTimeMainKt"  // Указание на главный класс
    }

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")


}


tasks.register("runDagster") {
    group = "dagster"
    description = "Запускает Dagster webserver и daemon"

    doLast {
        val projectDir = project.projectDir.absolutePath.replace("\\", "/")
        val venvActivate = "$projectDir/dagster/venv/Scripts/activate"
        val pipelineFile = "$projectDir/dagster/pipelines/file_pipeline.py"
        val dagsterHome = "C:/Users/huawei/IdeaProjects/antlr_test_2/dagster/dagster_home"
        File("$dagsterHome/history").mkdirs()

        val dagsterWebserver = ProcessBuilder(
            "cmd", "/c", "start", "cmd", "/k",
            "call \"$venvActivate\" && dagster-webserver -f \"$pipelineFile\" -p 3001"
        )
            .directory(File(projectDir))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val dagsterDaemon = ProcessBuilder(
            "cmd", "/c", "start", "cmd", "/k",
            "call \"$venvActivate\" && dagster-daemon run -w \"$projectDir/dagster/workspace.yaml\""
        )
            .directory(File(projectDir))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        dagsterWebserver.waitFor()
        dagsterDaemon.waitFor()
    }
}