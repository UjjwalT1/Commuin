package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cyrax.commuin.R
import com.cyrax.commuin.functions.Ssend
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.struct.AppData
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.Login
import com.cyrax.commuin.struct.RequestStatus
import com.cyrax.commuin.struct.SavedMember
import com.cyrax.commuin.struct.classOrgSignUp
import com.cyrax.commuin.struct.createMemState
import com.cyrax.commuin.struct.db
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.formIcons
import com.cyrax.commuin.struct.greyPeriList
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.cyrax.commuin.ui.theme.RainForest
import com.cyrax.commuin.ui.theme.Spinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.WatchEvent


//(Password,OrgName,Email, OrgDepartments->(DeptNames SUB Designation) , Dept Name -> { Designation :{ MemberID:{ID,Name,Address-nullable,Contact , Email}} } , OrgPosts , OrgMessages)




//This composable defines the outer boundary that holds the complete option for LOGIN(org,member) and SIGNUP(member,org)
@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Option(cb1:()->Unit,cb2:()->Unit,cb3:()->Unit,cb4:()->Unit , appModel: CommuinModel , onSuccess: () -> Unit){
    var expanded by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val DB = db.getDatabase(context)
    val list = remember{ mutableStateListOf(SavedMember()) }
    LaunchedEffect(key1 =Unit ){
        scope.launch(Dispatchers.Default){
            list.clear()
            list.addAll(DB.savedMem().getMemProfiles())
        }
    }
    if(appModel.LoginState is Login.Success){
        LaunchedEffect(key1 = Unit ){
            onSuccess()
        }
    }
    else if(appModel.LoginState is Login.Loading) InfoDialog(info = "Loading..")
    else if(appModel.LoginState is Login.Error) {
        TOAST(context, (appModel.LoginState as Login.Error).error)
        appModel.updateLoginState(Login.Default)
    }

    Column(modifier= Modifier
        .background(Brush.verticalGradient(listOf(MidnightBlue, BlueBerry, MidnightBlue)))
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)) {
        GreyCard( modifier = Modifier
            .width(300.dp)
            .heightIn(100.dp, 200.dp)
            .shadow(Color.Black.copy(alpha = .8f), 1.dp, 1.dp, 18.dp, 0.dp, 0.dp), paddingH = 20.dp , paddingV = 10.dp
             , backColor = RainForest.outline , shape = RoundedCornerShape(8.dp) ,
        ){
            LoginOrSignup(option = "LOGIN",
                onClick = {if(expanded=="LOGIN")expanded=""; else expanded="LOGIN"},
                expanded, style = MaterialTheme.typography.headlineLarge, fontFamily = Jost,
                orgOnClick= cb1,
                memberOnClick= cb2)
            LoginOrSignup(option = "SIGNUP",
                onClick = {if(expanded=="SIGNUP")expanded=""; else expanded="SIGNUP"},
                expanded,style = MaterialTheme.typography.headlineLarge, fontFamily = Jost,
                orgOnClick=cb3,
                memberOnClick = cb4)
        }
        if(list.size >= 1 && list[0].orgID.isNotBlank())
            LazyColumn(modifier = Modifier
                .width(300.dp)
                .align(Alignment.CenterHorizontally)){
                itemsIndexed(list){index,it ->
                    GreyCard(
                        Modifier.clickable {
                            appModel.loginFormUpdate(it.orgID, 1)
                            appModel.loginFormUpdate(it.memberID, 2)
                            appModel.loginFormUpdate(it.pswd, 3)
                            appModel.updateLoginState(Login.Loading)
                            appModel.loginFormUpdate("Logging In", 98)
                        },
                        paddingH = 0.dp,
                        paddingV = 0.dp,
                        backColor = Periwinkle,
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Row(modifier = Modifier
                            .fillMaxWidth().height(60.dp)
                            .padding(10.dp, 0.dp)
                            .background(Color.Transparent), verticalAlignment = Alignment.CenterVertically){
                            MiniImage(onProfileClick = { /*TODO*/ }, imgId = R.drawable.sampleprof, size = 50 , asyncc = extrasForDP("${it.memberID} dp",
                                "",
                                "${it.orgID}/${it.memberID}/dp"  ) )
                           // BrightImage(onClick = {}, uri = it.uri , asyncc = true ,size = 43 ,boxshadowColor = Color.Black.copy(alpha = 0.3f))
                            Spacer(Modifier.width(30.dp))
                            TextColumn(str1 = it.name , str2 = "Member" , FW1=FontWeight.Medium, FW2 = FontWeight.Light , FSize1=19.sp,FSize2= 13.sp)
                            Spacer(Modifier.weight(1f))
                            var visible by rememberSaveable{mutableStateOf(false)}
                            IconButton(onClick = {
                                visible = true
                            }) {
                                Image(painter = painterResource(id = R.drawable.trash),null,modifier = Modifier.size(30.dp))
                            }
                            if(visible)
                            ConfirmDialog({scope.launch(Dispatchers.Default){
                                DB.savedMem().delProfile(it.memberID)
                                list.removeAt(index)
                            }},{visible = false},"Confirm","Delete Quick Login Profile: ${it.name} ?")


                        }
                    }
                }
            }


    }



}

