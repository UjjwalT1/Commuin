package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cyrax.commuin.R
import com.cyrax.commuin.Utils.convertTimestampToDate
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Notification
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.greyPeriList
import com.cyrax.commuin.struct.notifn
import com.cyrax.commuin.struct.uploadableEvent
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.RainForest
import com.cyrax.commuin.ui.theme.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FeedHolder(memberModel: MemberModel = viewModel(),navController2:NavHostController = rememberNavController()){
    var str by remember{ mutableStateOf("This is a sample description that represents that here is something that is wrtten, This is a sample description that represents that here is something that is wrtten") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val arr = memberModel.notificationList
    var meForFolder = memberModel.myData.collectAsState().value
    LaunchedEffect(Unit){
        scope.launch(Dispatchers.Default){
            memberModel.getNotifn()
        }
    }
    Column(Modifier.fillMaxWidth().background(MidnightBlue).padding(0.dp,0.dp,4.dp,0.dp)){
        Spacer(Modifier.height(2.dp))
        Row(Modifier.fillMaxWidth()){
            Spacer(Modifier.weight(1f))
            if(memberModel.refresh.value) Spinner(20.dp)
            else Image(painterResource(R.drawable.reload),null,Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxSize()){
            if(arr.isEmpty()){
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MidnightBlue)
                        .wrapContentSize(Alignment.Center)) {
                    Image(painter = painterResource(id = R.drawable.blankdir), contentDescription = null , Modifier.size(200.dp) )
                }
            }
            else{
                LazyColumn(
                    Modifier.fillMaxSize()
                        .background(MidnightBlue)){
                    items(arr){
                        AnimatedContent(
                            targetState = it,
                            transitionSpec = {
                                fadeIn() with fadeOut()
                            }
                        ) {
                            Log.d("qwerty",it.toString())
                            if(it.status == "Unread") {
                                when(it.type){
                                    "NGroup" ->{
                                        UnReadNotification(memberModel,it,{ memberModel.changeNotificationStat(it.notificationID,"Decline") },
                                            { memberModel.acceptGroup(it.extra); memberModel.changeNotificationStat(it.notificationID,"Accept") }){
                                            //var person by remember{mutableStateOf<classOrgMember>(classOrgMember())}
                                            var person = memberModel.getPerson(it.idby)
                                            MiniImage({},R.drawable.sampleprof,60)
                                            Spacer(Modifier.width(10.dp))
                                            TextColumn(Modifier,it.nameOrTitle,"Created By: ${person.name} (${person.dept}|${person.designation})",
                                                FontWeight.Medium, FontWeight.Normal,19.sp,17.sp)

                                        }
                                    }
                                    "NEvent" ->{
                                        var event by remember{mutableStateOf(uploadableEvent())}
                                        LaunchedEffect(Unit){
                                            withContext(Dispatchers.Default) {
                                                event = withContext(Dispatchers.Default) { memberModel.getSingleEvent(it.extra) }
                                            }
                                        }
                                        UnReadNotification(memberModel,it,{ memberModel.changeNotificationStat(it.notificationID,"Accept") },
                                            { memberModel.changeNotificationStat(it.notificationID,"Accept");
                                                memberModel.setCurrEvent(event); memberModel.setCurr (
                                                    Screen.EEVENTS); navController2.navigate("EventView");
                                                memberModel.eventNotification = event
                                            },"Dismiss","Visit"){

                                            var person = memberModel.getPerson(it.idby)
                                            Column(Modifier.size(65.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                                                if(event.eventID.isBlank()) Spinner()
                                                else
                                                    Circle(start = event.Sdate/1000 ,curr = System.currentTimeMillis()/1000 , end = event.Edate/1000 , boxshadowColor = Color.Black.copy(alpha  = .2f)
                                                        , size = 60.dp, texts = false)
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            TextColumn(Modifier,it.nameOrTitle,"Created By : ${person.name} (${person.dept}|${person.designation})",
                                                FontWeight.Medium, FontWeight.Normal,19.sp,17.sp)
                                        }
                                    }
                                    "NGMsg" ->{
                                        UnReadNotification(memberModel,it,{ memberModel.changeNotificationStat(it.notificationID,"Accept") },
                                            { memberModel.changeNotificationStat(it.notificationID,"Accept"); },"Dismiss","Visit"){
                                            //var person by remember{mutableStateOf<classOrgMember>(classOrgMember())}
                                            var person = memberModel.getPerson(it.idby)
                                            MiniImage({},R.drawable.sampleprof,60)
                                            Spacer(Modifier.width(10.dp))
                                            TextColumn(Modifier,it.nameOrTitle,"Created By : ${person.name} (${person.dept}|${person.designation})",
                                                FontWeight.Medium, FontWeight.Normal,19.sp,17.sp)
                                        }
                                    }
                                    "NMsg"  ->{
                                        var person = memberModel.getPerson(it.idby)
                                        UnReadNotification(memberModel,it,{ memberModel.changeNotificationStat(it.notificationID,"Accept") },
                                            { memberModel.changeNotificationStat(it.notificationID,"Accept");
                                                memberModel.setTBQuery("")
                                                memberModel.setCurrChatOpened(person);
                                                navController2.navigate("currentChatOpened")
                                                memberModel.collectMsgAndStore(context)
                                                memberModel.populateOnScreenMsg(context)

                                            },"Dismiss","Visit"){
                                            //var person by remember{mutableStateOf<classOrgMember>(classOrgMember())}

                                            MiniImage({},R.drawable.sampleprof,60 , asyncc = extrasForDP("${person.memberID} dp",
                                                "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/ProfilePics",
                                                "${meForFolder.orgID}/${person.memberID}/dp"
                                            ))
                                            Spacer(Modifier.width(10.dp))
                                            TextColumn(Modifier,it.nameOrTitle,"Created By : ${person.name} (${person.dept}|${person.designation})",
                                                FontWeight.Medium, FontWeight.Normal,19.sp,17.sp)
                                        }
                                    }

                                }
                            }
                            else{
                                ReadNotification(it.timestamp,it.type,it.nameOrTitle,it)
                            }
                        }

                    }
                }
            }

        }

    }

}








@OptIn(ExperimentalMaterial3Api::class )
@Composable
fun Feed(imageId:Int= R.drawable.samp, description:String){
    var descExpand:Boolean by remember { mutableStateOf(true) }

    Card(
        modifier= Modifier
            .fillMaxWidth()
            .padding(0.dp, 4.dp)
            .shadow(
                elevation = 5.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(17.dp)
            )
            ,
        shape = RoundedCornerShape(15.dp),
        colors= CardDefaults.cardColors(containerColor = RainForest.outline)

    ) {
        Column(modifier= Modifier.padding(5.dp,7.dp)){
            UserPosted(imgId = R.drawable.sampleprof)
            Divider(color= RainForest.error, thickness = 1.dp)
            Spacer(Modifier.height(7.dp))
            Image(painter = painterResource(imageId), contentDescription = null,
                modifier= Modifier
                    .clip(RoundedCornerShape(6))
                    .height(230.dp)
                    .shadow(0.dp),
                contentScale = ContentScale.Crop

            )
            Row(modifier= Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .animateContentSize(
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ){
                SelectionContainer(){
                    Text(description,style=MaterialTheme.typography.bodyLarge, fontFamily = Jost,modifier= Modifier
                        .fillMaxWidth(.85f)
                        .padding(10.dp, 0.dp),
                        maxLines =  if(descExpand) 2 else 10,
                        overflow = TextOverflow.Ellipsis,
                      //  textAlign = TextAlign.Justify
                        )
                }

                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { descExpand=!descExpand },modifier= Modifier) {
                    Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null , tint= Color.Black,modifier= Modifier.size(30.dp) )
                }
            }
        }
    }
}



@Composable
fun UserPosted(imgId:Int,name:String="Sample Name", position:String="Designation",department:String="Designation",onProfileClick:()->Unit={}){
    Row(modifier= Modifier
        .fillMaxWidth()
        .padding(0.dp, 0.dp, 0.dp, 4.dp)

    )
    {
        Spacer(Modifier.width(10.dp))
        MiniImage(onProfileClick = onProfileClick, imgId = imgId , 45)
        Spacer(Modifier.width(15.dp))
        MiniText(
            name = name, style1 = MaterialTheme.typography.titleMedium,weight1= FontWeight.SemiBold,
            position = position,style2= MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterVertically) , department = department)
        Spacer(Modifier.weight(1f))
        MoreBtn()


    }
}

