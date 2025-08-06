import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    application
}

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
    // for tree edit distance comparing
    // https://mvnrepository.com/artifact/org.jgrapht/jgrapht-core
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-ext:1.5.2")

    implementation("io.github.bonede:tree-sitter:0.25.3")
    implementation("io.github.bonede:tree-sitter-java:0.23.4")
    // https://mvnrepository.com/artifact/org.jgrapht/jgrapht-io
    implementation("org.jgrapht:jgrapht-io:1.5.2")
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

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        )
}
