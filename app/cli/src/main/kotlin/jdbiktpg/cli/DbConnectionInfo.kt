package jdbiktpg.cli

data class DbConnectionInfo(
    val host: String,
    val port: Int,
    val db: String,
    val user: String,
    val password: String
) {
    constructor() : this(
        System.getenv("POSTGRES_HOST"),
        Integer.parseInt(System.getenv("POSTGRES_PORT")),
        System.getenv("POSTGRES_DB"),
        System.getenv("POSTGRES_USER"),
        System.getenv("POSTGRES_PASSWORD")
    )
}