//Vertical button that on click display action applicable of any specific feed
@Composable
fun MoreBtn(){
    var moreOptionsExpand:Boolean by remember { mutableStateOf(false) }
    Box(){
        IconButton(onClick = { moreOptionsExpand = !moreOptionsExpand }) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
        }

            DropdownMenu(
                modifier = Modifier.background(RainForest.primary),
                expanded = moreOptionsExpand,
                onDismissRequest = { moreOptionsExpand = false },
                properties = PopupProperties(focusable=false, excludeFromSystemGesture=true),
            ) {

                ItemOption("SHARE",{})
                ItemOption("ARCHIVE",{})
                ItemOption("REPLY",{})

            }
    }



}

//Item composable that contains the option's functionality and appearance
@SuppressLint("SuspiciousIndentation")
@Composable
fun ItemOption(str:String,onClick:()->Unit,dialog:Boolean=false,str1:String="",str2:String=""){
    var visible by rememberSaveable{ mutableStateOf(false) }
    DropdownMenuItem(
        text = {
            Row(){
                Spacer(Modifier.weight(1f))
                Text(str, style= MaterialTheme.typography.titleSmall , fontFamily = Jost , color = Color.Black )
                Spacer(Modifier.weight(1f))
            }

               },
        onClick = { if(!dialog)onClick(); else{ visible = true} }
    )
    if(visible)
    ConfirmDialog(onYes = { onClick(); visible=false }, onNo = { visible = false }, str1 = str1, str2 = str2)
}