//Displays the LOGIN AND SIGNUP TEXT and also makes it clickable
@Composable
fun LoginOrSignup(option:String, onClick:()->Unit, expanded:String, style: TextStyle, fontFamily : FontFamily,
                  orgOnClick:()->Unit, memberOnClick:()->Unit
){
    Column(modifier= Modifier.animateContentSize()) {
        Text(option, style = style , fontFamily = fontFamily , fontWeight = FontWeight.Bold ,
            modifier= Modifier.clickable { onClick() })
        if(option==expanded){
            SubOption(orgOnClick = orgOnClick, memberOnClick = memberOnClick)

        }
    }
}


//This composable is for the option that opens when LOGIN or SIGNUP is clicked
@Composable
fun SubOption(orgOnClick:()->Unit,memberOnClick:()->Unit , appModel: CommuinModel = viewModel()){
    Column(modifier= Modifier.drawBehind {
        val canvasWidth = size.width //This is provided by the system
        val canvasHeight = size.height
        drawLine( start = Offset(x = 20f, y = 0f), end = Offset(x = 20f, y = canvasHeight), color = Color.Black, strokeWidth = 9f )
    }){
        Column(modifier= Modifier.padding(20.dp , 3.dp)){
            Text("ORGANISATION",modifier= Modifier.clickable { orgOnClick() },fontSize=22.sp)
            Text("MEMBER",modifier= Modifier.clickable { memberOnClick() },fontSize=22.sp)
        }

    }
}

//Opens the form for Organisation signup
@Composable
fun OrgSignUp(appModel: CommuinModel , org : classOrgSignUp , callback:(String,Int)->Unit , onVerifyClick:(String)->Unit , onCancelClick:()->Unit, context: Context = LocalContext.current ){
    val whichPage = rememberSaveable { mutableStateOf(true)  }
    val scope = rememberCoroutineScope()
    BackHandler( enabled = true ) { onCancelClick() }
    Column(horizontalAlignment = Alignment.CenterHorizontally,modifier= Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(MidnightBlue, BlueBerry, MidnightBlue)))) {
        if(whichPage.value){
            Spacer(Modifier.height(5.dp))
            BackBtnForm {onCancelClick()}
            FormHeading("Org SIGNUP",R.drawable.org_signup)
            FormTextField(modifier = Modifier,value = org.orgID , callback = { callback(it,1) }, placeholer ="Org ID")
            FormTextField(modifier = Modifier,value = org.orgName , callback = { callback(it,2) }, placeholer ="Org Name"  )
            FormTextField(modifier = Modifier,value = org.email , callback = { callback(it,3) }, placeholer ="Org Email"  )
            FormTextField(modifier = Modifier,value = org.password , callback = { callback(it,4) }, placeholer ="Password" ,keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),visualTransformation = PasswordVisualTransformation() )
            Spacer(Modifier.height(15.dp))
            Button(onClick = {
                if(org.orgID.isBlank()) Toast.makeText(context , "OrgID cannot be blank !" , Toast.LENGTH_LONG).show()
                else{
                    scope.launch {
                        appModel.genratedOTP = genrateOTP()
                        val msg = "You have received this email because OTP request was initiated for this email address to create an account for orgID : ${org.orgID}.      Your OTP is :  ${appModel.genratedOTP} "
                        Ssend().sendMail(org.email , "Verify your Email" ,msg )
                        Toast.makeText(context , "OTP Sent !" , Toast.LENGTH_LONG).show()
                        whichPage.value = false
                    }
                }
            },Modifier.fillMaxWidth(.7f).shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),
                contentPadding = PaddingValues(40.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = RainForest.onSurface, contentColor = Color.Black),border = BorderStroke(1.dp,Color.Black)
            ) { Text("Get OTP",fontSize = 18.sp, fontFamily = Jost ) }
        }
        else{
            Spacer(Modifier.height(5.dp))
            BackBtnForm { whichPage.value=true }
            //OTP Page
            Text( "Enter the OTP sent to : ${org.email}" , fontFamily = Jost , fontSize = 25.sp)
            OTP(onVerifyClick = onVerifyClick )
        }

        if(appModel.LoginState is Login.Success){
            TOAST(context,"Account Created !\nNow Login.")
            appModel.updateLoginState(Login.Default)
        }
        else if(appModel.LoginState is Login.Error){
            TOAST(context,"Incorrect OTP")
            appModel.updateLoginState(Login.Default)
        }

    }
}


