package com.cyrax.commuin.sections


import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyrax.commuin.R
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.struct.AppData
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.Login
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.bottomBarIcons
import com.cyrax.commuin.struct.sidebarItemsMem
import com.cyrax.commuin.struct.sidebarItemsOrg
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.RainForest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



//TOP BAR
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Topbar(
    onHamClick:()->Unit,
    LMSvector: ImageVector,
    onProfileClick:()->Unit,
    RMSvector : ImageVector? = null ,
    curr: String
){
    LargeTopAppBar(colors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = RainForest.secondary),
        modifier = Modifier
            .height(52.dp)
            .shadow(Color.Black.copy(alpha = 1f), 0.dp, 0.dp, 15.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .shadow(4.dp, ambientColor = Color.Red, spotColor = Color.Red)
            .imePadding(),
        title = {
           AnimatedContent(targetState = curr,
               transitionSpec = {  scaleIn() +fadeIn() with  scaleOut(tween(100))+fadeOut(tween(100)) }) {
               Text(text = it,color= Color.Black, fontFamily = Jost,fontSize =24.sp, fontWeight = FontWeight.SemiBold)}
           },
        navigationIcon = {
            Row{
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick =  onHamClick ) {
                    Icon(
                        imageVector = LMSvector ,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(39.dp)
                    )
                }

                Spacer(modifier = Modifier.width(23.dp))
            }},
        actions={
            Row{
                if(RMSvector != null){
                    IconButton(onClick = onProfileClick,modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .size(40.dp)
                        .background(Color.Transparent) ){
                        Icon(imageVector = RMSvector, contentDescription = null)
                    }
                }
                else
                    MiniImage(onProfileClick = onProfileClick, imgId = R.drawable.sampleprof, size = 40)
                Spacer(modifier = Modifier.width(10.dp))
            } }
    )

}



//TOP BAR
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TopbarSearchable(
    memberModel: MemberModel ,
    onHamClick:()->Unit,
    LMSvector: ImageVector,
    onProfileClick:()->Unit,
    RMSvector : ImageVector? = null ,
    curr: String
){
    var search by rememberSaveable{ mutableStateOf(false) }
    val query = memberModel.topBarQuery.collectAsState().value
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    LargeTopAppBar(colors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = RainForest.secondary),
        modifier = Modifier
            .height(52.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        title = {
            AnimatedContent(targetState = curr,
                transitionSpec = {  scaleIn() +fadeIn() with  scaleOut(tween(100))+fadeOut(tween(100)) }) {
                if(search == false)Text(text = it,color= Color.Black, fontFamily = Jost,fontSize =24.sp, fontWeight = FontWeight.SemiBold)
                else{
                    Row(modifier = Modifier.fillMaxWidth().border(width = 1.5.dp, color = Color.Black, RoundedCornerShape(50))){
                    BasicTextField(value = query,
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .padding(8.dp, 5.dp, 6.dp, 0.dp)
                            .focusRequester(focusRequester)
                        ,
                        textStyle = TextStyle.Default.copy(fontSize = 19.sp),
                        onValueChange = {
                            if(it.length == 20 && query.length==19  ) TOAST(context,"Query length reached!")
                            else {
                                memberModel.setTBQuery(it)
                                memberModel.queriedPeopleListOnScreen.clear()
                                memberModel.queriedPeopleListOnScreen.addAll( searchAmongList(it,memberModel.peopleListOnScreen))
                            }
                        }
                    )
                    }
                }
            }
        },
        navigationIcon = {
            Row{
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = {
                    if(!search) onHamClick()
                    else{
                        search = false
                        memberModel.setTBQuery("")
                    }
                }  ) {
                    Icon(
                        imageVector = if(!search) LMSvector else Icons.Filled.Close ,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(39.dp)
                    )
                }

                Spacer(modifier = Modifier.width(23.dp))
            }},
        actions={
            Row{

                if(RMSvector != null){
                    IconButton(onClick = { search = !search;
                        if(search == false){
                            memberModel.setTBQuery("")
                        }
                        scope.launch {
                        delay(100)
                        focusRequester.requestFocus()
                    } },modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .size(40.dp)
                        .background(Color.Transparent) ){
                        Icon(imageVector = RMSvector, contentDescription = null)
                    }
                }
                else
                    MiniImage(onProfileClick = onProfileClick, imgId = R.drawable.sampleprof, size = 40)
                Spacer(modifier = Modifier.width(10.dp))
            } }
    )

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    onItemSelected:(Screen)->Unit={},
    currClass:String,
    height:Dp = 60.dp, boxcolor:Color = MidnightBlue, boxshadowColor:Color = Color.White.copy(alpha = .5f), shape: RoundedCornerShape = RoundedCornerShape(35.dp,35.dp,35.dp,35.dp),
    ){
    val icons= listOf(Screen.HOME,Screen.CHATS,Screen.GROUPS,Screen.EVENTS)


    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            ){
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceEvenly ,
            modifier = Modifier.fillMaxWidth(.95f).height(50.dp).shadow(boxshadowColor, 1.dp, 1.dp, 8.dp, 70.dp, 70.dp).clip(shape).background(boxcolor)
                .align(Alignment.CenterHorizontally)
        ) {

            icons.forEach { icon ->
                NavigationBarItem(
                    selected = currClass == icon.address,
                    onClick = { onItemSelected(icon) },
                    icon = {
                        Column {
                            if (currClass == icon.address)
                                Image(painter = painterResource(id = bottomBarIcons[icon.name]!!), contentDescription = null , modifier = Modifier.size(30.dp))
                            else Image(painter = painterResource(id = bottomBarIcons[icon.name]!!), contentDescription = null , modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = BlueBerry)

                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


//Floating action Button
@Composable
fun Fab(onFabClick:()->Unit={},
currPage:String){
    when (currPage) {
        "EVENTS" -> FloatingActionButton(onClick = onFabClick, containerColor = IndigoInk) {
            Text(
                modifier = Modifier.padding(19.dp, 0.dp),
                text = "Add Event",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                color = Periwinkle
            )
        }

        "GROUPS" -> FloatingActionButton(onClick = onFabClick, containerColor = IndigoInk) {
            Text(
                modifier = Modifier.padding(19.dp, 0.dp),
                text = "Create Group",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Jost,
                fontWeight = FontWeight.Normal,
                color = Periwinkle
            )
        }

        "CHATS" -> FloatingActionButton(onClick = onFabClick, containerColor = IndigoInk) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null , tint = Periwinkle)
        }

        else -> {}
    }


}




