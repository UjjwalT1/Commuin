package com.cyrax.commuin.sections

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.cyrax.commuin.R
import com.cyrax.commuin.struct.ChatList
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.MessageToSend
import com.cyrax.commuin.struct.Notification
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.toChatList
import kotlinx.coroutines.async
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@Composable
fun SendImg(memberModel: MemberModel, context : Context = LocalContext.current ,txt:String="") {
   // var txt by rememberSaveable{ mutableStateOf("")}
    val me = memberModel.myData.collectAsState().value
    val recipient: classOrgMember? = memberModel.currentChatOpened.collectAsState().value
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }
    if (imageUri != null && recipient!=null) {
        val temp = imageUri!!
        imageUri = null
        LaunchedEffect(key1 = temp) {
            val timeStuff = getTime()
            memberModel.myDataAsSender = me.toChatList(timeStuff.TS)
            val msgObj = MessageToSend( timeStuff.uniqueID, txt, timeStuff.date, timeStuff.time, memberModel.myData.value.memberID + " " + recipient.memberID,
                "--::--img",
                extra = "COMM_IMG_${timeStuff.uniqueID}"
            )
            async {
                memberModel.addImage( temp, context, "",
                    "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Images/Sent",
                    "COMM_IMG_${timeStuff.uniqueID}",
                    "${me.orgID}/${me.memberID}/images/COMM_IMG_${timeStuff.uniqueID}",
                    extension = "png",
                    filetype = "img"
                )
            }.await()
            val personObj = ChatList(timestamp = timeStuff.TS, memberID = recipient.memberID, orgName = recipient.organName, name = recipient.name, recipient.email, recipient.contact, recipient.designation, recipient.dept, path = ""
            )
            memberModel.sendMsg(msgObj)
            memberModel.sendNotification(Notification("","Unread",timeStuff.TS,"${me.memberID} ${me.dept} ${me.designation}",me.profileDetail.name,"-- -- --","NMsg"),"${me.orgID}/${recipient.memberID}/Notifications",recipient,me)
            memberModel.addPersonToServerList(personObj)
        }
    }
    IconButton(onClick = { launcher.launch("image/*") }) {
        Image(painter = painterResource(id = R.drawable.attach_image) , null , modifier = Modifier.size(48.dp))
    }
}


@Composable
fun SendDoc(memberModel: MemberModel, context : Context = LocalContext.current ,txt:String="") {
    // var txt by rememberSaveable{ mutableStateOf("")}
    val me = memberModel.myData.collectAsState().value
    val recipient: classOrgMember? = memberModel.currentChatOpened.collectAsState().value
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? -> imageUri = uri }
    if (imageUri != null && recipient!=null) {
        val temp = imageUri!!
        imageUri = null
        LaunchedEffect(key1 = temp) {
            val timeStuff = getTime()
            memberModel.myDataAsSender = me.toChatList(timeStuff.TS)
            val msgObj = MessageToSend( timeStuff.uniqueID, txt, timeStuff.date, timeStuff.time, memberModel.myData.value.memberID + " " + recipient.memberID,
                "--::--doc", extra = "COMM_DOC_${timeStuff.uniqueID}" ,
                extn = getFileExtension(temp.toString())!!
            )
            async {
                memberModel.addImage( temp, context, "",
                    "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/Sent",
                    "COMM_DOC_${timeStuff.uniqueID}",
                    "${me.orgID}/${me.memberID}/documents/COMM_DOC_${timeStuff.uniqueID}",
                    extension = getFileExtension(temp.toString())!!,
                    filetype = "doc"
                )
            }.await()
            val personObj = ChatList(timestamp = timeStuff.TS, memberID = recipient.memberID, orgName = recipient.organName, name = recipient.name, recipient.email, recipient.contact, recipient.designation, recipient.dept, path = ""
            )
            memberModel.sendMsg(msgObj)
            memberModel.sendNotification(Notification("","Unread",timeStuff.TS,"${me.memberID} ${me.dept} ${me.designation}",me.profileDetail.name,"-- -- --","NMsg"),"${me.orgID}/${recipient.memberID}/Notifications",recipient,me)
            memberModel.addPersonToServerList(personObj)
        }
    }
    IconButton(onClick = { launcher.launch(arrayOf("application/pdf","application/msword","application/vnd.ms-excel", "application/vnd.ms-powerpoint", "text/csv",)) }) {
        Image(painter = painterResource(id = R.drawable.document) , null , modifier = Modifier.size(48.dp))
    }
}



fun getFileExtension(url: String): String? {
    val uri = Uri.parse(url)
    val path = uri.lastPathSegment
    val extension = path?.substringAfterLast(".", missingDelimiterValue = "")
    return if (extension.isNullOrEmpty()) null else extension
}




fun openFileFromByteArray(context: Context, byteArray:ByteArray, id :String  , mimeType:String , suffix:String){
    val tempFile = File.createTempFile(id, suffix, context.cacheDir)
    tempFile.outputStream().use { outputStream ->
        outputStream.write(byteArray)
    }
    val uri = FileProvider.getUriForFile(context, "com.cyrax.commuin.fileprovider", tempFile)
    Log.d("qwerty","  - --  $uri")
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri,  mimeType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try{
        startActivity(context,intent,null)
    }catch(e:Exception){
        Log.e("ERROR", e.message!!)
    }

}


fun bytesToMegabytes(bytes: Long): String {
    val megabyte = 1024 * 1024
    val megabytes = bytes.toDouble() / megabyte
    return String.format("%.3f", megabytes)
}
