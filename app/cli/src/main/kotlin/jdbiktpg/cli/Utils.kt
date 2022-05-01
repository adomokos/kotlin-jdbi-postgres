package jdbiktpg.cli

import mu.KotlinLogging
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.core.statement.SqlLogger
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import java.sql.SQLException

// Example logging
class MyPostgresLogger : SqlLogger {
    private val logger = KotlinLogging.logger {}

    override fun logAfterExecution(context: StatementContext?) {
        logger.info { context?.renderedSql }
    }

    override fun logException(context: StatementContext?, ex: SQLException?) {
        logger.info { ex?.message }
    }
}

object JdbiRepository {
    val jdbi: Jdbi
    init {
        val dbInfo = DbConnectionInfo()

        jdbi = Jdbi.create(
            "jdbc:postgresql://${dbInfo.host}:${dbInfo.port}/${dbInfo.db}",
            dbInfo.user,
            dbInfo.password
        )

        jdbi.setSqlLogger(MyPostgresLogger())

        jdbi.installPlugin(KotlinPlugin())
        jdbi.installPlugin(KotlinSqlObjectPlugin())
    }
}

fun transaction(fn: (Handle) -> Unit) {
    val jdbi = JdbiRepository.jdbi
    val sharedHandle = jdbi.open()

    sharedHandle.use { sh ->
        sh.useTransaction<Exception>(fn)
    }
}
