package com.cyrax.commuin.sections

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.cyrax.commuin.R
import com.cyrax.commuin.Utils.FullImage
import com.cyrax.commuin.functions.deleteFileInDirectory
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.functions.uriToArrPdf
import com.cyrax.commuin.struct.ChatList
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.MessageToSend
import com.cyrax.commuin.struct.MessageToSendGP
import com.cyrax.commuin.struct.Notification
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.documentMime
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.greyPeriList
import com.cyrax.commuin.struct.littleIcons
import com.cyrax.commuin.struct.toChatList
import com.cyrax.commuin.struct.toMessageToSend
import com.cyrax.commuin.struct.toMessageToSendGP
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.RainForest
import com.cyrax.commuin.ui.theme.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun GroupHolder(navController2 : NavHostController = rememberNavController(),memberModel: MemberModel = viewModel()){
    val ar = memberModel.myGroups
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().background(MidnightBlue).padding(0.dp,0.dp,4.dp,0.dp)){
        Spacer(Modifier.height(2.dp))
        Row(Modifier.fillMaxWidth()){
            Spacer(Modifier.weight(1f))
            if(memberModel.gRefresh.value) Spinner(20.dp)
            else Image(painterResource(R.drawable.reload),null,Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.height(5.dp))
        if(ar.isEmpty()){
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MidnightBlue)
                    .wrapContentSize(Alignment.Center)) {
                Image(painter = painterResource(id = R.drawable.blankdir), contentDescription = null , Modifier.size(200.dp) )
            }
        }
        else{
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)){
                items(ar){
                    GreyCard(
                        Modifier
                            .height(63.dp)
                            .fillMaxWidth()
                            //  .background(RainForest.onSurface, shape = RoundedCornerShape(20.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {},
                                    onTap = { _ ->
                                        memberModel.setCurr(Screen.GroupConv)
                                        memberModel.setCurrGrpChatOpened(it)
                                        navController2.navigate("GroupConvList")
                                        scope.launch {
                                            memberModel.populateOnScreenMsgGrp(context)
                                            delay(100)
                                            memberModel.collectMsgAndStoreGrp(context)
                                        }

                                    }
                                )
                            },backColor = RainForest.onSurface, paddingV = 5.dp, paddingH = 10.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround){
                            MiniImage({},R.drawable.sampleprof,50)
                            Spacer(Modifier.width(20.dp))
                            TextColumn(str1 = it.title , str2 = it.description , FW1 = FontWeight.Medium, FW2 = FontWeight.Light, FSize1 = 20.sp, FSize2 = 16.sp )
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun CreateGroupForm(memberModel:MemberModel,navController2: NavHostController){
    BackHandler(true) {
        memberModel.resetEventForm();
        memberModel.setCurr(Screen.GROUPS)
        navController2.popBackStack()
    }
    val context = LocalContext.current
    val groupDataCache = memberModel.groupform.collectAsState().value
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MidnightBlue), horizontalAlignment = Alignment.CenterHorizontally){
        item{

            var color by remember{ mutableStateOf(Periwinkle) }
            var currExpand by rememberSaveable { mutableStateOf("") }
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH =0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand ==  "Title")Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("Title" ,FS = 20.sp, FontWeight.Medium,onClick = {
                    if( currExpand != "Title") {
                        currExpand = "Title";
                        color = Color.White
                        /*TODO WHEN SET IS CLICKED*/
                    } else {currExpand = ""; color = Periwinkle}

                } , expand =currExpand != "Title",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "Title"  , enter = expandVertically(),exit = shrinkVertically()) {
                    Row(modifier = Modifier.padding(20.dp,5.dp,20.dp,10.dp)){
                        CustomTextField(
                            placeholder = " Group Name comes here !",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFD4D4D4),
                                            Periwinkle
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            leadingIcon = {  },
                            paddingLeadingIconEnd = 10.dp,
                            value = groupDataCache.title,
                            onValueChange = {
                                memberModel.updateGform(it,1)
                            }
                        )
                    }

                }
            }
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH = 0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand == "Description" )Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("Description" ,FS = 20.sp,FontWeight.Medium, onClick = {
                    if( currExpand != "Description") {
                        currExpand = "Description";
                        color = Color.White
                        /*TODO WHEN SET IS CLICKED*/
                    } else {currExpand = ""; color = Periwinkle} } ,

                    expand =currExpand != "Description",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "Description"  , enter = expandVertically() ,exit = shrinkVertically()) {
                    Column{
                        GreyCard(
                            Modifier.fillMaxWidth(),
                            paddingH = 10.dp,
                            paddingV = 10.dp,
                            backColor = Color(0xffd4d4d4),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            BasicTextField(value = groupDataCache.description, onValueChange = { memberModel.updateGform(it ,2) } ,
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(300.dp, 1000.dp),
                                textStyle = TextStyle( fontSize = 16.sp)
                            )
                        }
                    }
                }
            }
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH = 0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand == "GroupMembers")Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("GroupMembers" ,FS = 20.sp,FW = FontWeight.Medium,
                    onClick = {   if( currExpand != "GroupMembers") {
                        currExpand = "GroupMembers";
                        color = Color.White
                        /*TODO WHEN SET IS CLICKED*/
                    } else {currExpand = ""; color = Periwinkle
                    }
                    }   ,
                    expand =  currExpand != "GroupMembers",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "GroupMembers"  , enter = expandVertically() ,exit = shrinkVertically()) {
                    Column(Modifier.heightIn(0.dp,600.dp)){
                        VisibiltyParent(memberModel)
                    }
                }
            }


        }

    }
}

