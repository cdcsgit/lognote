// Optional Gradle build so LogNote can be built from the command line / CI without IntelliJ.
// Usage: gradle fatJar   ->  build/libs/LogNote.jar
plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/flatlaf-3.5.1.jar", "libs/gson-2.13.2.jar"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        kotlin.srcDir("src")
        resources.srcDir("res")
    }
}

tasks.register<Jar>("fatJar") {
    archiveFileName.set("LogNote.jar")
    manifest { attributes["Main-Class"] = "com.blogspot.cdcsutils.lognote.Main" }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) } })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
