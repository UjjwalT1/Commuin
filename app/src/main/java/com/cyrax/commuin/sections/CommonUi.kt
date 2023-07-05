package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cyrax.commuin.R
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.RainForest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.struct.AppData
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.Login
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.bottomBarIcons
import com.cyrax.commuin.struct.classMemSignUp
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.createMemState
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.formIcons
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//This Composable is responsible for the small image you see on each persons chat
@Composable
fun MiniImage(onProfileClick:()->Unit, imgId:Int , size:Int, asyncc:Any? = false,memberModel:MemberModel = viewModel()){
    IconButton(onClick = { onProfileClick() },
        modifier = Modifier
            .size(size.dp)
    ) {
        if(asyncc is Boolean && asyncc == false)
        Image(
           // model="https://firebasestorage.googleapis.com/v0/b/commuin.appspot.com/o/dp%2FAmishdp?alt=media&token=65090b41-e39d-476e-b911-1ffab54e8a1a",
            painter = painterResource(id = imgId ),
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(50))
                ,
            contentScale = ContentScale.Crop
        )
        else if(asyncc is extrasForDP){
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            var uri  by rememberSaveable{ mutableStateOf<String?>(null) }
            if(uri == null && asyncc.ID.isNotBlank()){
                LaunchedEffect(key1 = Unit){
                    scope.launch(Dispatchers.IO) {
                       uri =  memberModel.getImage(context , ID = asyncc.ID,
                            pathTo = asyncc.pathTo,
                            pathfrom = asyncc.pathFrom ,
                            filetype = "img" , extension = "png"
                        )
                    }
                }
            }
            Log.d("qwerty",uri?:"$asyncc")
            AsyncImage(
                model=if(uri==null || uri!!.isBlank())"https://firebasestorage.googleapis.com/v0/b/commu-in-e20d8.appspot.com/o/placeholder.jpg?alt=media&token=3aff3d77-e734-41fb-9916-1ad9acbaf120" else uri,
                //  painter = painterResource(id = imgId ),
                contentDescription = null,
                modifier = Modifier
                    .size(size.dp)
                    .shadow(Color.Black.copy(alpha = .8f), 2.dp, 2.dp, 6.dp, size)
                    .clip(RoundedCornerShape(49))
                    .shadow(Color.Black.copy(alpha = .8f), 2.dp, 2.dp, 6.dp, size),
                contentScale = ContentScale.Crop
            )

        }
    }
}


//This Composable is responsible for The text that appears beside the image
@Composable
fun MiniText(name:String, style1: TextStyle,weight1:FontWeight , position:String, style2:TextStyle, modifier: Modifier = Modifier  , department:String="" , color:Color = Color.Black){
     Column(modifier= modifier){
            Text(name, style = style1, fontFamily = Jost , fontWeight = weight1 , color = color)
            Text("$department | $position", style = style2, fontFamily = Jost , fontWeight = FontWeight.Normal , color = color)


     }

}


//This Composable is the Final Stuff that makes the card that contains both image, text beside it and the color of the card view etc
//It is the button that is clicked to goto a persons chat.
@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageNameClickable(
    memberModel:MemberModel,
    navController2 : NavHostController ,
    onProfileClick:()->Unit={},
    onNameClick:()->Unit={},
    imgId:Int=R.drawable.sampleprof, obj: classOrgMember = classOrgMember()
){
    val meForFolder = memberModel.myData.collectAsState().value
    Card(onClick = onNameClick,modifier= Modifier
        .fillMaxWidth()
        .padding(1.dp, 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation= 3.dp),
        colors = CardDefaults.cardColors(RainForest.onSurface)) {
        Row(modifier= Modifier
            .fillMaxWidth()
            .padding(2.dp, 8.dp)
        ){
            var expanded by rememberSaveable{mutableStateOf(false)}
            Spacer(Modifier.width(10.dp))
            MiniImage(onProfileClick = { expanded = true } ,imgId = imgId , size = 55 , asyncc = extrasForDP("${obj.memberID} dp",
                "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/ProfilePics",
                "${meForFolder.orgID}/${obj.memberID}/dp"
            ))
            if(expanded)
            OthersProfile(onDismiss = { expanded= false } ,extrasForDP("${obj.memberID} dp",
                "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/ProfilePics",
                "${meForFolder.orgID}/${obj.memberID}/dp"
            ), obj)
            Spacer(Modifier.width(20.dp))
            MiniText(
                name =  obj.name, style1 = MaterialTheme.typography.titleLarge , weight1=FontWeight.Normal,
                position = obj.designation, style2 = MaterialTheme.typography.bodyLarge,
                Modifier.align(Alignment.CenterVertically) , department = obj.dept )
        }


    }
}



