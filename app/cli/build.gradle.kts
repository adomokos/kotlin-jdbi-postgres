plugins {
    kotlin("jvm")
    application
}

val cliktVersion: String by project

dependencies {
    // Command line processing
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
}

application {
    // Define the main class for the application.
    mainClass.set("jdbiktpg.cli.AppKt")
}
