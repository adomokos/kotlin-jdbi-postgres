package jdbiktpg.cli

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.jdbi.v3.sqlobject.transaction.Transaction

// ColumnName attribute can define the SQL column for us
data class User(val id: Int, @ColumnName("name") val name: String)

// Declarative API
interface UserDao {
    @SqlUpdate("INSERT INTO users (id, name) VALUES (?, ?)")
    fun insertPositional(id: Int, name: String)

    @SqlUpdate("INSERT INTO users (id, name) VALUES (:id, :name)")
    fun insertNamed(@Bind("id") id: Int, @Bind("name") name: String)

    // @RegisterBeanMapper(User::class)
    @SqlQuery("SELECT * FROM users ORDER BY name")
    fun listUsers(): List<User>
}

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

class IntroSpec : StringSpec({
    "can use good old positional sql" {
        transaction { handle ->
            handle.execute("INSERT INTO users (id, name) VALUES (?, ?)", 1, "Alice")

            handle.createUpdate("INSERT INTO users (id, name) VALUES (:id, :name)")
                .bindBean(User(2, "John"))
                .execute()

            val users = handle.createQuery("SELECT * FROM users ORDER BY name")
                .mapTo(User::class.java).list()

            users.size shouldBe 2

            users.first().name shouldBe "Alice"
            users[1].name shouldBe "John"

            handle.rollback()
        }
    }

    "can use dao object" {
        transaction { handle ->
            val dao = handle.attach(UserDao::class.java)

            dao.insertPositional(1, "Alice")
            dao.insertNamed(2, "Bob")

            val users = dao.listUsers()

            users.size shouldBe 2

            handle.rollback()
        }
    }

    "can utilize an in-line RowMapper" {
        transaction { handle ->
            handle.execute("INSERT INTO users (id, name) VALUES (?, ?)", 1, "Alice")
            handle.execute("INSERT INTO users (id, name) VALUES (?, ?)", 2, "Bob")

            val users = handle.createQuery("SELECT id, name FROM users ORDER BY id ASC")
                .map { rs, _ctx -> User(rs.getInt("id"), rs.getString("name")) }
                .list()

            users.size shouldBe 2
            users.first().name shouldBe "Alice"

            handle.rollback()
        }
    }

    "can utilize a RowMapper class" {
        transaction { handle ->
            handle.execute("INSERT INTO users (id, name) VALUES (?, ?)", 1, "Alice")
            handle.execute("INSERT INTO users (id, name) VALUES (?, ?)", 2, "Bob")

            val users = handle.createQuery("SELECT id, name FROM users ORDER BY id ASC")
                .mapTo<User>()
                .list()

            users.size shouldBe 2
            users.first().name shouldBe "Alice"

            handle.rollback()
        }
    }
})
