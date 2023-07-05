package com.cyrax.commuin

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cyrax.commuin.struct.Chat
import com.cyrax.commuin.struct.InventoryDatabase
import com.cyrax.commuin.struct.Message
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//class ExampleUnitTest {
//    @Test
//    fun addition_isCorrect() {
//    //    val str= fetch()
//     //   assertEquals("a", str)
//    }
//}

@RunWith(AndroidJUnit4::class)
class ChatDaoTest {

    private lateinit var chatDao: Chat
    private lateinit var db: InventoryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java).build()
        chatDao = db.chat()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetChat() = runBlocking {
        val message = Message("1", "Hello", "2022-01-01", "12:00", "user1")
        chatDao.insertMsg(message)
        val messages = chatDao.getMsg("user1", "user2").first()
        assertEquals(messages[0], message)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAndGetChat() = runBlocking {
        val message = Message("1", "Hello", "2022-01-01", "12:00", "user1")
        chatDao.insertMsg(message)
        chatDao.deleteMsg("1")
        val messages = chatDao.getMsg("user1", "user2").first()
        assertEquals(messages.size, 0)
    }
}