@Composable
fun MemberSignUp(appmodel:CommuinModel , onCreateClick:()->Unit ,onCancelClick:()->Unit,context:Context = LocalContext.current){
    val whichPage = rememberSaveable { mutableStateOf(true)  }
    val mem = appmodel.mem.collectAsState().value
    val scope = rememberCoroutineScope()
    BackHandler( enabled = true ) { onCancelClick() }
    Column(horizontalAlignment = Alignment.CenterHorizontally,modifier= Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(MidnightBlue, BlueBerry, MidnightBlue)))) {
        if(whichPage.value){ //Opens the form for Member signup
            Spacer(Modifier.height(5.dp))
            BackBtnForm { onCancelClick() }
            FormHeading("Member  SIGNUP",R.drawable.mem_signup)
            FormTextField(modifier = Modifier,value = mem.orgID , { appmodel.resetTemp(createMemState.Default); appmodel.memSignUpUpdate(it,1)}, "Org ID" ,
                keyboardOptions= KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions {
                    appmodel.resetTemp(createMemState.Loading)
                    appmodel.checkOrg(mem.orgID)
                },
                trailingIcon = {
                    if(appmodel.dAndDObj is createMemState.Loading) Spinner()
                    else if(appmodel.dAndDObj is createMemState.Success) Image(painter = painterResource( R.drawable.check ) , null , modifier = Modifier.size(28.dp))
                    else if(appmodel.dAndDObj is createMemState.Error) {
                        Image(painter = painterResource(R.drawable.cross) , null , modifier = Modifier.size(28.dp))
                        Toast.makeText(context, (appmodel.dAndDObj as createMemState.Error).error.message , Toast.LENGTH_LONG).show()

                    }
                })
            GreyCard(
                Modifier
                    .height(53.dp)
                    .shadow(Color.Black.copy(alpha = .8f), 1.dp, 1.dp, 18.dp, 120.dp, 120.dp),shape = RoundedCornerShape(35.dp)) {DropDown(valueOnDisplay= mem.dept, appmodel = appmodel ,  query = "Department" , callback = { appmodel.memSignUpUpdate(it,2) })}
            GreyCard(
                Modifier
                    .height(53.dp)
                    .shadow(Color.Black.copy(alpha = .8f), 1.dp, 1.dp, 18.dp, 120.dp, 120.dp),shape = RoundedCornerShape(35.dp)) {DropDown( valueOnDisplay= mem.designation,appmodel = appmodel , query = "Designation" , callback = { appmodel.memSignUpUpdate(it,3) })}
            FormTextField(modifier = Modifier,value = mem.memberID , {appmodel.memSignUpUpdate(it,4)}, "MemberID")
            Spacer(Modifier.height(15.dp))
            Button(onClick = {
                scope.launch {
                    appmodel.checkMember(mem.memberID)
                    if(appmodel.memberExist != "Member Doesn't Exists" && appmodel.memberExist != "Already"){ //If org has record of provided member
                        appmodel.genratedOTP = genrateOTP()
                        // appmodel.memberExits contain email ID of the ID entered
                        val msg = "You have received this email because OTP request was initiated for this email address to create an account for memberID : ${appmodel.memberExist.trim()}.      Your OTP is :  ${appmodel.genratedOTP} "
                        Ssend().sendMail(appmodel.memberExist.trim() , "Verify your Email" ,msg )
                        Toast.makeText(context , "OTP Sent !" , Toast.LENGTH_LONG).show()
                        whichPage.value= false
                    }
                    else if(appmodel.memberExist == "Already") TOAST(context,"MemberID already has an Account! Try Logging in instead.","Long")
                    else TOAST(context,"Member Doesn't Exists")
                }


            },Modifier.fillMaxWidth(.7f).shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),enabled=appmodel.dAndDObj is createMemState.Success,
                contentPadding = PaddingValues(40.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = RainForest.onSurface, contentColor = Color.Black),border = BorderStroke(1.dp,Color.Black)
            ) { Text("Verify Member",fontSize = 18.sp, fontFamily = Jost ) }

        }
        else{ //Opens the setPassword page for members
                var cnfPswd by rememberSaveable { mutableStateOf("") }
            var enteredOTP by rememberSaveable { mutableStateOf("") }
            Spacer(Modifier.height(5.dp))
            BackBtnForm {
                appmodel.memSignUpUpdate("",5); cnfPswd = ""  //Resetting the Password and ConfirmPassword field
                whichPage.value= true
            }
            Spacer(Modifier.height(25.dp))
           Text( "OTP Sent to your email registered with organisation" , fontFamily = Jost , fontSize = 25.sp, color = Color.White)
            FormTextField(modifier = Modifier,value = enteredOTP , { enteredOTP = it}, "OTP" )
            Text( "Set Password" , fontFamily = Jost , fontSize = 25.sp, color = Color.White)
            FormTextField(modifier = Modifier,value = mem.password , {appmodel.memSignUpUpdate(it,5)}, "Password" , keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)  , visualTransformation = PasswordVisualTransformation())
            FormTextField(modifier = Modifier ,value = cnfPswd , { cnfPswd=it; appmodel.memSignUpUpdate(it,90) }, "Confirm Password" , keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = { if(appmodel.passwordState is RequestStatus.Error )Icon(imageVector = Icons.Filled.Error, contentDescription = null,tint = Color.Red) } , visualTransformation = PasswordVisualTransformation())
            Spacer(Modifier.height(10.dp))
            Button(onClick = {
                //Checking whether the passwords entered are same
                if(appmodel.genratedOTP != enteredOTP) TOAST(context , "Incorrect OTP")
                else if( mem.password != cnfPswd)
                    TOAST(context , "Password doesn't match")
                else if(mem.password.isBlank()){
                    TOAST(context , "Field cannot be empty")
                }
                else{
                    scope.launch {
                        onCreateClick()
                        TOAST(context ,"Account Created!\nNow Login")
                    }

                }

            },Modifier.fillMaxWidth(.7f).shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),
                contentPadding = PaddingValues(40.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = RainForest.onSurface, contentColor = Color.Black),border = BorderStroke(1.dp,Color.Black)
            ) { Text("Create Account",fontSize = 18.sp, fontFamily = Jost ) }

        }
    }
}

