plugins {
    java
    application
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
    maven("https://repo.spring.io/milestone/")
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

val modules = mapOf(
    "logback-classic" to "ch.qos.logback.classic",
    "logback-core" to "ch.qos.logback.core",
    "slf4j-api" to "org.slf4j",
    // jackson
    "jackson-annotations" to "com.fasterxml.jackson.annotation",
    "jackson-core" to "com.fasterxml.jackson.core",
    "jackson-databind" to "com.fasterxml.jackson.databind",
    "jackson-datatype-jdk8" to "com.fasterxml.jackson.datatype.jdk8",
    "jackson-datatype-jsr310" to "com.fasterxml.jackson.datatype.jsr310",
    "jackson-module-parameter-names" to "com.fasterxml.jackson.module.paramnames",
    // jakarta
    "jetty-jakarta-servlet-api" to "jetty.servlet.api",
    "jetty-jakarta-websocket-api" to "jetty.websocket.api",
    // jetty annotations
    "annotation-api" to "jakarta.annotation",
    "asm" to "org.objectweb.asm",
    // jetty websocket
    "websocket-core-client" to "org.eclipse.jetty.websocket.core.client",
    "websocket-core-common" to "org.eclipse.jetty.websocket.core.common",
    "websocket-core-server" to "org.eclipse.jetty.websocket.core.server",
    "websocket-jakarta-client" to "org.eclipse.jetty.websocket.jakarta.client",
    "websocket-jakarta-common" to "org.eclipse.jetty.websocket.jakarta.common",
    "websocket-jakarta-server" to "org.eclipse.jetty.websocket.jakarta.server",
    "websocket-jetty-api" to "org.eclipse.jetty.websocket.jetty.api",
    "websocket-jetty-common" to "org.eclipse.jetty.websocket.jetty.common",
    "websocket-jetty-server" to "org.eclipse.jetty.websocket.jetty.server",
    "websocket-servlet" to "org.eclipse.jetty.websocket.servlet",
    // jetty
    "jetty-alpn-client" to "org.eclipse.jetty.alpn.client",
    "jetty-annotations" to "org.eclipse.jetty.annotations",
    "jetty-client" to "org.eclipse.jetty.client",
    "jetty-http" to "org.eclipse.jetty.http",
    "jetty-io" to "org.eclipse.jetty.io",
    "jetty-jndi" to "org.eclipse.jetty.jndi",
    "jetty-plus" to "org.eclipse.jetty.plus",
    "jetty-security" to "org.eclipse.jetty.security",
    "jetty-server" to "org.eclipse.jetty.server",
    "jetty-servlet" to "org.eclipse.jetty.servlet",
    "jetty-servlets" to "org.eclipse.jetty.servlets",
    "jetty-util" to "org.eclipse.jetty.util",
    "jetty-webapp" to "org.eclipse.jetty.webapp",
    "jetty-xml" to "org.eclipse.jetty.xml",
    // log4j
    "log4j-api" to "org.apache.logging.log4j",
    // snakeyaml
    // "snakeyaml" to "org.yaml.snakeyaml",
)

// ext["snakeyaml.version"] = "1.34-SNAPSHOT"

application {
    mainClass.set("com.example.loomservlet.ServletOfTheLoomApplication")
    applicationDefaultJvmArgs = listOf("--enable-preview", "--add-modules=jdk.incubator.concurrent")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = listOf("--enable-preview", "--add-modules=jdk.incubator.concurrent")
}

tasks.named<JavaExec>("run") {
    jvmArgs("--enable-preview", "-Xlog:class+path=info")
}

configurations.configureEach {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("jakarta.servlet:jakarta.servlet-api"))
                .using(module("org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2"))
            substitute(module("jakarta.websocket:jakarta.websocket-api"))
                .using(module("org.eclipse.jetty.toolchain:jetty-jakarta-websocket-api:2.0.0"))
        }
    }
}

dependencies {
    // required by org.eclipse.jetty.annotations
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("org.ow2.asm:asm:9.4")

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}

tasks.bootRun {
    jvmArgs("--enable-preview")
    // sprint boot uses an internal jetty class
    jvmArgs("--add-opens", "org.eclipse.jetty.websocket.jakarta.server/org.eclipse.jetty.websocket.jakarta.server.internal=ALL-UNNAMED")

    // split by modules
    val (moduleCollection, classCollection) = classpath.partition { cp -> modules.keys.any { cp.name.contains(it) } }
    println("${moduleCollection.size} modular jars")
    println("${classCollection.size} classpath jars")
    jvmArgs("-p", moduleCollection.joinToString(separator = ";"), "--add-modules", modules.values.joinToString(separator = ","))
    classpath = project.layout.files(classCollection)
}
