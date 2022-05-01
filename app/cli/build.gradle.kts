plugins {
    kotlin("jvm")
    application
}

val cliktVersion: String by project
val jdbiVersion: String by project
val postgresqlVersion: String by project

dependencies {
    // Command line processing
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

    // JDBI
    implementation("org.jdbi:jdbi3-core:$jdbiVersion")
    implementation("org.jdbi:jdbi3-sqlobject:$jdbiVersion")
    implementation("org.jdbi:jdbi3-kotlin:$jdbiVersion")
    implementation("org.jdbi:jdbi3-kotlin-sqlobject:$jdbiVersion")
    implementation("org.jdbi:jdbi3-postgres:$jdbiVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
}

application {
    // Define the main class for the application.
    mainClass.set("jdbiktpg.cli.AppKt")
}
