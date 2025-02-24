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
    mainClass.set("RunnerKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "RunnerKt"  // Указание на главный класс
    }

    // Собираем все зависимости в один JAR (fat jar)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // Добавляем содержимое исходных файлов
    from(sourceSets.main.get().output)
}