@Composable
fun OTP(onVerifyClick:(String)->Unit ){
    var otp = rememberSaveable { mutableStateOf("") }
    Column(modifier= Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)){

        OutlinedTextField(value = otp.value , onValueChange ={otp.value = it} ,label = { Text("OTP") } , keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Number))
        Button(onClick = {
            onVerifyClick(otp.value)
        }) {
            Text("VERIFY",color= Color.White)
        }
    }
}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(valueOnDisplay: String,appmodel: CommuinModel ,query:String , callback : (String)->Unit){

    val options = if(appmodel.dAndDObj is createMemState.Success) {
        if(query=="Department") (appmodel.dAndDObj as createMemState.Success).dandD.depts
        else{ (appmodel.dAndDObj as createMemState.Success).dandD.designations }
    }
    else mutableListOf<String>()

    var isExpanded by rememberSaveable { mutableStateOf(false)  }
    ExposedDropdownMenuBox(expanded = isExpanded , onExpandedChange = { isExpanded = !isExpanded} ) {

        OutlinedTextField(
            modifier = Modifier
                .width(310.dp)
                .height(57.dp),
            shape = RoundedCornerShape(50),
            readOnly = true,
            value = valueOnDisplay,//if(query=="Department")appmodel.mem.collectAsState().value.dept else appmodel.mem.collectAsState().value.designation,
            onValueChange = {},
            placeholder = { Text(query)},
        trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)},
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            leadingIcon = { Image(painter = painterResource(id = formIcons[query]!!) , null , modifier = Modifier.size(28.dp)) }
        
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
            }
        ) {
            options.forEach {
                DropdownMenuItem(
                    onClick = {
                        callback(it)
                        isExpanded = false
                    },
                text={
                        Text(it)
                    }
                )
            }
        }
    }
}

@Composable
fun InfoDialog(info:String){

    Dialog(onDismissRequest = {  }) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .size(200.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightBlue)
            ){
            Spinner(40.dp)
            Spacer(Modifier.height(20.dp))
            Text(info , fontFamily = Jost , fontWeight = FontWeight.Normal , style= MaterialTheme.typography.headlineSmall , color = Color.White)
        }
    }

}

@Composable
fun LogoutDialog(onYes:()->Unit,onNo:()->Unit){

    Dialog(onDismissRequest = { onNo()  }) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .size(300.dp, 200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightBlue)
        ){
            Column( modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)) {
                Text( "Log Out !", fontFamily = Jost , fontWeight = FontWeight.Normal , style= MaterialTheme.typography.headlineSmall,color = Color.White)
                Divider(color = Color.White, thickness = 1.dp , modifier = Modifier.padding(0.dp,5.dp))
                Row(){
                    Text("Are You Sure ?" , fontFamily = Jost , fontWeight = FontWeight.Normal , style= MaterialTheme.typography.bodyLarge ,color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(){
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { onYes() } , colors = ButtonDefaults.buttonColors(containerColor = BlueBerry)) {
                        Text("Yes",color = Color.White,fontWeight = FontWeight.Medium)
                    }

                }
            }



        }
    }

}


