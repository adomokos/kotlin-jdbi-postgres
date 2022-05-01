package jdbiktpg.cli.db

import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

// ColumnName attribute can define the SQL column for us
data class User(val id: Int, @ColumnName("name") val name: String)

// Declarative API
interface UserDao {
    @SqlUpdate("INSERT INTO users (id, name) VALUES (?, ?)")
    fun insertPositional(id: Int, name: String)

    @SqlBatch("INSERT INTO users (id, name) VALUES (:id, :name)")
    fun insertMany(@BindBean users: List<User>): IntArray

    @SqlUpdate("INSERT INTO users (id, name) VALUES (:id, :name)")
    fun insertNamed(@Bind("id") id: Int, @Bind("name") name: String)

    // @RegisterBeanMapper(User::class)
    @SqlQuery("SELECT * FROM users ORDER BY name")
    fun listUsers(): List<User>
}

/*
abstract class Dao {
    @SqlUpdate("insert into something values('something')")
    abstract fun saveSomething()

    @SqlUpdate("insert into something_else values('something_else')")
    abstract fun saveSomethingElse()

    @Transaction
    fun insertInTransaction() {
        saveSomething()
        saveSomethingElse()
    }
}
*/

