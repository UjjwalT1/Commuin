package com.cyrax.commuin.struct

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chatTable")
data class Message(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("keyId")
    val id:Long = 0,

    @ColumnInfo("uniqueID" , index = true)
    val uniqueID:String="",

    @ColumnInfo("msg")
    val msg:String="" ,


    @ColumnInfo("date")
    val date:String="" ,

    @ColumnInfo("time")
    val time:String="" ,

    @ColumnInfo("room")
    val room:String="",   //This is field represents the messages that both sender has shared with receiver

    @ColumnInfo("type")
    val type:String="" ,  //This is field represents type of msg

    @ColumnInfo("extra")
    val extra:String=""   ,//This is field represents type of msg

    @ColumnInfo("extension")
    val extn:String=""
    )

@Dao
interface Chat{
    @Insert
    suspend fun insertMsg(message:Message)

    @Query("DELETE FROM chatTable WHERE uniqueID = :msgID")
    suspend fun deleteMsg(msgID:String)

    @Query("SELECT * FROM chatTable WHERE room = :room1 OR room = :room2 ORDER BY keyId DESC LIMIT 1 ") //Qeury just to get the single latest message
    fun getMsg(room1:String , room2:String):Flow<Message?>

    @Query("SELECT COUNT(*) FROM chatTable WHERE room = :room1")
    suspend fun getMsgCount(room1:String):Long

    @Query("SELECT * FROM (SELECT * FROM chatTable WHERE room = :room1 OR room = :room2 ORDER BY keyId DESC LIMIT 100)Var1 ORDER BY keyId ASC LIMIT 99") //Fetches last 100 latest message
    fun populateMsg(room1:String , room2:String): List<Message>
}

@Entity(tableName = "chatTableGroup")
data class MessageGrp(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("keyId") val id:Long = 0,

    @ColumnInfo("uniqueID" , index = true) val uniqueID:String="",
    @ColumnInfo("msg")  val msg:String="" ,
    @ColumnInfo("date") val date:String="" ,
    @ColumnInfo("time") val time:String="" ,
    @ColumnInfo("grpID") val grpID:String="",   //This is field represents the messages that both sender has shared with receiver
    @ColumnInfo("type") val type:String="" ,  //This is field represents type of msg
    @ColumnInfo("extra") val extra:String=""   ,//This is field represents type of msg
    @ColumnInfo("extension") val extn:String="",
    @ColumnInfo("whoSendID") val whoSendID:String="",
    @ColumnInfo("whoSendDept") val whoSendDept:String="",
    @ColumnInfo("whoSendDesig") val whoSendDesig:String="",
)

@Dao
interface GroupChat{
    @Insert
    suspend fun insertMsg(message:MessageGrp)

    @Query("DELETE FROM chatTable WHERE uniqueID = :msgID")
    suspend fun deleteMsg(msgID:String)

    @Query("SELECT * FROM chatTableGroup WHERE grpID = :grpID ORDER BY keyId DESC LIMIT 1 ") //Qeury just to get the single latest message
    fun getMsg(grpID:String):Flow<MessageGrp?>

    @Query("SELECT COUNT(*) FROM chatTableGroup WHERE grpID = :grpID")
    suspend fun getMsgCount(grpID:String):Long

    @Query("SELECT * FROM (SELECT * FROM chatTableGroup WHERE grpID = :grpID ORDER BY keyId DESC LIMIT 100)Var1 ORDER BY keyId ASC LIMIT 99") //Fetches last 100 latest message
    fun populateMsg(grpID:String ): List<MessageGrp>
}

/**
 * This Data class represents the the details of a member that is related to the organisation.
 *  The content of this class will be used to create a UI form some 3rd Person user.
 * Eg: In whatsapp we can view their profile and see their name and other details.
 */
@Entity(tableName = "chatList")
data class ChatList(
    @ColumnInfo("timeStamp") val timestamp:Long = 0,
    @PrimaryKey @ColumnInfo("memberID") val memberID:String="",
    @ColumnInfo("orgName") var orgName:String="",
    @ColumnInfo("name") var name:String="",
    @ColumnInfo("email")var email:String="",
    @ColumnInfo("contact")var contact:String = "",
    @ColumnInfo("designation") var designation:String="",
    @ColumnInfo("dept") var dept:String="",
    @ColumnInfo("profilePath")var path :String="null"
)

@Dao
interface PeopleList{
    @Query("DELETE FROM chatList WHERE memberID = :memberID")
    fun deletePersonFromList(memberID:String)

    @Query("SELECT * FROM chatList ORDER BY timeStamp")
    fun getChatList(): Flow<List<ChatList>>
}

/**
 * Database class with a singleton INSTANCE object.
 */
@Database(entities = [Message::class,ChatList::class , ImageEntity::class, DocEntity::class,SavedMember::class,SavedOrganisation::class,MessageGrp::class], version = 1, exportSchema = false)
abstract class db : RoomDatabase() {

    abstract fun chat(): Chat
    abstract  fun peopleList():PeopleList
    abstract fun image():ImageDao
    abstract fun doc():DocDao
    abstract fun savedMem():savedMemDao
    abstract fun savedOrd():savedOrgDao
    abstract fun groupChat():GroupChat


    companion object {
        @Volatile
        private var Instance: db? = null

        fun getDatabase(context: Context): db {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, db::class.java, "database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