@Composable
fun ConfirmDialog(onYes:()->Unit,onNo:()->Unit,str1:String,str2:String){
    Dialog(onDismissRequest = { onNo()  }) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .size(300.dp, 200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightBlue)
        ){
            Column( modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)) {
                Text( str1, fontFamily = Jost , fontWeight = FontWeight.Normal , style= MaterialTheme.typography.headlineSmall,color = Color.White)
                Divider(color = Color.White, thickness = 1.dp , modifier = Modifier.padding(0.dp,5.dp))
                Row(){
                    Text(str2 , fontFamily = Jost , fontWeight = FontWeight.Normal , style= MaterialTheme.typography.bodyLarge ,color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(){
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { onYes() } , colors = ButtonDefaults.buttonColors(containerColor = BlueBerry)) {
                        Text("Yes",color = Color.White,fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}


@Composable
fun ProgressBarDemo(progress:Float , Colour:Color = Color.Gray ,Color2:Color = Color.Black , width: Dp) {

    Column() {
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(width)
                .height(1.dp)
                .clip(RoundedCornerShape(50)),
            color = Colour,
            trackColor = Color2
        )
        if(progress < 1f)
            Row(modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(width)){
                Spacer(Modifier.weight(1f))
                Text("${progress*100} %" , color = Colour)
            }
        Spacer(modifier = Modifier.width(10.dp))
    }

}

@Composable
fun NewTopBarSearch(icon1:Int = R.drawable.hham, icon2:Int= R.drawable.search, onClickFirst:()->Unit, onClickLast:()->Unit, title:String ="Sample",
                    memberModel: MemberModel, curr:String,
){
    var search by rememberSaveable{ mutableStateOf(false) }
    val query = memberModel.topBarQuery.collectAsState().value
    val context = LocalContext.current
    val focusRequester = remember{ FocusRequester() }
    val scope = rememberCoroutineScope()
    Row(modifier= Modifier
        .fillMaxWidth()
        .background(MidnightBlue)
        .padding(0.dp, 5.dp, 0.dp, 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
        BrightWithButton(height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f), textColor = Periwinkle,
            shape = RoundedCornerShape(0.dp,40.dp,40.dp,0.dp),
            onClick =
            {
                if(!search) onClickFirst()
                else{
                    search = false
                    memberModel.setTBQuery("")
                }
            },
            id = if(!search)icon1 else R.drawable.cro_ss, btnSize = 30.dp )

        Spacer(modifier=Modifier.width(0.dp))
        if(search == false)
            BrightWithButton(text = title,height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f), textColor = Periwinkle,
            shape = RoundedCornerShape(40.dp,0.dp,0.dp,40.dp),
            onClick =
            { search = !search;
                if(search == false){
                    memberModel.setTBQuery("")
                }
                scope.launch {
                    delay(100)
                    focusRequester.requestFocus()
                }
            },
            id = icon2, btnSize = 30.dp)
        else{
            Searcha(memberModel = memberModel,height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f),
                shape = RoundedCornerShape(25.dp,0.dp,0.dp,25.dp),
                onClick =
                {
                    if(!search) onClickFirst()
                    else{
                        search = false
                        memberModel.setTBQuery("")
                    }
                },
                id = icon2 , btnSize = 30.dp , curr= curr ,focusRequester = focusRequester,query = query)

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searcha( memberModel: MemberModel,height:Dp, boxcolor:Color, boxshadowColor:Color, shape: RoundedCornerShape = RoundedCornerShape(10.dp,10.dp,10.dp,10.dp),
                      onClick:()->Unit, id:Int, btnSize:Dp  , curr:String , focusRequester: FocusRequester,query: String
){
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = shape ,
        modifier = Modifier
            .widthIn(65.dp, 300.dp)
            .height(height)
            .shadow(boxshadowColor, 1.dp, 1.dp, 13.dp, 70.dp, 70.dp)
            .clip(shape)){
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically){
            ExtnForSearch(memberModel = memberModel, curr = curr , focusRequester = focusRequester , query = query )
            IconButton(onClick = onClick,modifier = Modifier
            ) {
                Image(painter = painterResource(id = id), contentDescription = null , modifier = Modifier.size(btnSize))
            }
        }
        Spacer(modifier = Modifier.weight(1f))

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExtnForSearch(memberModel: MemberModel, context: Context= LocalContext.current, curr:String, focusRequester: FocusRequester, query: String){
    AnimatedContent(targetState = curr,
        transitionSpec = {  scaleIn() + fadeIn() with  scaleOut(tween(100)) + fadeOut(tween(100)) }) {
        Row(modifier = Modifier
            .widthIn(50.dp, 255.dp)
            .padding(10.dp, 0.dp)
            .border(width = 1.dp, color = Periwinkle, RoundedCornerShape(50))){
            BasicTextField(value = query,
                modifier = Modifier
                    .height(35.dp)
                    .fillMaxWidth()
                    .padding(18.dp, 5.dp, 6.dp, 0.dp)
                    .focusRequester(focusRequester)
                ,
                textStyle = TextStyle.Default.copy(fontSize = 17.sp , color = Periwinkle),
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

@Composable
fun OthersProfile(onDismiss:()->Unit, extrasForDP: extrasForDP = extrasForDP() ,data: classOrgMember){
    Dialog( onDismissRequest = onDismiss ) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(BlueBerry), horizontalAlignment = Alignment.CenterHorizontally , verticalArrangement = Arrangement.SpaceAround){
            Spacer(modifier = Modifier.height(15.dp))
            RefImage(onProfileClick = {}, imgId = R.drawable.sampleprof , size = 150, asyncc = extrasForDP  )
            Spacer(modifier = Modifier.height(15.dp))
            ProfRow(Modifier,"ID",data.memberID)
            ProfRow(Modifier,"Name",data.name)
            ProfRow(Modifier,"Department",data.dept)
            ProfRow(Modifier,"Designation",data.designation)
            ProfRow(Modifier,"Email",data.email,size2=17.sp)
            ProfRow(Modifier,"Contact",data.contact)
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun RefImage(onProfileClick:()->Unit, imgId:Int , size:Int, asyncc:Any? = false,memberModel:MemberModel = viewModel()){
    IconButton(onClick = { onProfileClick() },
        modifier = Modifier
            .size(size.dp)
    ) {
        if(asyncc is Boolean && asyncc == false)
            Image(
                // model="https://firebasestorage.googleapis.com/v0/b/commuin.appspot.com/o/dp%2FAmishdp?alt=media&token=65090b41-e39d-476e-b911-1ffab54e8a1a",
                painter = painterResource(id = imgId ),
                contentDescription = null,
                modifier = Modifier
                    .size(size.dp)
                    .clip(RoundedCornerShape(50))
                ,
                contentScale = ContentScale.Crop
            )
        else if(asyncc is extrasForDP){
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            var uri  by rememberSaveable{ mutableStateOf<String?>("loading") }
            if(uri == "loading" && asyncc.ID.isNotBlank()){
                LaunchedEffect(key1 = Unit){
                    scope.launch(Dispatchers.IO) {
                        uri =  memberModel.refreshImage(context , ID = asyncc.ID,
                            pathTo = asyncc.pathTo,
                            pathfrom = asyncc.pathFrom ,
                            filetype = "img" , extension = "png"
                        )
                    }
                }
            }
            Log.d("qwerty",uri?:"$asyncc")
            if(uri == "loading"){Spinner(40.dp)}
                else
            AsyncImage(
                model=if(uri==null)"https://firebasestorage.googleapis.com/v0/b/commu-in-e20d8.appspot.com/o/placeholder.jpg?alt=media&token=3aff3d77-e734-41fb-9916-1ad9acbaf120" else uri,
                //  painter = painterResource(id = imgId ),
                contentDescription = null,
                modifier = Modifier
                    .size(size.dp)
                    .shadow(Color.Black.copy(alpha = .8f), 2.dp, 2.dp, 6.dp, size)
                    .clip(RoundedCornerShape(49))
                    .shadow(Color.Black.copy(alpha = .8f), 2.dp, 2.dp, 6.dp, size),
                contentScale = ContentScale.Crop
            )

        }
    }
}


