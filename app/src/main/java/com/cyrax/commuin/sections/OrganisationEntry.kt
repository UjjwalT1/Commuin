package com.cyrax.commuin.sections

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideBarOrg(options:List<Screen>, onExitClick:()->Unit, onItemSelected:(Screen)->Unit, appModel: CommuinModel, navController: NavHostController, memberModel: MemberModel = viewModel()){
    BackHandler( enabled = true ) { onExitClick();}
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedItem = appModel.Curr.value
    var isOut by rememberSaveable{ mutableStateOf(false) }
    if(isOut){
        LogoutDialog(
            onYes = {
                appModel.resetAll()
                navController.popBackStack()
            } ,
            onNo = { isOut = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            Spacer(Modifier.height(12.dp))
            options.forEach { item ->
                NavigationDrawerItem(
                    icon = {
                        if(item == selectedItem) Icon(item.iconActive, contentDescription = null)
                        else Icon(item.iconInactive, contentDescription = null)
                    },
                    label = { Text(item.toString()) },
                    selected = item == selectedItem,
                    onClick = {
                        when(item){
                            Screen.LogOut -> {
                                isOut = true
                            }
                            else -> { scope.launch { drawerState.close() }
                                appModel.setCurr(item)
                                onItemSelected(item) }
                        }

                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

        },
        content = {
            OrgScreen(
                appModel = appModel ,
                onHamClick = { scope.launch { drawerState.open() } } ,
                onProfileClick = { navController.navigate("OrgProfile") }
            )

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OrgScreen(appModel:CommuinModel , onHamClick: () -> Unit , onProfileClick: () -> Unit){

    val curr = appModel.Curr.value.address

    Scaffold(
        topBar = { Topbar(onHamClick, Icons.Outlined.Menu, onProfileClick,null ,curr) },
        bottomBar = {  BottomBar({i ->  appModel.setCurr(i); },curr) },
        floatingActionButton = {
            Fab(currPage=curr) }
    ){
        Surface(modifier = Modifier
            // .background(RainForest.primary)
            .padding(it)){
            OrgNavManager(route = appModel.Curr.value.toString() , appModel = appModel)

        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrgNavManager(appModel : CommuinModel, route:String){
    val context = LocalContext.current

    AnimatedContent(targetState = route,
        transitionSpec = {  fadeIn()  with
                fadeOut(animationSpec = tween(200)) }) {
        Column {
            when(it){
                Screen.HOME.toString() -> FeedHolder()
                Screen.CHATS.toString() -> ChatHolder( )
                Screen.GROUPS.toString() -> GroupHolder()
                Screen.EVENTS.toString() -> EventHolder()
                Screen.Members.toString() -> AddMember(appmodel = appModel)
                Screen.Departments.toString() -> AddDept(appmodel = appModel)
                Screen.Designations.toString() -> AddDesignation(appmodel = appModel)
            }
        }
    }
}