@Composable
fun GroupConvList(  navController2: NavHostController , onBackClick: () -> Unit , memberModel: MemberModel){
    BackHandler( enabled = true ) { onBackClick(); memberModel.CurrChatJob!!.cancel() }
    val arr = memberModel.grpMessagesOnScreen
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val meForFolder = memberModel.myData.collectAsState().value
    LazyColumn(state = listState ,modifier = Modifier
        .fillMaxSize()
        .background(brush = Brush.verticalGradient(listOf(IndigoInk, MidnightBlue)))
        , verticalArrangement = Arrangement.Bottom){
        items(arr){

            if( it.whoSendID == memberModel.myData.collectAsState().value.memberID ) {
                Row( modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically){
                    //   Checkbox(checked = checkedMap.containsKey(it.uniqueID) , onCheckedChange = null)
                    when(it.type){
                        "--::--doc" ->{DocMsgGrp(it.toMessageToSendGP() , design = "Send" , memberModel = memberModel,onLongPress = {/*TODO LONGPRESS*/})}
                        "--::--img" ->{ImageMsgGrp(it.toMessageToSendGP(), design = "Send" , memberModel = memberModel, onLongPress = {/*TODO LONGPRESS*/})}
                        "--::--msg" ->{MsgGrp(it.toMessageToSendGP() , design = "Send" , color = MidnightBlue,onLongPress = {/*TODO LONGPRESS*/}, memberModel = memberModel)}
                    }
                }

            }
            else{
                Row( modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
                    Spacer(Modifier.width(5.dp))
                    Column{
                        Spacer(Modifier.height(5.dp))
                        MiniImage(onProfileClick = {} ,imgId = R.drawable.sampleprof , size = 25 , asyncc = extrasForDP("${it.whoSendID} dp",
                            "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/ProfilePics",
                            "${meForFolder.orgID}/${it.whoSendID}/dp"
                        ))
                    }
                    when (it.type) {
                        "--::--doc" -> {DocMsgGrp(it.toMessageToSendGP()  , design = "Rec" , memberModel = memberModel,onLongPress = {/*TODO LONGPRESS*/})}
                        "--::--img" -> {ImageMsgGrp(it.toMessageToSendGP() ,  design = "Rec", memberModel = memberModel, onLongPress = {/*TODO LONGPRESS*/}) }
                        "--::--msg" -> {MsgGrp(it.toMessageToSendGP() , color = MidnightBlue ,design = "Rec",onLongPress = {/*TODO LONGPRESS*/},memberModel)}
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


@Composable
fun SendBoxGrp(memberModel: MemberModel, context : Context = LocalContext.current){
    var txt by rememberSaveable{ mutableStateOf("")}
    val scope = rememberCoroutineScope()
    val me = memberModel.myData.collectAsState().value
    val grp = memberModel.currentGrpChatOpened.collectAsState().value!!

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
                            modifier = Modifier.background(BlueBerry),
                            expanded = moreOptionsExpand,
                            onDismissRequest = { moreOptionsExpand = false },
                            properties = PopupProperties(focusable=false),
                        ) {
                            Row(){
                                SendImgGrp(memberModel = memberModel ,context , txt = txt)
                                SendDocGrp(memberModel = memberModel ,context , txt = txt)
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
                        val msg = MessageToSendGP(timeStuff.uniqueID,txt,timeStuff.date,timeStuff.time,grp.groupID,"--::--msg","","",
                        me.dept,me.designation,me.memberID)
                        scope.launch {
                            memberModel.sendMsgGrp(msg)
                           // memberModel.sendNotification(Notification("","Unread",timeStuff.TS,me.memberID,me.profileDetail.name,grp.groupID,"NGMsg"),"${me.orgID}/${recipient.memberID}/Notifications")
                        }
                        txt = ""

                    }

            },id = R.drawable.aeroplae, btnSize = 35.dp)

    }

}

@Composable
fun SendImgGrp(memberModel: MemberModel, context : Context = LocalContext.current ,txt:String="") {
    // var txt by rememberSaveable{ mutableStateOf("")}
    val me = memberModel.myData.collectAsState().value
    val grp  = memberModel.currentGrpChatOpened.collectAsState().value
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }
    if (imageUri != null && grp!=null) {
        val temp = imageUri!!
        imageUri = null
        LaunchedEffect(key1 = temp) {
            val timeStuff = getTime()
            memberModel.myDataAsSender = me.toChatList(timeStuff.TS)
            val msg = MessageToSendGP(timeStuff.uniqueID,txt.trim(),timeStuff.date,timeStuff.time,grp.groupID,"--::--img",
                "COMM_IMG_${timeStuff.uniqueID}",getFileExtension(temp.toString())!!,
                me.dept,me.designation,me.memberID)
            async {
                memberModel.addImage( temp, context, "",
                    "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Images/Sent",
                    "COMM_IMG_${timeStuff.uniqueID}",
                    "${me.orgID}/Groups/${grp.groupID}/images/COMM_IMG_${timeStuff.uniqueID}",
                    extension = "png",
                    filetype = "img", isGroup = true
                )
            }.await()
            memberModel.sendMsgGrp(msg)

        }
    }
    IconButton(onClick = { launcher.launch("image/*") }) {
        Image(painter = painterResource(id = R.drawable.attach_image) , null , modifier = Modifier.size(68.dp))
    }
}

@Composable
fun SendDocGrp(memberModel: MemberModel, context : Context = LocalContext.current ,txt:String="") {
    // var txt by rememberSaveable{ mutableStateOf("")}
    val me = memberModel.myData.collectAsState().value
    val scope = rememberCoroutineScope()
    val grp  = memberModel.currentGrpChatOpened.collectAsState().value
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? -> imageUri = uri }
    if (imageUri != null && grp!=null) {
        val temp = imageUri!!
        imageUri = null
        LaunchedEffect(key1 = temp) {
            val timeStuff = getTime()
            val msg = MessageToSendGP(timeStuff.uniqueID,txt.trim(),timeStuff.date,timeStuff.time,grp.groupID,"--::--doc",
                "COMM_DOC_${timeStuff.uniqueID}",getFileExtension(temp.toString())!!,
                me.dept,me.designation,me.memberID)
            async {
                memberModel.addImage( temp, context, "",
                    "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/Sent",
                    "COMM_DOC_${timeStuff.uniqueID}",
                    "${me.orgID}/Groups/${grp.groupID}/documents/COMM_DOC_${timeStuff.uniqueID}",
                    extension = getFileExtension(temp.toString())!!,
                    filetype = "doc", isGroup = true
                )
            }.await()
            memberModel.sendMsgGrp(msg)
        }
    }
    IconButton(onClick = { launcher.launch(arrayOf("application/pdf","application/msword","application/vnd.ms-excel", "application/vnd.ms-powerpoint", "text/csv",)) }) {
        Image(painter = painterResource(id = R.drawable.document) , null , modifier = Modifier.size(68.dp))
    }
}


@Composable
fun TopbarGroup(
    memberModel : MemberModel,
    onBackClick:()->Unit,
    onProfileClick:()->Unit,
){
    val group = memberModel.currentGrpChatOpened.collectAsState().value
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
                    MiniImage(onProfileClick = onProfileClick, imgId = R.drawable.sampleprof, size = 45 )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(group?.title?:"", style = MaterialTheme.typography.titleLarge ,  fontWeight = FontWeight.Medium , color = Color.White)
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
fun MsgGrp(msg:MessageToSendGP , color:Color, design:String = "",onLongPress:()->Unit,memberModel: MemberModel){
    val context =LocalContext.current
    val meForFolder = memberModel.myData.collectAsState().value
    val descExpand = rememberSaveable { mutableStateOf(false)  }
    GreyCard(modifier = Modifier
        .padding(3.dp, 0.dp).shadow(Color.Black.copy(alpha= .5f),1.dp, 1.dp,7.dp,30.dp,70.dp), backColor = if(design == "Rec")BlueBerry else MidnightBlue
    ) {
        Column{
            if(design == "Rec"){
                val myOrg  = memberModel.myOrganisation.collectAsState().value
                var expanded by rememberSaveable{mutableStateOf(false)}
                var obj = myOrg.members[msg.whoSendDept]!![msg.whoSendDesig]!![msg.whoSendID]!!
                Text(text = obj.name,color = Color.White, modifier = Modifier.clickable {
                    expanded = true
                }.padding(8.dp,0.dp), fontSize = 13.sp, maxLines = 1)
                if(expanded)
                    OthersProfile(onDismiss = { expanded= false } ,extrasForDP("${obj.memberID} dp",
                        "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/ProfilePics",
                        "${meForFolder.orgID}/${obj.memberID}/dp"
                    ), obj)
            }
            Row(){
                if(design=="Send")Spacer(Modifier.width(1.dp))
                Card(modifier = Modifier,colors = CardDefaults.cardColors(Color.White)) {
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
                if(design=="Rec")Spacer(Modifier.width(5.dp))
            }
            Spacer(Modifier.height(1.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageMsgGrp(msg:MessageToSendGP, color:Color = MidnightBlue, design:String = "Rec" , memberModel: MemberModel,onLongPress:()->Unit){
    val context =LocalContext.current
    val me = memberModel.myData.collectAsState().value
    var uri by rememberSaveable { mutableStateOf<String?>("loading")  }
    val grp = memberModel.currentGrpChatOpened.collectAsState().value
    var scope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit){
        scope.launch(Dispatchers.Default) {
            try{
                uri = memberModel.checkAndFetch(context , msg.extra , "Image")
                Log.d("qwerty","At line 496 Groups $uri")
            }catch(e:Exception){
                Log.d("qwerty",e.message!!)}
        }
    }
    val descExpand = rememberSaveable { mutableStateOf(false)  }
    GreyCard(modifier = Modifier
        .padding(3.dp, 0.dp).shadow(Color.Black.copy(alpha= .5f),1.dp, 1.dp,7.dp,100.dp,100.dp), backColor = if(design == "Rec")BlueBerry else MidnightBlue
    ) {
        Column{
            if(design == "Rec"){
                var expanded by rememberSaveable{mutableStateOf(false)}
                val myOrg  = memberModel.myOrganisation.collectAsState().value
                val obj = myOrg.members[msg.whoSendDept]!![msg.whoSendDesig]!![msg.whoSendID]!!
                Text(text = obj.name,color = Color.White, modifier = Modifier.clickable {
                    expanded = true
                }.padding(8.dp,0.dp), fontSize = 13.sp, maxLines = 1)
                if(expanded)
                    OthersProfile(onDismiss = { expanded= false } ,extrasForDP("${obj.memberID} dp",
                        "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/ProfilePics",
                        "${me.orgID}/${obj.memberID}/dp"
                    ), obj)
            }
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
                                                            val pathToDwnld = if(design == "Send")"Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Images/Sent" else "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Images/${grp?.title}_${grp?.groupID!!.substring(7,13)}"
                                                            memberModel.downloadAndReturnUri(context,pathToDwnld,
                                                                "${me.orgID}/Groups/${grp!!.groupID}/images/${msg.extra}",
                                                                msg.extra,"png","img"
                                                            ).collect{
                                                                if(it!=null){
                                                                    uri = it
                                                                }
                                                            }
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
fun DocMsgGrp(msg:MessageToSendGP,  color:Color = MidnightBlue, design:String = "Rec" , memberModel: MemberModel,onLongPress:()->Unit){
    val context =LocalContext.current
    val me = memberModel.myData.collectAsState().value
    val scope = rememberCoroutineScope()
    val grp = memberModel.currentGrpChatOpened.collectAsState().value
    var uri by rememberSaveable { mutableStateOf<String?>("loading")  }
    LaunchedEffect(key1 = Unit){
        scope.launch(Dispatchers.Default) {
            try{
                uri = memberModel.checkAndFetch(context , msg.extra , "Document")
            }catch(e:Exception){
                Log.d("qwerty",e.message!!)}
        }
    }
    val descExpand = rememberSaveable { mutableStateOf(false)  }
    var size by  rememberSaveable { mutableStateOf(0)  }
    if(uri!=null && uri!= "loading"){
        LaunchedEffect(key1 = Unit, block = {size = uriToArrPdf(context, Uri.parse(uri))!!.size})
    }
    GreyCard(modifier = Modifier
        .padding(3.dp, 0.dp).shadow(Color.Black.copy(alpha= .5f),1.dp, 1.dp,7.dp,100.dp,100.dp), backColor = if(design == "Rec")BlueBerry else MidnightBlue
    ) {
        Column{
            if(design == "Rec"){
                var expanded by rememberSaveable{mutableStateOf(false)}
                val myOrg  = memberModel.myOrganisation.collectAsState().value
                val obj = myOrg.members[msg.whoSendDept]!![msg.whoSendDesig]!![msg.whoSendID]!!
                Text(text = obj.name,color = Color.White, modifier = Modifier.clickable {
                    expanded = true
                }.padding(8.dp,0.dp), fontSize = 13.sp, maxLines = 1)
                if(expanded)
                    OthersProfile(onDismiss = { expanded= false } ,extrasForDP("${obj.memberID} dp",
                        "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/ProfilePics",
                        "${me.orgID}/${obj.memberID}/dp"
                    ), obj)
            }
            Row(){
                var visible by rememberSaveable{mutableStateOf(false)}
                if(visible)
                    ConfirmDialog(onYes = {
                        deleteFileInDirectory(uri!!)
                        uri = null
                        size = 0; TOAST(context,"Deleted"); visible = false }, onNo = { visible = false }, str1 = "Delete Document", str2 = "Delete document ${msg.extra} from device ?")
                IconButton(onClick = {
                    when (uri) {
                        "loading" -> {}
                        null -> {
                            uri = "loading"
                            scope.launch {
                                val pathTo = if(design == "Send")"Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/Sent" else "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/${grp?.title}_${grp?.groupID!!.substring(7,13)}"
                                memberModel.downloadAndReturnUri(context,pathTo,
                                    "${me.orgID}/Groups/${grp?.groupID}/documents/${msg.extra}",
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
                                                        val pathTo = if(design == "Send")"Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/Sent" else "Commuin/${me.profileDetail.organName}_${me.orgID}/${me.profileDetail.name}_${me.memberID}/Documents/${grp?.title}_${grp?.groupID!!.substring(7,13)}"
                                                        memberModel.downloadAndReturnUri(context,pathTo,
                                                            "${me.orgID}/Groups/${grp?.groupID}/documents/${msg.extra}",
                                                            msg.extra,msg.extn,"doc"
                                                        ).collect{
                                                            if(it!=null){
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
                if(design=="Rec")Spacer(Modifier.width(2.dp))
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}

