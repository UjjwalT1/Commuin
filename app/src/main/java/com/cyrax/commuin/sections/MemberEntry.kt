package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyrax.commuin.Live
import com.cyrax.commuin.R
import com.cyrax.commuin.Utils.Directory
import com.cyrax.commuin.Utils.convertTimestampToDate
import com.cyrax.commuin.Utils.splitString
import com.cyrax.commuin.functions.createFolder
import com.cyrax.commuin.functions.createFolderUnder
import com.cyrax.commuin.functions.deleteFileInDirectory
import com.cyrax.commuin.functions.downloadFromByteArray
import com.cyrax.commuin.functions.storePdfFromByteArray
import com.cyrax.commuin.struct.AppData
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.LiveMsgModel
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.groupDetailObj
import com.cyrax.commuin.struct.supplemntIcon
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.RainForest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideBarMem(options:List<Screen>, onExitClick:()->Unit, onItemSelected:(Screen)->Unit, appModel: CommuinModel , navController: NavHostController, memberModel: MemberModel){

    val context = LocalContext.current
    BackHandler( enabled = true ) { (context as? Activity)?.finishAffinity()}
    val meForFolder = memberModel.myData.collectAsState().value
    if(meForFolder.profileDetail.organName != "")
        LaunchedEffect(key1 = Unit){
          //  createFolderUnder(context ,"Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/Images" )
            createFolder(context , "Sent" , "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/Images")
            createFolder(context , "Sent" , "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/Documents")
           createFolder(context , "Sent" , "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/Videos")
            createFolder(context , "Sent" , "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/Audios")
            createFolder(context , "ProfilePics" , "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}")
            //createFolder(context , "Groups" , "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}")

        }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedItem = memberModel.Curr.value
    var isOut by rememberSaveable{ mutableStateOf(false) }

    LaunchedEffect(key1 = Unit ){
        memberModel.setMyData((appModel.uiState as AppData.Member).appState)
        memberModel.updateOnScreenPeople()
        memberModel.populateAccName()
    }
    lateinit var exitFun:()->Unit
    if(isOut){
        LogoutDialog(
            onYes = {
               memberModel.resetAll()
               exitFun()
                navController.navigate("Option"){
                    popUpTo(navController.currentBackStackEntry?.destination?.route!!){ inclusive = true }
                }

            } ,
            onNo = { isOut = false }
        )
    }
    ModalNavigationDrawer(
        drawerContainerColor= MidnightBlue,
        drawerState = drawerState,
        drawerContent = {

            Spacer(Modifier.height(12.dp))
            options.forEach { item ->
                NavigationDrawerItem(
                    colors = NavigationDrawerItemDefaults.colors(BlueBerry, Color.Transparent, selectedTextColor= Color.White),
                    icon = {
                        if(item == selectedItem) Image(painter = painterResource(supplemntIcon[item.address]!!) , null , modifier = Modifier.size(30.dp), colorFilter = ColorFilter.tint(
                            Color.Gray))
                        else  Image(painter = painterResource(supplemntIcon[item.address]!!) , null , modifier = Modifier.size(30.dp))
                    },
                    label = { Text(item.toString() , color = Periwinkle , fontSize = 20.sp , modifier = Modifier.padding(10.dp,0.dp) ) },
                    selected = item == selectedItem,
                    onClick = {
                        when(item){
                            Screen.LogOut -> {
                                isOut = true
                            }
                            else -> { scope.launch { drawerState.close() }
                                if(item.address != "currentChatOpened")
                                memberModel.setCurr(item)
                                onItemSelected(item)
                            }
                        }

                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            Spacer(modifier = Modifier.height(70.dp))


        },
        content = {
            memberModel.myOrganisation.update { it.copy(members = appModel.myOrganisation.collectAsState().value.members)   }
            exitFun = MemberScreen(appModel = appModel ,memberModel = memberModel , onHamClick = { scope.launch { drawerState.open() } } )

        }
    ,
        gesturesEnabled = memberModel.Curr.value.address != "PROFILE" && memberModel.Curr.value.address != "Create Event"&&
                memberModel.Curr.value.address != "Create Group" && memberModel.Curr.value != Screen.EEVENTS
    )
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MemberScreen(appModel: CommuinModel, memberModel: MemberModel , onHamClick: () -> Unit):() -> Unit{
    val context = LocalContext.current
    val curr = memberModel.Curr.value.address
    val scope = rememberCoroutineScope()
    val navController2 = rememberNavController()
    val myData = memberModel.myData.collectAsState().value
    val eventData = memberModel.currentEventOpened.collectAsState().value
    val exitFun = fun (){
        navController2.popBackStack(navController2.currentBackStackEntry?.destination?.route!!,true)
    }
    when(curr){
        "My Organisation" -> {
            LaunchedEffect(key1 = Unit ){navController2.navigate(curr){ popUpTo(navController2.currentBackStackEntry?.destination?.route!!){ inclusive = true } } }
        }
        "Files" -> {
            LaunchedEffect(key1 = Unit ){
                navController2.navigate(curr)
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedContent(targetState = curr , transitionSpec = { fadeIn() with fadeOut() }) {
                Log.d("qwerty",it)
                if(it == "Files"){}
                else if(memberModel.Curr.value == Screen.EEVENTS){
                    NewTopBar(icon1 = R.drawable.cro_ss,
                        icon2 = if(eventData.whoUploaded.memberID == myData.memberID) R.drawable.more else R.drawable.idea
                        , onClickFirst = { memberModel.setCurr(memberModel.prevCurr); navController2.popBackStack() }, onClickLast = {
                            if(getTime().TS >= memberModel.eventNotification.Edate ){
                                TOAST(context,"Event already expired.")
                            }
                            else if(getTime().TS >= memberModel.eventNotification.Edate - 18_000_000 ){
                                TOAST(context,"Attend Right Away! Event expiring in less than 5Hrs.")
                            }
                            else{
                                setNoteReminder(context,memberModel.eventNotification.description   , memberModel.eventNotification.Edate-18_000_000 );
                                TOAST(context , "Event Set for ${convertTimestampToDate(memberModel.eventNotification.Edate-18_000_000)} ","SHORT");
                                saveTextToNotesApp(memberModel.eventNotification.description + "\n\nExpiring on ${convertTimestampToDate(memberModel.eventNotification.Edate)}",context)
                            }
                        },
                        title = it,list = listOf(
                            Pair("DELETE") { memberModel.deleteEvent(memberModel.currentEventOpened.value.eventID);
                                memberModel.setCurr(memberModel.prevCurr); navController2.popBackStack() },
                            Pair("UPDATE",{ TOAST(context , "To be implemented later")}),
                            Pair("Set Notes"){
                                if(getTime().TS >= memberModel.eventNotification.Edate ){
                                    TOAST(context,"Event already expired.")
                                }
                                else if(getTime().TS >= memberModel.eventNotification.Edate - 18_000_000 ){
                                    TOAST(context,"Attend Right Away! Event expiring in less than 5Hrs.")
                                }
                                else{
                                    setNoteReminder(context,memberModel.eventNotification.description   , memberModel.eventNotification.Edate-18_000_000 );
                                    TOAST(context , "Event Set for ${convertTimestampToDate(memberModel.eventNotification.Edate-18_000_000)} ","SHORT");
                                    saveTextToNotesApp(memberModel.eventNotification.description + "\n\nExpiring on ${convertTimestampToDate(memberModel.eventNotification.Edate)}",context)
                                }

                            }
                        )
                    )
                }
                else if(it == "PROFILE"){
                    NewTopBar(icon1 = R.drawable.left_chevron, icon2 = R.drawable.more
                        , onClickFirst = { memberModel.setCurr(Screen.HOME); navController2.popBackStack() }, onClickLast = {},
                        title = it,list = listOf(
                            Pair("Download Timeline",{ TOAST(context , "TimeLine Later Implementation"); navController2.navigate("Live")})
                        )
                    )
                }
                else if(it == "Create Event"){
                    var visible by rememberSaveable{ mutableStateOf(false) }
                    if(visible)
                    ConfirmDialog(onYes = {
                        memberModel.setCurr(Screen.EVENTS); navController2.popBackStack() ;
                        scope.launch(Dispatchers.IO) {
                        try{memberModel.uploadEvent(); TOAST(context, "Creating ...")  }
                        catch(_:Exception){memberModel.resetEventForm(); TOAST(context, "Try Again!\nEmpty Duration Fields !" , "Long")}

                        }
                        visible = false

                    }, onNo = { visible = false }, str1 = "Create Event", str2 ="Please recheck the details! Especially visibility. " )
                    NewTopBar(icon1 = R.drawable.cro_ss, icon2 = R.drawable.tasks
                        , onClickFirst = {memberModel.resetEventForm();  memberModel.setCurr(Screen.EVENTS);  navController2.popBackStack() },
                        onClickLast = { visible = true },
                        title = it)
                }
                else if(it == "Create Group"){
                    var visible by rememberSaveable{ mutableStateOf(false) }
                    if(visible)
                        ConfirmDialog(onYes = {
                            memberModel.setCurr(Screen.GROUPS); navController2.popBackStack() ;
                            scope.launch(Dispatchers.IO) {
                                try{memberModel.updateGform("creating the group",99); TOAST(context, "Creating ...")  }
                                catch(e:Exception){memberModel.resetEventForm(); TOAST(context, "Error");Log.d("qwerty",e.message?:"")}

                            }
                            visible = false

                        }, onNo = { visible = false }, str1 = "Create Group", str2 ="Please recheck the details! Especially group members. Group DP and leaders can be set by you once its created." )
                    NewTopBar(icon1 = R.drawable.cro_ss, icon2 = R.drawable.tasks
                        , onClickFirst = {memberModel.resetEventForm();  memberModel.setCurr(Screen.GROUPS);  navController2.popBackStack() },
                        onClickLast = { visible = true },
                        title = it)
                }
                else if(it == "GroupConvList"){
                    TopbarGroup(memberModel, onBackClick = {memberModel.CurrChatJob!!.cancel(); memberModel.setCurr(Screen.GROUPS)
                        memberModel.setCurrGrpChatOpened(null); navController2.popBackStack() }, onProfileClick = { /*TODO*/ } )
                }
                else if(memberModel.currentChatOpened.collectAsState().value == null && it == "HOME"){
                    val meForFolder = memberModel.myData.collectAsState().value
                    NewTopBar(icon1 = R.drawable.hham, icon2 = R.drawable.sampleprof
                        , onClickFirst = onHamClick, onClickLast = { memberModel.setCurr(Screen.PROFILE); navController2.navigate("MemProfile")},
                        title = it , isHome = true , extra = extrasForDP("${meForFolder.memberID} dp",
                            pathTo = "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}",
                            pathFrom = "${meForFolder.orgID}/${meForFolder.memberID}/dp")
                    )
                }

                else if(memberModel.currentChatOpened.collectAsState().value == null)
                    NewTopBarSearch(icon1 = R.drawable.hham, icon2 = R.drawable.search
                        , onClickFirst = onHamClick, onClickLast = { },
                        title = it , memberModel = memberModel , it)
              //      TopbarSearchable(memberModel,onHamClick,Icons.Outlined.Menu,{},Icons.Filled.Search ,it)
                else  TopbarChat(memberModel, onBackClick = {memberModel.markAsRead(memberModel.currentChatOpened.value)
                    memberModel.CurrChatJob!!.cancel();
                    memberModel.setCurrChatOpened(null); navController2.popBackStack() }, onProfileClick = { /*TODO*/ } )
//                    NewTopBarSearch(icon1 = R.drawable.hham, icon2 = R.drawable.search
//                    , onClickFirst = onHamClick, onClickLast = { },
//                    title = it , memberModel = memberModel , it)
             //   TopbarChat(memberModel, onBackClick = {memberModel.setCurrChatOpened(null); navController2.popBackStack() }, onProfileClick = { /*TODO*/ } )
            }

                 },
        bottomBar = {
            if(curr == "PROFILE" || curr == "Files" || curr == "Create Event" || curr == "Create Group" || memberModel.Curr.value == Screen.EEVENTS){} //we dont want bottom bar for Profile and Files page
            else if(memberModel.currentChatOpened.collectAsState().value == null && curr != "GroupConvList")
                BottomBar(
            onItemSelected = { i ->  memberModel.setCurr(i);
                navController2.navigate(i.address){ popUpTo(navController2.currentBackStackEntry?.destination?.route!!){ inclusive = true } }
            },curr)
            else if(memberModel.currentChatOpened.collectAsState().value != null)SendBox( memberModel )
            else if(memberModel.currentGrpChatOpened.collectAsState().value != null)SendBoxGrp(memberModel = memberModel)
                    },
        floatingActionButton = {
            if(memberModel.currentChatOpened.collectAsState().value == null && curr == "CHATS")
                Fab(onFabClick = { navController2.navigate("SearchMembers") }, currPage=curr)
            else if(memberModel.currentChatOpened.collectAsState().value == null && curr == "EVENTS")
                Fab(onFabClick = { navController2.navigate("EventCreationForm"); memberModel.setCurr(Screen.CEVENTS) }, currPage=curr)
            else if(memberModel.currentChatOpened.collectAsState().value == null && curr == "GROUPS")
                Fab(onFabClick = { navController2.navigate("CreateGroupForm"); memberModel.setCurr(Screen.CGROUPS) }, currPage=curr)
            else if(memberModel.currentChatOpened.collectAsState().value == null && memberModel.Curr.value == Screen.EEVENTS )
                Fab(onFabClick = { /*TODO when edit event is clicked ||-> navController2.navigate("EventCreationForm"); memberModel.setCurr(Screen.CEVENTS)*/ }, currPage=curr)
        }
    ){
        Surface(modifier = Modifier
            // .background(RainForest.primary)
            .padding(it)){

            NavHost(navController = navController2, startDestination = "HOME"){
                composable("HOME"){
                    FeedHolder(memberModel,navController2)
                }
                composable("CHATS"){

                    ChatHolder(navController2  = navController2 , memberModel)
                }

                composable("currentChatOpened"){
                    ConvList( navController2  = navController2 , memberModel = memberModel , onBackClick = { memberModel.setCurrChatOpened(null); navController2.popBackStack() } )
                }
                composable("GROUPS"){
                    LaunchedEffect(key1 = Unit){
                        scope.launch (Dispatchers.IO){
                            memberModel.getMyGroups()
                        }
                    }
                    GroupHolder(navController2,memberModel = memberModel)
                }
                composable("CreateGroupForm"){
                    CreateGroupForm(memberModel = memberModel, navController2 )
                }
                composable("GroupConvList"){
                    GroupConvList(navController2 , {  navController2.popBackStack();  memberModel.setCurr(Screen.GROUPS); memberModel.setCurrGrpChatOpened(null); } ,memberModel)
                }
                composable("EVENTS"){
                    EventHolder(memberModel,navController2)

                }
                composable("My Organisation"){
                    MyOrganisationComposable(memberModel = memberModel , navController2 =navController2)
                }
                composable("SearchMembers"){
                    SearchDialog(memberModel = memberModel , navController2 =navController2 , onNo = {})
                }
                composable("MemProfile"){
                    MemProfile(obj = memberModel.myData.collectAsState().value , memberModel = memberModel  , onBack = { memberModel.setCurr(Screen.HOME); navController2.popBackStack() })
                }

                composable("Files"){
                    Directory(memberModel = memberModel , { memberModel.setCurr(memberModel.prevCurr);  navController2.popBackStack()})
                }

                composable("EventCreationForm"){
                    EventForm(memberModel = memberModel, navController2)
                }

                composable("EventView"){
                    EventView(memberModel = memberModel, navController2)
                }

                composable("Live"){
                    val vM: LiveMsgModel = viewModel()
                    Live(memberModel,vM)
                }
            }
        }
    }
    return exitFun
}


