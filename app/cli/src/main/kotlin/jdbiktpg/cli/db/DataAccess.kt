package jdbiktpg.cli.db

import org.jdbi.v3.core.mapper.Nested
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlBatch
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

// ColumnName attribute can define the SQL column for us
data class User(val id: Int, @ColumnName("name") val name: String, val phoneNumbers: List<PhoneNumber> = emptyList())

data class PhoneNumber(
    val id: Int = 0,
    @Nested(value = "user")
    val user: User,

    @ColumnName("phone_number")
    val phoneNumber: String
)

// Declarative API
interface UserDao {
    @SqlUpdate("INSERT INTO users (id, name) VALUES (?, ?)")
    fun insertPositional(id: Int, name: String)

    @SqlBatch("INSERT INTO users (id, name) VALUES (:id, :name)")
    fun insertMany(@BindBean users: List<User>): IntArray

    @SqlUpdate("INSERT INTO users (id, name) VALUES (:id, :name)")
    fun insertNamed(@Bind("id") id: Int, @Bind("name") name: String)

    // This can return the persisted object with new IDs
    @SqlQuery("INSERT INTO users (name) VALUES (:user.name) returning *")
    fun insertObject(user: User): User

    // @RegisterBeanMapper(User::class)
    @SqlQuery("SELECT * FROM users ORDER BY name")
    fun listUsers(): List<User>
}

interface PhoneNumberDao {
    // This can return the persisted object with new IDs
    @SqlUpdate("INSERT INTO phone_numbers (user_id, phone_number) VALUES (:phone_number.user.id, :phone_number.phone_number)")
    fun insertObject(phoneNumber: PhoneNumber)
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