@Composable
fun OrgLogin(navController : NavHostController, appModel: CommuinModel, context: Context = LocalContext.current, onBackClick:()->Unit , onSuccess:()->Unit){  //Opens Organisation Login form
    BackHandler( enabled = true ) {  onBackClick()  }
    val formObj = appModel.login.collectAsState().value

    if(appModel.LoginState is Login.Success){
        LaunchedEffect(key1 = Unit ){
            onSuccess()
        }
    }

    else if(appModel.LoginState is Login.Loading) InfoDialog(info = "Loading..")
    else if(appModel.LoginState is Login.Error) {
        TOAST(context, (appModel.LoginState as Login.Error).error)
        appModel.updateLoginState(Login.Default)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier= Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(MidnightBlue, BlueBerry, MidnightBlue)))) {
        Spacer(Modifier.height(5.dp))
        BackBtnForm{ appModel.loginFormUpdate("Reset Form",90); onBackClick() }
        FormHeading("Org LOGIN",R.drawable.org)
        FormTextField(modifier = Modifier ,value = formObj.orgID , callback = { appModel.loginFormUpdate(it,1)}, placeholer = "Org ID", keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next))
        FormTextField(modifier = Modifier ,value = formObj.Password , callback = { appModel.loginFormUpdate(it,3)}, "Password" , visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            appModel.updateLoginState(Login.Loading)
            appModel.loginFormUpdate("Logging In",99)
        },Modifier.fillMaxWidth(.7f).shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),
            contentPadding = PaddingValues(40.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = RainForest.onSurface, contentColor = Color.Black),border = BorderStroke(1.dp,Color.Black)
        ) { Text("LOGIN",fontSize = 18.sp, fontFamily = Jost ) }

        Spacer(modifier = Modifier.height(10.dp))
    }



}

