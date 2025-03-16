plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.stge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library and reflection
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    
    // Kotlin compiler for static analysis
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.9.22")
    // IDE dependencies needed for PSI
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.9.22")
    
    // JavaParser for code parsing
    implementation("com.github.javaparser:javaparser-core:3.25.5")
    
    // JUnit Jupiter for testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
    
    // Mockito for mocking in tests
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

application {
    mainClass.set("com.stge.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.stge.MainKt"
    }
    
    // Include all dependencies in the jar
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.named<JavaExec>("run") {
    if (project.hasProperty("mainClass")) {
        mainClass.set(project.properties["mainClass"] as String)
    }
} 