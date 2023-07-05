package com.cyrax.commuin.sections

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.app.NotificationCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.cyrax.commuin.R
import com.cyrax.commuin.Utils.convertTimestampToDate
import com.cyrax.commuin.Utils.getFileNameAndLength
import com.cyrax.commuin.Utils.splitString
import com.cyrax.commuin.Utils.uriToImageBitmap
import com.cyrax.commuin.functions.deleteFileInDirectory
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.functions.uriToArrPdf
import com.cyrax.commuin.functions.uritoArr
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.documentMime
import com.cyrax.commuin.struct.littleIcons
import com.cyrax.commuin.struct.uploadableEvent
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.Spinner
import com.google.android.gms.actions.NoteIntents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@Composable
fun VisibiltyParent(memberModel: MemberModel){
    var whatToShow = rememberSaveable{ mutableStateOf("Department") }
    var whereWeAre = rememberSaveable{ mutableStateOf("My Org") }.value
    var dept = rememberSaveable{ mutableStateOf("") }.value
    val myOrgObj = memberModel.myOrganisation.collectAsState().value

    GreyCard(Modifier,paddingH = 0.dp , paddingV = 0.dp , shape = RoundedCornerShape(35.dp)) {
        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
            Spacer(Modifier.width(20.dp))
            IconButton(enabled = !memberModel.visibilityStk.isEmpty(),onClick = {
                    whatToShow.value = memberModel.visibilityStk.peek().first;
                    whereWeAre = memberModel.visibilityStk.peek().second
                    memberModel.visibilityStk.pop()

            },modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.ArrowBackIos,null,Modifier.size(30.dp),tint= Color.Black)
            }
            Spacer(Modifier.weight(1f))
            Text(whereWeAre.uppercase() , fontSize = 24.sp , fontWeight = FontWeight.Bold,color = Color.Black)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(50.dp))
        }
        when(whatToShow.value){
            "Department"->{

                LazyColumn {
                    items(myOrgObj.members.keys.toList()){
                        Selectables(memberModel,{
                            memberModel.visibilityStk.push(Pair(whatToShow.value,whereWeAre))
                            whatToShow.value = "Designation";
                            whereWeAre = it
                        },{isSelected->
                            if(isSelected)memberModel.updateVisibleTo(it,"Department") //adding
                            else memberModel.updateVisibleTo(it) // removing
                        } , it ,checked = memberModel.visibleTo.containsKey(it))
                    }
                }
            }
            "Designation" -> {
                LazyColumn {
                    try{
                        items(myOrgObj.members[whereWeAre]!!.keys.toList()){
                        Selectables(memberModel,{
                            dept = whereWeAre
                            memberModel.visibilityStk.push(Pair(whatToShow.value,whereWeAre))
                            whatToShow.value = "Member";
                            whereWeAre = it
                        },{isSelected->
                            if(isSelected)memberModel.updateVisibleTo("$whereWeAre $it","Designation") //adding
                            else memberModel.updateVisibleTo("$whereWeAre $it") // removing
                        } , it , checked = memberModel.visibleTo.containsKey("$whereWeAre $it" ))
                    }
                    }catch (_:NullPointerException){ }

                }
            }
            "Member" -> {
                LazyColumn {
                    try{
                        val temp = myOrgObj.members[dept]!![whereWeAre]!!
                        item{
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(40.dp), horizontalArrangement = Arrangement.Center){
                                Text("Path: ${dept}/${whereWeAre}",color = Color.Black)
                            }

                        }
                        items(temp.keys.toList()){
                            Selectables(memberModel,{
                                memberModel.visibilityStk.push(Pair(whatToShow.value,whereWeAre))
                                whatToShow.value = "Member";
                                whereWeAre = it
                            },{isSelected->
                                if(isSelected)memberModel.updateVisibleTo(it,"Member") //adding
                                else memberModel.updateVisibleTo(it) // removing
                            } , it ,temp[it]!! , memberModel.visibleTo.containsKey(it))

                        }
                    }catch (_:NullPointerException){ }

                }

            }
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Selectables(memberModel: MemberModel , onClick:()->Unit , onChecked:(Boolean)->Unit , txt:String , mem:classOrgMember? = null , checked:Boolean){
    val visibleTo  = memberModel.visibleTo
    var clr  by remember{ mutableStateOf( Color(0xffd4d4d4) ) }
    val clrTransition = animateColorAsState(targetValue = clr).value
    clr = if(checked)Periwinkle else Color(0xffd4d4d4)
    GreyCard(Modifier.clickable { onClick() },paddingH = 20.dp , paddingV = 0.dp , shape = RoundedCornerShape(25.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(clrTransition), verticalAlignment = Alignment.CenterVertically){
            Spacer(modifier = Modifier.width(20.dp))
            if(mem == null) Text(txt.uppercase() , fontSize = 20.sp , fontWeight = FontWeight.Medium, color = Color.Black)
            else {
                TextColumn(Modifier,mem.name,"ID: $txt" , FW1=  FontWeight.Medium , FW2=  FontWeight.Normal , FSize1 = 20.sp , FSize2 = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Checkbox(checked = checked, onCheckedChange = { onChecked(it) ;
            } , colors = CheckboxDefaults.colors() )
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}


@Composable
fun EventView(memberModel: MemberModel , navController2: NavHostController){
    val event = memberModel.currentEventOpened.collectAsState().value
    val scope = rememberCoroutineScope()
    //Log.d("qwerty","$event")
    BackHandler(true) {
        memberModel.setCurr(memberModel.prevCurr)
        navController2.popBackStack()
    }
    val context = LocalContext.current
    var color by remember{ mutableStateOf(Periwinkle) }
    var currExpand by rememberSaveable { mutableStateOf("") }
    val myData = memberModel.myData.collectAsState().value
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(IndigoInk) , horizontalAlignment = Alignment.CenterHorizontally){
        item{
            /*TODO :   Who uploaded details here*/
            Box(){
                var dropDown by rememberSaveable{ mutableStateOf(false) }
                ProfRow(Modifier.clickable { dropDown = true },"Posted By :" , "${event.whoUploaded.name}")
                DropdownMenu(expanded = dropDown, onDismissRequest = { dropDown = false }) {
                    SingleMember(navController2 = navController2, ele = event.whoUploaded , memberModel = memberModel)
                }
            }

            /*TODO :   progressbar*/
            Circle(start = event.Sdate/1000 ,curr = System.currentTimeMillis()/1000/*+20000000*/ , end = event.Edate/1000)

            /*TODO :  Duration */
            GreyCard(Modifier.fillMaxWidth(.95f), paddingH = 0.dp, paddingV = 3.dp,
                backColor = if(currExpand == "Duration")Color.White else Periwinkle, shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("Duration expandLess" ,FS = 20.sp, onClick = {
                    if( currExpand != "Duration") {
                        currExpand = "Duration";
                        color = Color.White
                    } else {currExpand = ""; color = Periwinkle}
                } ,
                    expand = currExpand != "Duration",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "Duration"  , enter = expandVertically() ,exit = shrinkVertically()) {
                    Column(){
                        Row(
                            Modifier .fillMaxWidth() .height(50.dp) .padding(10.dp, 3.dp) .clip(RoundedCornerShape(15.dp))
                                .background( Brush.horizontalGradient( listOf( Color(0xffd4d4d4), Periwinkle ) ))
                            ,
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround){
                            Spacer(Modifier.width(0.dp))
                            Image(painterResource(R.drawable.startline),null,Modifier.size(40.dp))
                            Spacer(Modifier.width(0.dp))
                            Text(convertTimestampToDate(event.Sdate), fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                        }
                        Row(
                            Modifier .fillMaxWidth() .height(50.dp) .padding(10.dp, 4.dp) .clip(RoundedCornerShape(15.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xffd4d4d4), Periwinkle)  ) )
                            ,
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround){
                             Spacer(Modifier.width(0.dp))
                            Image(painterResource(R.drawable.finishh),null,Modifier.size(40.dp))
                            Spacer(Modifier.width(0.dp))
                            Text(convertTimestampToDate(event.Edate), fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                        }
                    }

                }
            }

            /*TODO :   Description here */
            GreyCard(Modifier.fillMaxWidth(.95f), paddingH = 0.dp, paddingV = 3.dp,
                backColor = if(currExpand == "Description" )Color.White else Periwinkle, shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("Description expandLess" ,FS = 20.sp, onClick = {
                    if( currExpand != "Description") {
                        currExpand = "Description";
                        color = Color.White
                        /*TODO WHEN SET IS CLICKED*/
                    } else {currExpand = ""; color = Periwinkle} } ,

                    expand = currExpand != "Description",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "Description"  , enter = expandVertically() ,exit = shrinkVertically()) {
                    Column{
                        GreyCard(Modifier.fillMaxWidth(), paddingH = 10.dp, paddingV = 10.dp, backColor = Color(0xffd4d4d4),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text(event.description,
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(300.dp, 1000.dp),fontSize = 18.sp)
                        }
                    }
                }
            }

            /*TODO :   Attachments */
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH = 0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand == "Attachments")Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {

                Box(Modifier.fillMaxWidth()){
                    RowForEvent("Attachments expandLess" ,FS = 20.sp, onClick = {
                        if( currExpand != "Attachments") {
                            currExpand = "Attachments";
                            color = Color.White
                        } else {
                            currExpand = ""
                            color = Periwinkle
                        } } ,
                        expand = currExpand != "Attachments",H=15.dp,V=0.dp)
                }

                AnimatedVisibility(visible = currExpand == "Attachments"  ,enter = expandVertically() ,exit = shrinkVertically()) {
                    Column(Modifier.heightIn(0.dp,500.dp)){
                        LazyColumn{
                            items(event.docUri){
                                val uri = rememberSaveable{ mutableStateOf<String?>("loading") }
                                var size by  rememberSaveable { mutableStateOf(0)  }
                                if(uri.value!=null && uri.value!= "loading"){
                                    LaunchedEffect(key1 = Unit, block = {size = uriToArrPdf(context, Uri.parse(uri.value))!!.size})
                                }
                                LaunchedEffect(key1 = Unit){
                                    scope.launch(Dispatchers.Default) {
                                        try{
                                            uri.value = memberModel.checkAndFetch(context , it.first , "Document")
                                        }catch(e:Exception){Log.d("qwerty",e.message!!)}
                                    }
                                }
                                    GreyCard( modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            Color.Black.copy(alpha = .3f),
                                            1.dp,
                                            1.dp,
                                            3.dp,
                                            100.dp,
                                            100.dp
                                        )
                                        .clickable {
                                            if (uri.value != null && uri.value != "loading") {
                                                openFileFromByteArray(
                                                    context,
                                                    uriToArrPdf(context, Uri.parse(uri.value))!!,
                                                    it.first,
                                                    documentMime[it.second]!!.first, //Here the mime type and extension are stored in a map
                                                    documentMime[it.second]!!.second
                                                )
                                            } else {
                                                TOAST(context, "Download First")
                                            }
                                        },
                                    paddingH = 10.dp,
                                    paddingV = 4.dp,
                                    shape= RoundedCornerShape(25.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                                        Image(painterResource(littleIcons[it.second]!!),null,Modifier.size(50.dp,50.dp))
                                        Spacer(Modifier.width(10.dp))
                                        TextColumn(Modifier.widthIn(0.dp,240.dp),str1 = it.first , str2 = "${bytesToMegabytes(size.toLong())} MB" ,
                                            FW1 = FontWeight.Normal, FW2 = FontWeight.Light , FSize1 = 15.sp, FSize2 = 14.sp)
                                        Spacer(Modifier.width(5.dp))
                                        IconButton(onClick = {
                                            when(uri.value){
                                                "loading" ->{}
                                                null -> {
                                                    uri.value = "loading"
                                                    scope.launch {
                                                        val pathTo = "Commuin/${myData.profileDetail.organName}_${myData.orgID}/${myData.profileDetail.name}_${myData.memberID}/Events/${event.title}"
                                                        memberModel.downloadAndReturnUri(context,pathTo,
                                                            "${myData.orgID}/Events/docsAndImg/${event.eventID}/${it.first}",
                                                            it.first,it.second,"doc"
                                                        ).collect{
                                                            if(it!=null){
                                                                uri.value = it
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    deleteFileInDirectory(uri.value!!)
                                                    uri.value = null
                                                    size = 0
                                                }
                                            }
                                        }) {
                                            when(uri.value){
                                                "loading" -> Spinner(indicatorSize = 30.dp)
                                                null -> Image(  painterResource(R.drawable.downloa) , null,Modifier.size(25.dp,25.dp))
                                                else -> Image(  painterResource(R.drawable.trash) , null,Modifier.size(25.dp,25.dp))
                                            }

                                        }
                                    }
                                }
                            }
                            items(event.imgUri){
                                val uri = rememberSaveable{ mutableStateOf<String?>("loading") }
                                var size by rememberSaveable{ mutableStateOf(0) }
                                if(uri.value!=null && uri.value!= "loading"){
                                    LaunchedEffect(key1 = Unit, block = {size = uritoArr(Uri.parse(uri.value),context).size})
                                }
                                LaunchedEffect(key1 = Unit){
                                    scope.launch(Dispatchers.Default) {
                                        try{
                                            uri.value = memberModel.checkAndFetch(context, it , "Image")
                                        }catch(e:Exception){Log.d("qwerty",e.message!!)}
                                    }
                                }
                                GreyCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            Color.Black.copy(alpha = .3f),
                                            1.dp,
                                            1.dp,
                                            3.dp,
                                            100.dp,
                                            100.dp
                                        )
                                        .clickable {
                                            if (uri.value != null && uri.value != "loading") {
                                                try {
                                                    openFileFromByteArray(
                                                        context,
                                                        uritoArr(Uri.parse(uri.value), context)!!,
                                                        it,
                                                        "image/*", //Here the mime type and extension are stored in a map
                                                        "png"
                                                    )
                                                } catch (e: Exception) {
                                                    Log.d("qwerty", e.message ?: "Some Error")
                                                }
                                            } else {
                                                TOAST(context, "Download First")
                                            }
                                        },
                                    paddingH = 10.dp,
                                    paddingV = 4.dp,
                                    shape= RoundedCornerShape(25.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                                        when(uri.value){
                                            null-> Image(painterResource(id = R.drawable.image),null,
                                                Modifier
                                                    .size(50.dp, 50.dp)
                                                    .clip(RoundedCornerShape(13.dp)), contentScale = ContentScale.Crop)
                                            else -> AsyncImage(model = uri.value , null,
                                                Modifier
                                                    .size(50.dp, 54.dp)
                                                    .clip(RoundedCornerShape(13.dp)), contentScale = ContentScale.Crop)
                                        }

                                        Spacer(Modifier.width(10.dp))
                                        TextColumn(Modifier.widthIn(0.dp,240.dp),str1 = it , str2 = "${bytesToMegabytes(size.toLong())} MB" ,
                                            FW1 = FontWeight.Normal, FW2 = FontWeight.Bold , FSize1 = 16.sp, FSize2 = 14.sp)
                                        Spacer(Modifier.width(5.dp))
                                        IconButton(onClick = {
                                            when (uri.value) {
                                                "loading" -> {}
                                                null -> {
                                                    uri.value = "loading"
                                                    scope.launch {
                                                        val pathTo = "Commuin/${myData.profileDetail.organName}_${myData.orgID}/${myData.profileDetail.name}_${myData.memberID}/Events/${event.title}"
                                                        memberModel.downloadAndReturnUri(context,pathTo,
                                                            "${myData.orgID}/Events/docsAndImg/${event.eventID}/${it}",
                                                            it,"png","img"
                                                        ).collect{
                                                            if(it!=null){
                                                                uri.value = it
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    deleteFileInDirectory(uri.value!!)
                                                    uri.value = null
                                                    size = 0
                                                }
                                            }
                                        }) {
                                            when(uri.value){
                                                "loading" -> Spinner(indicatorSize = 30.dp)
                                                null -> Image(  painterResource(R.drawable.downloa) , null,Modifier.size(25.dp,25.dp))
                                                else -> Image(  painterResource(R.drawable.trash) , null,Modifier.size(25.dp,25.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }


        }

    }

}

fun setNoteReminder(context: Context, note: String, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    val reminderIntent = Intent(context, NoteReminderReceiver::class.java).apply {
        putExtra("note", note)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        reminderIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager?.setExact(
        AlarmManager.RTC_WAKEUP,
        timeInMillis,
        pendingIntent
    )
}



// Broadcast receiver to handle the note reminder trigger
class NoteReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val note = intent?.getStringExtra("note")

        // Display a notification
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.let {
            val channelId = "note_reminder_channel"
            val channel = NotificationChannel(
                channelId,
                "Event Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Event Reminder")
                .setContentText(note)
                .setSmallIcon(R.drawable.calendar)
                .build()

            notificationManager.notify(0, notification)
        }
    }
}

fun saveTextToNotesApp(text: String, context: Context) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    intent.setPackage("com.google.android.keep") // Package name of the Notes app may vary across devices

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        TOAST(context,"This feature uses Google Keep.Install it to proceed.","Long")
    }
}