@Composable
fun MemberLogin(navController : NavHostController, appModel: CommuinModel, context: Context = LocalContext.current, onBackClick:()->Unit, onSuccess:()->Unit){ //Opens the form for Member login
    BackHandler( enabled = true ) {  onBackClick() }
    val formObj = appModel.login.collectAsState().value
    val DB = db.getDatabase(context)
    if(appModel.LoginState is Login.Success){
        LaunchedEffect(key1 = Unit ){
            withContext(Dispatchers.Default){
                val temp = (appModel.uiState as AppData.Member).appState
                DB.savedMem().insertProfile(SavedMember(orgID = temp.orgID , memberID = temp.memberID, pswd = temp.password , name = temp.profileDetail.name ))
            }
            navController.popBackStack()
            onSuccess()

        }
    }
    else if(appModel.LoginState is Login.Loading) InfoDialog(info = "Loading..")
    else if(appModel.LoginState is Login.Error) {
        TOAST(context, (appModel.LoginState as Login.Error).error)
        appModel.updateLoginState(Login.Default)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally,modifier= Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(MidnightBlue, BlueBerry, MidnightBlue)))){
        Spacer(Modifier.height(5.dp))
        BackBtnForm { appModel.loginFormUpdate("Reset Form",90); onBackClick() }
        FormHeading("Member Login",R.drawable.mem)
        FormTextField(modifier = Modifier ,value = formObj.orgID , { appModel.loginFormUpdate(it,1)}, "Org ID", keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next))
        FormTextField(modifier = Modifier ,value = formObj.memID , { appModel.loginFormUpdate(it,2)}, "Member ID", keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next))
        FormTextField(modifier = Modifier ,value = formObj.Password , { appModel.loginFormUpdate(it,3)}, "Password" ,
            keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next),visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            appModel.updateLoginState(Login.Loading)
            appModel.loginFormUpdate("Logging In",98 )
        },Modifier.fillMaxWidth(.7f).shadow(Color.Black.copy(alpha = .2f),0.dp,0.dp,5.dp,100.dp,100.dp),
            contentPadding = PaddingValues(40.dp,0.dp),colors = ButtonDefaults.buttonColors(containerColor = RainForest.onSurface, contentColor = Color.Black),border = BorderStroke(1.dp,Color.Black)
        ) { Text("LOGIN",fontSize = 18.sp, fontFamily = Jost ) }
        Spacer(modifier = Modifier.height(5.dp))
    }
}


fun genrateOTP():String{
    var a :String=""
    for(i in 0..5){
        a +=(0..9).random().toString()
    }
    return a
}

@Composable
fun FormHeading(text:String,imgId:Int){
    Spacer(Modifier.height(0.dp))
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp), horizontalAlignment = Alignment.CenterHorizontally , verticalArrangement = Arrangement.SpaceBetween){
        Image(painterResource(id = imgId), null , Modifier.size(130.dp))
        Row(){
            Spacer(Modifier.weight(1f))
            BrightBox( modifier = Modifier ,
                height = 45.dp, boxcolor =  MidnightBlue, boxshadowColor = Color.White.copy(alpha = .0f) ,
                text = text, textColor = Color.White,
                textSize = 26.sp, fontWeight = FontWeight.Medium , shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.weight(1f))
        }

    }
    Spacer(Modifier.height(40.dp))


}
@Composable
fun FormTextField(modifier:Modifier = Modifier, value:String, callback: (String) -> Unit, placeholer:String ,
                  keyboardOptions: KeyboardOptions = KeyboardOptions() ,
                  keyboardActions: KeyboardActions = KeyboardActions(),
                  visualTransformation: VisualTransformation = VisualTransformation.None,
                  trailingIcon: (@Composable() () -> Unit)? = null,

){
    GreyCard(
        Modifier
            .height(53.dp)
            .shadow(Color.Black.copy(alpha = .8f), 1.dp, 1.dp, 18.dp, 120.dp, 120.dp) , shape = RoundedCornerShape(35.dp)) {
        CustomTextField(modifier = modifier
            .width(310.dp)
            .height(53.dp)
            .background(Brush.horizontalGradient(greyPeriList), shape = CircleShape)
            .padding(horizontal = 15.dp, vertical = 0.dp), paddingTrailingIconStart = 30.dp,value = value,
            onValueChange = { callback(it) } ,
            placeholder = placeholer,
            leadingIcon = {
                Row(){
                    Log.d("qwerty",placeholer)
                    Image(painter = painterResource(formIcons[placeholer]!!) , null , modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(20.dp))
                }
            },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions ,
            visualTransformation = visualTransformation
        )
    }
    Spacer(Modifier.height(2.dp))
}


@Composable
fun BackBtnForm(onClick: () -> Unit){
    Row(modifier= Modifier.fillMaxWidth()){
        Spacer(Modifier.width(10.dp))
        IconButton(modifier= Modifier.size(50.dp),onClick = onClick) {
            Image(painter = painterResource(id = R.drawable.left_chevron), contentDescription = null,Modifier.size(50.dp))
        }
        Spacer(Modifier.weight(1f))
    }
}