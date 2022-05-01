package jdbiktpg.cli

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jdbiktpg.cli.db.User
import jdbiktpg.cli.db.UserDao
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.statement.PreparedBatch

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
                .map { rs, _ -> User(rs.getInt("id"), rs.getString("name")) }
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

    "can insert records in bulk" {
        transaction { handle ->
            val batch: PreparedBatch = handle.prepareBatch("INSERT INTO users (id, name) VALUES(:id, :name)")
            for (i in 100..4999) {
                batch.bind("id", i).bind("name", "User:$i").add()
            }
            val counts = batch.execute()

            counts.size shouldBe 4900

            handle.rollback()
        }
    }

    "can insert records in bulk with Dao" {
        transaction { handle ->
            val dao = handle.attach(UserDao::class.java)

            val inserted = dao.insertMany(
                listOf(
                    User(1, "Alice"),
                    User(2, "Bob")
                )
            )

            inserted shouldBe listOf(1, 1)

            handle.rollback()
        }
    }
})
