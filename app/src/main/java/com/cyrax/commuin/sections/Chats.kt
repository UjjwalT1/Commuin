package com.cyrax.commuin.sections


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.cyrax.commuin.R
import com.cyrax.commuin.Utils.FullImage
import com.cyrax.commuin.functions._to12Hr
import com.cyrax.commuin.functions.deleteFileInDirectory
import com.cyrax.commuin.functions.uriToArrPdf
import com.cyrax.commuin.struct.ChatList
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Message
import com.cyrax.commuin.struct.MessageToSend
import com.cyrax.commuin.struct.Notification
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.db
import com.cyrax.commuin.struct.documentMime
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.greyPeriList
import com.cyrax.commuin.struct.littleIcons
import com.cyrax.commuin.struct.toChatList
import com.cyrax.commuin.struct.toClassOrgMember
import com.cyrax.commuin.struct.toMessageToSend
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.RainForest
import com.cyrax.commuin.ui.theme.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun ChatHolder(navController2 :NavHostController = rememberNavController() , memberModel: MemberModel = viewModel()){
    val listOfChats = if(memberModel.topBarQuery.collectAsState().value.isBlank())memberModel.peopleListOnScreen else memberModel.queriedPeopleListOnScreen
    val context = LocalContext.current
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(MidnightBlue)){
        items(listOfChats){
            val temp = it.toClassOrgMember()
            ImageNameClickable(
                memberModel,
                navController2 = navController2, obj = temp,
                onNameClick = {
                    memberModel.setTBQuery("")
                    memberModel.setCurrChatOpened(temp);
                    navController2.navigate("currentChatOpened")
                    memberModel.collectMsgAndStore(context)
                    memberModel.populateOnScreenMsg(context)
                },
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvList(  navController2: NavHostController , onBackClick: () -> Unit , memberModel: MemberModel){
    BackHandler( enabled = true ) { onBackClick(); memberModel.CurrChatJob!!.cancel()}
    val arr = memberModel.messagesOnScreen
    val listState = rememberLazyListState()
  //  val checkedMap = memberModel.selectedMsgs
    val scope = rememberCoroutineScope()
    LazyColumn(state = listState ,modifier = Modifier
        .fillMaxSize()
        .background(brush = Brush.verticalGradient(listOf(IndigoInk, MidnightBlue)))
        , verticalArrangement = Arrangement.Bottom){
        items(arr){
            val msg = it.toMessageToSend()
            if( myIDFromRoom(it.room) == memberModel.myData.collectAsState().value.memberID ) {
                Row( modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically){
                 //   Checkbox(checked = checkedMap.containsKey(it.uniqueID) , onCheckedChange = null)
                    when(it.type){
                        "--::--doc" ->{DocMsg(MessageToSend(msg = it.msg , time = it.time , extra = it.extra , extn = it.extn) , design = "Send" , memberModel = memberModel,onLongPress = {/*TODO LONGPRESS*/})}
                        "--::--img" ->{ImageMsg(MessageToSend(msg = it.msg , time = it.time , extra = it.extra) , design = "Send" , memberModel = memberModel, onLongPress = {/*TODO LONGPRESS*/})}
                        "--::--msg" ->{Msg(msg = msg , design = "Send" , color = MidnightBlue,onLongPress = {/*TODO LONGPRESS*/})}
                    }
                }

            }
            else{
                Row( modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                    when (it.type) {
                        "--::--doc" -> {DocMsg(MessageToSend(msg = it.msg , time = it.time , extra = it.extra , extn = it.extn)  , design = "Rec" , memberModel = memberModel,onLongPress = {/*TODO LONGPRESS*/})}
                        "--::--img" -> {ImageMsg(MessageToSend(msg = it.msg , time = it.time , extra = it.extra) ,  design = "Rec", memberModel = memberModel, onLongPress = {/*TODO LONGPRESS*/}) }
                        "--::--msg" -> {Msg(msg= msg , color = MidnightBlue ,design = "Rec",onLongPress = {/*TODO LONGPRESS*/})}
                    }
                //    Checkbox(checked = checkedMap.containsKey(it.uniqueID), onCheckedChange = null)
                }
            }

        }

    }
    if(arr.size != 0 )
        LaunchedEffect(arr.size) {
            listState.scrollToItem(arr.size - 1)
        }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Msg(msg:MessageToSend , color:Color, design:String = "",onLongPress:()->Unit){
    val context =LocalContext.current
    val descExpand = rememberSaveable { mutableStateOf(false)  }
    Card(modifier = Modifier
        .widthIn(0.dp, 300.dp)
        .wrapContentWidth()
        .padding(10.dp, 1.dp),colors = CardDefaults.cardColors(color)
    ) {
        Column(modifier = Modifier.padding(0.dp,0.dp)){
            Row(){
                if(design=="Send")Spacer(Modifier.width(1.dp))
                Card(modifier = Modifier,colors = CardDefaults.cardColors(Color.White)

                ) {
                    Column(modifier = Modifier
                        .padding(7.dp, 0.dp, 5.dp, 0.dp)
                        .animateContentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                // onPress = { TOAST(context,"Done") },
                                onTap = { descExpand.value = !descExpand.value },
                                onLongPress = { TOAST(context, "Done") }
                            )

                        }) {
                        //Actual message text
                        Text(
                            msg.msg,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = if (!descExpand.value) 10 else 100,
                            overflow = TextOverflow.Ellipsis,color= Color.Black
                        )
                        Text(modifier = Modifier.align(Alignment.End),text= msg.time, fontSize = 9.sp ,color=Color.Black)
                    }
                }
                if(design=="Rec")Spacer(Modifier.width(1.dp))
            }
            Spacer(Modifier.height(1.dp))
        }
    }
}

@Composable
fun SendBox(memberModel: MemberModel, context : Context = LocalContext.current){
    var txt by rememberSaveable{ mutableStateOf("")}
    val scope = rememberCoroutineScope()
    val me = memberModel.myData.collectAsState().value
    val recipient = memberModel.currentChatOpened.collectAsState().value!!

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .fillMaxWidth()
        .heightIn(60.dp, 270.dp)
        .background(
            MidnightBlue
        )){
        var moreOptionsExpand:Boolean by remember { mutableStateOf(false) }
        CustomTextField(
            Modifier
                .fillMaxWidth(.80f)
                .heightIn(40.dp, 200.dp)
                .background(
                    Brush.horizontalGradient(greyPeriList),
                    shape = RoundedCornerShape(40.dp)
                )
                .clip(RoundedCornerShape(20.dp)),
            value = txt, onValueChange = { txt = it } , placeholder = "Your Message" ,
        trailingIcon = {
            Row(){
                Box(){
                    IconButton(onClick = { moreOptionsExpand = !moreOptionsExpand }) {
                        Image(painter = painterResource(id = R.drawable.clip) , null , modifier = Modifier.size(28.dp)) }
                    DropdownMenu(
                        modifier = Modifier.background(RainForest.primary),
                        expanded = moreOptionsExpand,
                        onDismissRequest = { moreOptionsExpand = false },
                        properties = PopupProperties(focusable=false),
                    ) {
                        Row(){
                            SendImg(memberModel = memberModel ,context , txt = txt)
                            SendDoc(memberModel = memberModel ,context , txt = txt)
                        }

                    }
                }

                IconButton(onClick = { /*TODO : ON MOREVERT CLICKED*/ }) { Image(painter = painterResource(id = R.drawable.more_vertical) , null , modifier = Modifier.size(24.dp)) }
            }

        } , maxLines = 6 , paddingLeadingIconEnd = 40.dp)

        Spacer(Modifier.weight(1f))
        BrightWithButton(height = 45.dp, boxcolor = IndigoInk, boxshadowColor = Color.Black.copy(alpha = .7f), textColor = Periwinkle,
            shape = RoundedCornerShape(40.dp,40.dp,40.dp,40.dp), onClick = {
                if(txt.isNotBlank())
                    scope.launch {
                        txt = txt.trim()
                        val timeStuff = getTime()
                        memberModel.myDataAsSender = me.toChatList(timeStuff.TS)
                        val msgObj = MessageToSend(timeStuff.uniqueID , txt , timeStuff.date , timeStuff.time , memberModel.myData.value.memberID+" "+recipient.memberID, "--::--msg" , "")
                        val personObj = ChatList(timestamp = timeStuff.TS , memberID = recipient.memberID , orgName = recipient.organName,name = recipient.name , recipient.email ,
                            recipient.contact , recipient.designation , recipient.dept , path = "" )
                        memberModel.sendMsg(msgObj)
                        memberModel.sendNotification(Notification("","Unread",timeStuff.TS,"${me.memberID} ${me.dept} ${me.designation}",me.profileDetail.name,"-- -- --","NMsg"),"${me.orgID}/${recipient.memberID}/Notifications",recipient,me)
                        txt = ""
                        memberModel.addPersonToServerList(personObj)
                    }

            },id = R.drawable.aeroplae, btnSize = 35.dp)

    }

}




/**
 * The Material Design type scale includes a range of contrasting styles that support the needs of
 * @property labelSmall labelSmall is one of the smallest font sizes. It is used sparingly to
 * annotate imagery or to introduce a headline.
 */
@Composable
fun TopbarChat(
    memberModel : MemberModel,
    onBackClick:()->Unit,
    onProfileClick:()->Unit,
){
    val recipient = memberModel.currentChatOpened.collectAsState().value!!
    val context = LocalContext.current
    LargeTopAppBar(colors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = MidnightBlue),
        modifier = Modifier
            .height(55.dp)
            .imePadding(),
        title = {},
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically){
                BrightWithButton(height = 45.dp, boxcolor = IndigoInk, boxshadowColor = Color.Black.copy(alpha = .7f), textColor = Periwinkle,
                    shape = RoundedCornerShape(0.dp,40.dp,40.dp,0.dp), onClick = onBackClick,id = R.drawable.left_chevron, btnSize = 30.dp)
                Spacer(modifier = Modifier.width(10.dp))
                val meForFolder = memberModel.myData.collectAsState().value
                Row(modifier = Modifier.fillMaxHeight(),verticalAlignment = Alignment.CenterVertically) {
                    MiniImage(onProfileClick = onProfileClick, imgId = R.drawable.sampleprof, size = 45 , asyncc = extrasForDP("${recipient.memberID} dp",
                        "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/ProfilePics",
                        "${meForFolder.orgID}/${recipient.memberID}/dp"
                    )
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    MiniText(name = recipient.name, style1 = MaterialTheme.typography.titleLarge , weight1 = FontWeight.Medium, position = recipient.memberID , style2 = MaterialTheme.typography.labelSmall , department = recipient.dept , color = Color.White)
                }
            }},
        actions={
            Row{
                BrightWithButton(height = 45.dp, boxcolor = IndigoInk, boxshadowColor = Color.Black.copy(alpha = .7f), textColor = Periwinkle,
                    shape = RoundedCornerShape(40.dp,0.dp,0.dp,40.dp), onClick = {
                       // memberModel.share(context,"","","")
                                                                                 },id = R.drawable.more_vertical, btnSize = 30.dp)
            } }
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageMsg(msg:MessageToSend = MessageToSend("","Hey there!","12:30 PM"), color:Color = MidnightBlue, design:String = "Rec" , memberModel: MemberModel,onLongPress:()->Unit){
    val context =LocalContext.current
    val me = memberModel.myData.collectAsState().value
    var uri by rememberSaveable { mutableStateOf<String?>("loading")  }
    val recipient: classOrgMember? = memberModel.currentChatOpened.collectAsState().value
    var scope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit){
        scope.launch(Dispatchers.Default) {
            try{
                uri = memberModel.checkAndFetch(context , msg.extra , "Image")
            }catch(e:Exception){Log.d("qwerty",e.message!!)}
        }
    }
    val descExpand = rememberSaveable { mutableStateOf(false)  }
    Card(modifier = Modifier
        .padding(10.dp, 1.dp),colors = CardDefaults.cardColors(color)
    ) {
        Column{
            Row(){
                var visible by rememberSaveable{mutableStateOf(false)}
                if(visible)
                    ConfirmDialog(onYes = {
                        deleteFileInDirectory(uri!!)
                        uri = null; TOAST(context,"Deleted"); visible = false }, onNo = { visible = false }, str1 = "Delete Image", str2 = "Delete image ${msg.extra} from device ?")
                if(uri != "loading" && uri != null){
                    IconButton(onClick = { visible = true
                    }){
                        Image(  painterResource(R.drawable.trash) , null,Modifier.size(25.dp,25.dp))
                    }
                }
                if(design=="Send")Spacer(Modifier.width(1.dp))
                Card(modifier = Modifier,colors = CardDefaults.cardColors(Periwinkle)
                ) {
                    Column(modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                        .animateContentSize()) {
                        //Actual message text
                        Box(modifier = Modifier.size(260.dp, 350.dp)){
                            var big by rememberSaveable { mutableStateOf(false)}
                            Column(
                                Modifier
                                    .clip(RoundedCornerShape(3))
                                    .size(260.dp, 350.dp).pointerInput(Unit){
                                        detectTapGestures(onLongPress = {
                                            onLongPress()
                                                                        },
                                            onTap = {
                                            when (uri) {
                                                "loading" -> {}
                                                null -> {
                                                    uri = "loading"
                                                    scope.launch {
                                                        val pathToDwnld = if(design == "Send")"Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Images/Sent" else "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Images/${recipient?.name}_${recipient?.memberID}"
                                                        memberModel.downloadAndReturnUri(context,pathToDwnld,
                                                            "${me.orgID}/${me.memberID}/images/${msg.extra}",
                                                            msg.extra,"png","img"
                                                        ).collect{
                                                            if(it!=null){
                                                                uri = it
                                                            }
                                                        }
//                                            memberModel.getImageEach(context,imgID ,"${me.orgID}/${me.memberID}/images/${imgID}", pathToDwnld ,"img", "jpg")
//                                                .collect{
//                                                    if(it != null) uri = it
//                                                }
                                                    }
                                                }
                                                else -> {big = !big }
                                            }
                                        })
                                    }) {
                                Box(Modifier
                                    .clip(RoundedCornerShape(3))
                                    .size(260.dp, 350.dp).wrapContentSize(Alignment.Center)){
                                    Column( modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)){
                                        when(uri){
                                            "loading" -> Spinner(indicatorSize = 70.dp)
                                            null -> Image(
                                                painter = painterResource(R.drawable.downloa),
                                                // model = "https://firebasestorage.googleapis.com/v0/b/commu-in-e20d8.appspot.com/o/placeholder.jpg?alt=media&token=3aff3d77-e734-41fb-9916-1ad9acbaf120",
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3))
                                                    .size(90.dp, 90.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            else -> AsyncImage(
                                                model = uri ,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3))
                                                    .size(260.dp, 350.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha=.7f)).wrapContentSize(Alignment.Center)){
                                        Spacer(Modifier.width(3.dp))
                                        Text(msg.extra,fontSize = 15.sp , fontFamily = Jost , color = Color.White)
                                        Spacer(Modifier.width(3.dp))
                                    }

                                }

                            }

                            if(big) FullImage(uri!!,{big=false})
                        }

                        Column( modifier = Modifier
                            .width(260.dp)
                            .padding(4.dp, 4.dp) ){
                            if(msg.msg.isNotBlank())
                                Text(
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { descExpand.value = !descExpand.value },
                                            onLongPress = { TOAST(context, "Long press") }
                                        )

                                    } ,
                                    text =msg.msg,
                                    fontSize = 18.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = if (!descExpand.value) 10 else 100,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            Text(modifier = Modifier.align(Alignment.End) , text=msg.time , fontSize = 9.sp ,color=Color.Black)
                        }


                    }
                }
                if(design=="Rec")Spacer(Modifier.width(3.dp))
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocMsg(msg:MessageToSend = MessageToSend("","Sample message",time ="12:30 PM"),  color:Color = MidnightBlue, design:String = "Rec" , memberModel: MemberModel,onLongPress:()->Unit){
    val context =LocalContext.current
    val me = memberModel.myData.collectAsState().value
    val scope = rememberCoroutineScope()
    val recipient = memberModel.currentChatOpened.collectAsState().value
    var uri by rememberSaveable { mutableStateOf<String?>("loading")  }
    LaunchedEffect(key1 = Unit){
        scope.launch(Dispatchers.Default) {
            try{
                uri = memberModel.checkAndFetch(context , msg.extra , "Document")
            }catch(e:Exception){Log.d("qwerty",e.message!!)}
        }
    }
    val descExpand = rememberSaveable { mutableStateOf(false)  }
    var size by  rememberSaveable { mutableStateOf(0)  }
    if(uri!=null && uri!= "loading"){
        LaunchedEffect(key1 = Unit, block = {size = uriToArrPdf(context, Uri.parse(uri))!!.size})
    }
    Card(modifier = Modifier
        .padding(10.dp, 1.dp),colors = CardDefaults.cardColors(color)
    ) {
        Column{
            Row(){
                var visible by rememberSaveable{mutableStateOf(false)}
                if(visible)
                    ConfirmDialog(onYes = {deleteFileInDirectory(uri!!)
                        uri = null
                        size = 0; TOAST(context,"Deleted"); visible = false }, onNo = { visible = false }, str1 = "Delete Document", str2 = "Delete document ${msg.extra} from device ?")
                IconButton(onClick = {
                    when (uri) {
                        "loading" -> {}
                        null -> {
                            uri = "loading"
                            scope.launch {
                                val pathTo = if(design == "Send")"Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/Sent" else "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/${recipient?.name}_${recipient?.memberID}"
                                memberModel.downloadAndReturnUri(context,pathTo,
                                    "${me.orgID}/${me.memberID}/documents/${msg.extra}",
                                    msg.extra,msg.extn,"doc"
                                ).collect{
                                    if(it!=null){
                                        uri = it
                                    }
                                }
                            }
                        }
                        else -> {
                            visible = true
                        }
                    }
                }) {
                    when(uri){
                        "loading" -> Spinner(indicatorSize = 30.dp)
                        null -> Image(  painterResource(R.drawable.downloa) , null,Modifier.size(25.dp,25.dp))
                        else -> Image(  painterResource(R.drawable.trash) , null,Modifier.size(25.dp,25.dp))
                    }
                }
                if(design=="Send")Spacer(Modifier.width(1.dp))
                Card(modifier = Modifier,colors = CardDefaults.cardColors(Periwinkle)
                ) {
                    Column(modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                        .animateContentSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .width(250.dp)
                                .padding(horizontal = 2.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { Log.d("TAG", "PDF") },
                                        onTap = {
                                            when (uri) {
                                                "loading" -> {
                                                    TOAST(context, "LOADING")
                                                }

                                                null -> {
                                                    TOAST(context, "Downloading ...")
                                                    uri = "loading"
                                                    scope.launch {
                                                        val pathTo =
                                                            if (design == "Send") "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/Sent" else "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/${recipient?.name}_${recipient?.memberID}"
                                                        memberModel
                                                            .downloadAndReturnUri(
                                                                context, pathTo,
                                                                "${me.orgID}/${me.memberID}/documents/${msg.extra}",
                                                                msg.extra, msg.extn, "doc"
                                                            )
                                                            .collect {
                                                                if (it != null) {
                                                                    uri = it
                                                                }
                                                            }
                                                    }
                                                }

                                                else -> {
                                                    openFileFromByteArray(
                                                        context,
                                                        uriToArrPdf(context, Uri.parse(uri))!!,
                                                        msg.extra,
                                                        documentMime[msg.extn]!!.first, //Here the mime type and extension are stored in a map
                                                        documentMime[msg.extn]!!.second
                                                    )
                                                }
                                            }
                                        })
                                }){
                            Image(painter = painterResource(id = littleIcons[msg.extn]!!), contentDescription = null , modifier= Modifier
                                .size(50.dp)
                                .padding(horizontal = 2.dp))
                            Column(modifier= Modifier.padding(horizontal = 2.dp)) {
                                Spacer(modifier= Modifier.height(5.dp))
                                Text(msg.extra,fontSize = 15.sp,maxLines = 2, lineHeight = 15.sp ,
                                    overflow = TextOverflow.Ellipsis,color = Color.Black)
                                Text("${bytesToMegabytes(size.toLong())} MB",fontSize = 12.sp,color = Color.Black)
                            }
                        }
                        Column( modifier = Modifier
                            .width(250.dp)
                            .padding(4.dp, 4.dp) ){
                            Spacer(Modifier.height(5.dp))
                            if(msg.msg.isNotBlank())
                                Text( modifier = Modifier.clickable { descExpand.value=!descExpand.value },text =msg.msg, fontSize = 17.sp, lineHeight = 17.sp, fontWeight = FontWeight.Normal,
                                maxLines = if (!descExpand.value) 10 else 100,
                                overflow = TextOverflow.Ellipsis, )
                            Text(modifier = Modifier.align(Alignment.End) , text=msg.time , fontSize = 9.sp , color = Color.Black)
                        }
                    }
                }
                if(design=="Rec")Spacer(Modifier.width(3.dp))
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}


data class CurrTime(var date:String="null", var time:String="null", var uniqueID :String = "" , var TS:Long=0)
/**
 * Function that returns date and time
 * @return Object (CurrTime)
 */
fun getTime():CurrTime{
    val obj = CurrTime(TS = System.currentTimeMillis())

    val date = Date(obj.TS)
    val formatter1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    obj.date = formatter1.format(date)
    val inputFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
    obj.time = _to12Hr(inputFormat.format(date) )
    obj.uniqueID = timeToID(obj.TS)

    return obj
}


fun timeToID( timestamp: Long):String{
    var id:String = ""
    for(i in 1..4)
        id+= (0..9).random()

    id += timestamp.toString()

    return id
}


fun myIDFromRoom(room:String):String{
    var ans=""
    var i=0
    while(i<room.length && room[i]!=' '){
        ans+=room[i]
        i++;
    }

    return ans;
}

fun RecipientIDFromRoom(room:String):String{
    var i=0
    while(i<room.length && room[i]!=' '){
        i++;
    }

    return room.substring(i+1);
}




fun openPdf(context: Context, byteArray:ByteArray, id :String){
    val tempFile = File.createTempFile(id, ".pdf", context.cacheDir)
    tempFile.outputStream().use { outputStream ->
        outputStream.write(byteArray)
    }
    val uri = FileProvider.getUriForFile(context, "com.cyrax.commuin.fileprovider", tempFile)
    Log.d("qwerty","  - --  $uri")
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "application/pdf")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try{
        startActivity(context,intent,null)
    }catch(e:Exception){
        Log.e("ERROR", e.message!!)
    }

}

fun fileNameFromUri(uri:String):String{
    var i = uri.length -1
    var name= ""
    while(uri[i]!='/'){
        name+=uri[i]
        i--
    }
    return name.reversed()
}


fun searchAmongList(str:String ,arr: List<ChatList>):List<ChatList>{
    return arr.filter { it.name.lowercase().contains(str.lowercase())   }
}



