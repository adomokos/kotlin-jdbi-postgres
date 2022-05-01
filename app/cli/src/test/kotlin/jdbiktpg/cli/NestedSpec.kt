package jdbiktpg.cli

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jdbiktpg.cli.db.PhoneNumber
import jdbiktpg.cli.db.PhoneNumberDao
import jdbiktpg.cli.db.User
import jdbiktpg.cli.db.UserDao
import org.jdbi.v3.core.Handle

fun setUpNestedRelationship(handle: Handle): Map<String, User> {
    val dao = handle.attach(UserDao::class.java)

    val alice = dao.insertObject(User(0, "Alice"))
    val bob = dao.insertObject(User(0, "Bob"))

    (alice.id > 0) shouldBe true

    val alicePhoneNumbers = listOf(
        PhoneNumber(0, alice, "612-324-4932"),
        PhoneNumber(0, alice, "425-342-3392")
    )

    val bobPhoneNumbers = listOf(
        PhoneNumber(0, bob, "242-192-9942"),
        PhoneNumber(0, bob, "345-382-9592")
    )

    val batch = handle.prepareBatch("INSERT INTO phone_numbers(user_id, phone_number) VALUES(:user_id, :phone_number)")

    (alicePhoneNumbers + bobPhoneNumbers).forEach {
        batch.bind("user_id", it.user.id)
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

class NestedSpec : StringSpec({
    "can find nested collections" {
        transaction { handle ->
            val users = setUpNestedRelationship(handle)

            val pnDao = handle.attach(PhoneNumberDao::class.java)
            val results = pnDao.phoneNumbersForUserId(users["alice"]!!.id)

            results.size shouldBe 2

            results.map { pn -> pn.user.name } shouldBe listOf("Alice", "Alice")

            handle.rollback()
        }
    }
})