@Composable
fun UnReadNotification(memberModel: MemberModel,notification: Notification,onLeftClick:()->Unit,onRightClick:()->Unit,bStr1:String= "Decline",bStr2:String="Accept",content: @Composable()(RowScope.() -> Unit)){
    Row(Modifier.fillMaxWidth()){
        Spacer(Modifier.width(10.dp))
        Column(){
            Spacer(Modifier.height(10.dp))
            Box(){
                var time by rememberSaveable{mutableStateOf(false)}
                Button(onClick = {time = true},Modifier.size(15.dp),colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)){}
                MaterialTheme(colorScheme = RainForest, shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(46.dp))  ){
                    DropdownMenu(time,{time = false},properties = PopupProperties(excludeFromSystemGesture = false) ,  modifier = Modifier.height(40.dp)){
                        GreyCard(paddingH = 10.dp, backColor = Color(0xFF649FFD)){ Text(convertTimestampToDate(notification.timestamp), fontSize = 13.sp,color = Color.Black) }
                    }
                }


            }

        }
        Spacer(Modifier.width(10.dp))
        GreyCard(Modifier.fillMaxWidth().shadow(Color.Black.copy(alpha = .5f),1.dp,1.dp,7.dp,100.dp,100.dp),paddingH=10.dp,paddingV=4.dp,backColor = RainForest.onSurface){
            Column(){
                Row(Modifier.fillMaxWidth()){
                    Image(painterResource(notifn[notification.type]!!.second),null,Modifier.size(22.dp))
                    Spacer(Modifier.width(18.dp))
                    Text(notifn[notification.type]!!.first,fontSize=18.sp,fontFamily = Jost)
                    Spacer(Modifier.weight(1f))
                    Text(momentAgo(notification.timestamp),fontSize=16.sp,fontFamily = Jost)
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                    content()
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()){
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {onLeftClick(); memberModel.getNotifn()},Modifier.shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),
                        contentPadding = PaddingValues(40.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = RainForest.onSurface, contentColor = Color.Black),border = BorderStroke(1.dp,Color.Black)
                    ) { Text(bStr1,fontSize = 18.sp, fontFamily = Jost ) }
                    Spacer(Modifier.width(35.dp))
                    Button(onClick = {onRightClick(); memberModel.getNotifn()},Modifier.shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),
                        contentPadding = PaddingValues(46.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) { Text(bStr2,fontSize = 18.sp, fontFamily = Jost) }
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(3.dp))
            }
        }
    }

}

@Composable
fun ReadNotification(time:Long,type:String,textMain:String,itt:Notification){
    Row(Modifier.fillMaxWidth()){
        Spacer(Modifier.width(10.dp))
        Column(){
            Spacer(Modifier.height(10.dp))
            Box(){
                var tim by rememberSaveable{mutableStateOf(false)}
                Button(onClick = {tim = true},Modifier.size(15.dp),colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)){}
                MaterialTheme(colorScheme = RainForest, shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(46.dp))  ){
                    DropdownMenu(tim,{tim = false},properties = PopupProperties(excludeFromSystemGesture = false) ,  modifier = Modifier.height(40.dp)){
                        GreyCard(paddingH = 10.dp, backColor = Color(0xFF649FFD)){ Text(convertTimestampToDate(itt.timestamp), fontSize = 13.sp,color = Color.Black) }
                    }
                }

            }
        }
        Spacer(Modifier.width(10.dp))
        GreyCard(Modifier.fillMaxWidth().shadow(Color.Black.copy(alpha = .4f),0.dp,1.dp,8.dp,30.dp,90.dp),paddingH=10.dp,paddingV=4.dp,backColor = IndigoInk){
            Column(){
                Row(Modifier.fillMaxWidth()){
                    Image(painterResource(notifn[type]!!.second),null,Modifier.size(17.dp))
                    Spacer(Modifier.width(18.dp))
                    Text(notifn[type]!!.first,fontSize=13.sp,fontFamily = Jost,color = Color.White)
                    Spacer(Modifier.weight(1f))
                    Text(momentAgo(time),fontSize=13.sp,fontFamily = Jost,color = Color.White)
                }
                Spacer(Modifier.height(0.dp))
                Text(textMain,fontSize=20.sp,fontFamily = Jost,color = Color.White)
            }
        }
    }


}


fun momentAgo(smallerTimestamp: Long): String {
    val currentTimestamp = System.currentTimeMillis() / 1000 // Convert to seconds

    val smallerInstant = Instant.ofEpochSecond(smallerTimestamp/1000)
    val currentInstant = Instant.ofEpochSecond(currentTimestamp)

    val duration = Duration.between(smallerInstant, currentInstant)
    val seconds = duration.seconds

    return when {
        seconds < 60 -> "$seconds seconds ago"
        seconds < 3600 -> "${seconds / 60} minutes ago"
        seconds < 86400 -> "${seconds / 3600} hours ago"
        seconds < 2592000 -> "${seconds / 86400} days ago"
        else -> {
            val smallerDateTime = LocalDateTime.ofEpochSecond(smallerTimestamp, 0, ZoneOffset.UTC)
            smallerDateTime.toString()
        }
    }
}