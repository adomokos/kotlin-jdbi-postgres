package jdbiktpg.cli

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jdbiktpg.cli.db.PhoneNumber
import jdbiktpg.cli.db.User
import jdbiktpg.cli.db.UserDao
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.mapper.RowMapperFactory
import org.jdbi.v3.core.result.RowView

fun setUpNestedRelationship(handle: Handle): Map<String, User> {
    val dao = handle.attach(UserDao::class.java)

    val alice = dao.insertObject(User(0, "Alice"))
    val bob = dao.insertObject(User(0, "Bob"))

    (alice.id > 0) shouldBe true

    val alicePhoneNumbers = listOf(
        PhoneNumber(0, "612-324-4932", alice),
        PhoneNumber(0, "425-342-3392", alice)
    )

    val bobPhoneNumbers = listOf(
        PhoneNumber(0, "242-192-9942", bob),
        PhoneNumber(0, "345-382-9592", bob)
    )

    val batch = handle.prepareBatch("INSERT INTO phone_numbers(user_id, phone_number) VALUES(:user_id, :phone_number)")

    (alicePhoneNumbers + bobPhoneNumbers).forEach {
        batch.bind("user_id", it.user!!.id)
            .bind("phone_number", it.phoneNumber)
            .add()
    }

    val counts = batch.execute()

    counts.size shouldBe 4

    return mapOf(
        "alice" to alice,
        "bob" to bob
    )
}

fun factory(type: Class<*>, prefix: String): RowMapperFactory {
    return RowMapperFactory.of(type, KotlinMapper(type, prefix))
}

class NestedSpec : StringSpec({
    "can find nested collections" {
        transaction { handle ->
            val createdUsers = setUpNestedRelationship(handle)

            createdUsers.size shouldBe 2

            val dao = handle.attach(UserDao::class.java)

            val users = handle.createQuery(dao.findUsersWithPhoneNumbersSql())
                .registerRowMapper(factory(User::class.java, "u"))
                .registerRowMapper(factory(PhoneNumber::class.java, "p"))
                .reduceRows(linkedMapOf()) { map: LinkedHashMap<Int, User>, rowView: RowView ->
                    val user = map.computeIfAbsent(rowView.getColumn("u_id", Int::class.javaObjectType)) {
                        rowView.getRow(User::class.java)
                    }

                    if (rowView.getColumn("p_id", Int::class.javaObjectType) != null) {
                        user.addPhone(rowView.getRow(PhoneNumber::class.java))
                    }
                    map
                }
                .toList().map { it.second }

            users.size shouldBe 2

            val allPhoneNumbers = users.fold(mutableListOf<PhoneNumber>()) { coll, u ->
                coll.addAll(u.phoneNumbers)
                coll
            }

            allPhoneNumbers.size shouldBe 4

            handle.rollback()
        }
    }
})
