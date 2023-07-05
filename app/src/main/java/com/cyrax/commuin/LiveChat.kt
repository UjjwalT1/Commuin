package com.cyrax.commuin

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cyrax.commuin.struct.LiveMsgModel
import com.cyrax.commuin.struct.MemberModel
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.FrameType
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID


@Serializable data class Message2(val id:String ="temp", val txt:String="temp", val sender:String="temp")
@Serializable data class Room(val msgsList:MutableList<Message2> = mutableListOf())

val BaseUrl = "ws://192.168.30.188:8080/chat"

interface RealtimeMessagingClient{
    fun getMessages():Flow<Room>
    suspend fun send(msg:Message2)
    suspend fun close()
}

class KtorRTimeMsgClient(private val client:HttpClient) : RealtimeMessagingClient{

    private var session : WebSocketSession?= null
    override fun getMessages(): Flow<Room> {
        return flow{
            session = client.webSocketSession {
                url(BaseUrl)
            }
            val list = session!!
                .incoming
                .consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .mapNotNull {
                    Json.decodeFromString<Room>(it.readText())
                }

            emitAll(list)
        }
    }

    override suspend fun send(msg: Message2) {
        session?.outgoing?.send(
            Frame.Text(Json.encodeToString(msg))
        )
    }

    override suspend fun close() {
        session?.close()
        session = null
    }

}


@Composable
fun Live(memberModel: MemberModel,liveMsgModel: LiveMsgModel){
    Log.d("httpreq","Msg : "+liveMsgModel.state.collectAsState().value.toString())
    if(liveMsgModel.connError.collectAsState().value)  Log.d("httpreq","Error ")
    if(liveMsgModel.Connecting.collectAsState().value)  Log.d("httpreq","Connecting ")

    var str  by remember { mutableStateOf("") }
    val name =  memberModel.myData.collectAsState().value.profileDetail.name
    Column(Modifier.fillMaxSize()) {
        TextField(value = str, onValueChange = {str = it}, Modifier.fillMaxWidth())
        Button(onClick = {  try{
            liveMsgModel.sendMessage(   Message2(UUID.randomUUID().toString(),str,name)   )
        } catch(e:Exception){Log.d("live",e.message?:"NULL")} } , Modifier.align(Alignment.CenterHorizontally)){
            Text("Send")
        }
    }

}