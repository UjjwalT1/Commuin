package com.cyrax.commuin.sections

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cyrax.commuin.R
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.RequestStatus
import com.cyrax.commuin.struct.classMemSignUp
import com.cyrax.commuin.struct.classOrgSignUp
import com.cyrax.commuin.struct.extrasForDP
import com.cyrax.commuin.struct.greyPeriList
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgProfile(appModel: CommuinModel, obj: classOrgSignUp = classOrgSignUp()){
    var strRef : StorageReference = FirebaseStorage.getInstance().reference
    var imageUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    if(imageUri != null){
        appModel.requestState = RequestStatus.Loading
        var temp = imageUri!!
        imageUri = null
        Log.d(TAG, "-----------------------${imageUri}---")
        LaunchedEffect(key1 = temp){
            appModel.set( temp , "${obj.orgID}/${obj.orgID}/dp" )
        }

    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(){
                when(appModel.requestState){
                    is RequestStatus.Success -> {
                        AsyncImage(
                            model= (appModel.requestState as RequestStatus.Success).link,
                            //"https://firebasestorage.googleapis.com/v0/b/commuin.appspot.com/o/dp%2FAmishdp?alt=media&token=65090b41-e39d-476e-b911-1ffab54e8a1a",
                            //  painter = painterResource(id = imgId ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    is RequestStatus.Default -> {
                        Image(
                            painter = painterResource(id = R.drawable.sampleprof ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.loading ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                FloatingActionButton(onClick = {
                    launcher.launch("image/*")
                                               } ,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .align(Alignment.BottomEnd)) {
                    Icon(imageVector = Icons.Filled.Upgrade, contentDescription = null)
                }
            }
            
            Text(obj.orgName)
            val expanded = rememberSaveable { mutableStateOf(false) }
            Card(elevation = CardDefaults.cardElevation(10.dp) , modifier = Modifier.animateContentSize()) {
                Row(){
                    Text("Departments")
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {  expanded.value = !(expanded.value) }) {
                        if(expanded.value)  Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null)
                        else Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null)
                    }

                }
                if(expanded.value){
                    Column{
                        mapToArray(obj.orgDepartments).forEach{
                            Text(it)
                        }
                    }
                }

            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemProfile(memberModel: MemberModel, obj: classMemSignUp , onBack:()->Unit ){
    BackHandler(enabled = true) { onBack() }
    val scope = rememberCoroutineScope()
    var bitmap  by rememberSaveable{ mutableStateOf<String?>(null) }
    val meForFolder = memberModel.myData.collectAsState().value
    val context = LocalContext.current
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    if(bitmap == null){
        LaunchedEffect(key1 = Unit){
            val a = scope.launch(Dispatchers.IO) {
             bitmap = memberModel.getImage(context ,"${meForFolder.memberID} dp",
                    pathTo = "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}",
                    pathfrom = "${meForFolder.orgID}/${meForFolder.memberID}/dp" ,
                    filetype = "img" , extension = "png"
                )
            }


        }
    }

    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        //deleteFileInDirectory("Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}/15039 dp.jpg")
    }
    if(imageUri != null){
        bitmap = null
        val temp = imageUri!!
        imageUri = null
        Log.d(TAG, "-----------------------${imageUri}- line 209 profile.kt--")
        LaunchedEffect(key1 = temp){
            memberModel.addImage(temp,context , pathForRTDB ="${obj.orgID}/${obj.memberID}/dp" ,
                pathTo = "Commuin/${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}" , extension = "png" , filetype = "img")
            bitmap = temp.toString()
        }
    }
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .fillMaxSize()
        .background(MidnightBlue)){
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom,modifier= Modifier
                .fillMaxWidth()
                .padding(10.dp)){
                BrightImage(onClick = { /*TODO when profile pic is clicked*/ }, uri = bitmap, asyncc = true,size = 200)
                Spacer(modifier = Modifier.width(0.dp))
                BrightImage(onClick = { launcher.launch("image/*") }, uri = R.drawable.cloud_upload.toString(), asyncc = false , size = 50, boxshadowColor = Color.Black.copy(alpha = 0.5f))
                BrightImage(onClick = {
                    scope.launch {
                        memberModel.deleteImageFormCloud(context);
                        bitmap = null;
                    }

                                      },
                    uri = R.drawable.cro_ss.toString(), asyncc = false , size =50, boxshadowColor = Color.Black.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.width(15.dp))
            }

            ProfRowHolder(boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f),shape = RoundedCornerShape(17.dp,17.dp,17.dp,17.dp),title = "Organisation",data = obj)
            ProfRowHolder(boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f),shape = RoundedCornerShape(17.dp,17.dp,17.dp,17.dp),title = "Your Details",data = obj)
            ProfRowHolder(boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f),shape = RoundedCornerShape(17.dp,17.dp,17.dp,17.dp),title = "Settings",data = obj)
        }
    }
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightBox(modifier: Modifier,width: Dp=0.dp, height:Dp , boxcolor:Color,boxshadowColor:Color,
              text:String,textColor:Color,textSize:TextUnit,fontWeight: FontWeight=FontWeight.Normal,
              shape: RoundedCornerShape = RoundedCornerShape(0.dp,10.dp,10.dp,0.dp)
){
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = shape ,
        modifier = modifier
            .widthIn(50.dp, 250.dp)
            .height(height)
            .shadow(boxshadowColor, 1.dp, 1.dp, 18.dp, 0.dp, 10.dp)
            .clip(shape)){
        Spacer( modifier = Modifier.weight(1f) )
        Text(modifier = Modifier.padding(15.dp,0.dp),text = text , color = textColor , fontSize = textSize , fontWeight = fontWeight , fontFamily = Jost)
        Spacer( modifier = Modifier.weight(1f) )
    }
}

@Composable
fun BrightImage(onClick:()->Unit, uri:String? , size:Int=100,boxshadowColor:Color= Color.White.copy(alpha = 0.5f),asyncc:Boolean){
    IconButton(onClick = { onClick() },
        colors = IconButtonDefaults.iconButtonColors(IndigoInk),
        modifier = Modifier
            .size(size.dp)
            .shadow(boxshadowColor, 2.dp, 2.dp, 6.dp, size)
            .clip(RoundedCornerShape(49))
            .shadow(boxshadowColor, 2.dp, 2.dp, 6.dp, size)



    ) {
        if(asyncc == true)
            AsyncImage(
                 model=if(uri==null || uri.isBlank())"https://firebasestorage.googleapis.com/v0/b/commu-in-e20d8.appspot.com/o/placeholder.jpg?alt=media&token=3aff3d77-e734-41fb-9916-1ad9acbaf120" else uri,
              //  painter = painterResource(id = imgId ),
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(50)),
                contentScale = ContentScale.Crop
            )
        else{
            Image(
               // model=if(uri==null)"https://firebasestorage.googleapis.com/v0/b/commuin.appspot.com/o/dp%2FAmishdp?alt=media&token=65090b41-e39d-476e-b911-1ffab54e8a1a" else uri,
                painter = painterResource(id = uri!!.toInt() ),
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(50)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ProfRow(modifier:Modifier = Modifier,key:String,value:String,size1:TextUnit=20.sp,size2:TextUnit=23.sp,weight1:FontWeight = FontWeight.Bold,weight2: FontWeight=FontWeight.Normal){
    Row(modifier= modifier
        .fillMaxWidth()
        .padding(0.dp, 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
        BrightBox( modifier=Modifier,height = 36.dp, boxcolor = Periwinkle , boxshadowColor =  Color.White.copy(alpha = .3f) ,
            text = key, textColor = MidnightBlue , textSize = size1 , fontWeight = weight1, shape = RoundedCornerShape(0.dp,10.dp,10.dp,0.dp)
        )
        Spacer(modifier=Modifier.width(30.dp))
        BrightBox( modifier=Modifier.weight(1f), height = 39.dp, boxcolor = MidnightBlue , boxshadowColor = Color.Black.copy(alpha = .7f),
            text =  value, textColor = Periwinkle, textSize = size2  ,fontWeight = weight2, shape =   RoundedCornerShape(10.dp,0.dp,0.dp,10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfRowHolder( boxcolor:Color,boxshadowColor:Color,shape: RoundedCornerShape = RoundedCornerShape(10.dp,10.dp,10.dp,10.dp),
                   title :String,data: classMemSignUp = classMemSignUp()
){
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = shape ,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .shadow(boxshadowColor, 0.dp, 0.dp, 30.dp, 0.dp, 10.dp)
            .clip(shape)){
        Row(modifier=Modifier.fillMaxWidth()){
            Spacer(modifier=Modifier.weight(1f))
            BrightBox( modifier=Modifier,height = 26.dp, boxcolor = Periwinkle , boxshadowColor =  Color.White.copy(alpha = .3f) ,
                text = title, textColor = MidnightBlue , textSize = 20.sp , shape = RoundedCornerShape(10.dp,10.dp,10.dp,10.dp)
            )
            Spacer(modifier=Modifier.weight(1f))
        }
        Spacer(modifier=Modifier.height(25.dp))
        when(title){
            "Your Details" -> {
                ProfRow(Modifier,"ID",data.memberID)
                ProfRow(Modifier,"Name",data.profileDetail.name)
                ProfRow(Modifier,"Department",data.profileDetail.dept)
                ProfRow(Modifier,"Designation",data.profileDetail.designation)
                ProfRow(Modifier,"Email",data.profileDetail.email,size2=17.sp)
                ProfRow(Modifier,"Contact",data.profileDetail.contact)
            }
            "Organisation" -> {
                ProfRow(Modifier,"ID",data.orgID)
                ProfRow(Modifier,"Name",data.profileDetail.organName)
            }
            "Settings" -> {
                RowWithButtonLikeDeleteAccount("Password","CHANGE" , R.drawable.changepass , {})
                RowWithButtonLikeDeleteAccount("Manage A/c","DELETE" , R.drawable.trash , {})
            }
        }

        Spacer(modifier=Modifier.height(20.dp))
    }

}

@Composable
fun RowWithButtonLikeDeleteAccount(text1:String,text2:String , id:Int , onClick: () -> Unit){
    Row(modifier= Modifier
        .fillMaxWidth()
        .padding(0.dp, 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
        BrightBox( modifier=Modifier.weight(1f), height = 36.dp, boxcolor = Periwinkle , boxshadowColor = Color.White.copy(alpha = .7f),
            text =  text1, textColor = MidnightBlue, textSize = 20.sp  ,fontWeight = FontWeight.Normal, shape =   RoundedCornerShape(0.dp,10.dp,10.dp,0.dp)
        )
        Spacer(modifier=Modifier.width(20.dp))
        BrightWithButton(
            spaceNeed= true,
            text = text2,
            height = 39.dp,
            boxcolor = MidnightBlue,
            boxshadowColor = Color.Black.copy(alpha = .7f),
            textColor = Periwinkle ,
            onClick = onClick,
            id = id,
            btnSize = 25.dp,
            shape = RoundedCornerShape(10.dp,0.dp,0.dp,10.dp)
        )
    }
}

@Preview (showSystemUi = true)
@Composable
fun a(){
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightWithButton(spaceNeed:Boolean=false, height:Dp, boxcolor:Color, boxshadowColor:Color, shape: RoundedCornerShape = RoundedCornerShape(10.dp,10.dp,10.dp,10.dp),
                     text:String="", textColor:Color, onClick:()->Unit, id:Int, btnSize:Dp , fontWeight: FontWeight= FontWeight.Bold
                     ){
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = shape ,
        modifier = Modifier
            .widthIn(65.dp, if (!spaceNeed) 300.dp else 230.dp)
            .height(height)
            .shadow(boxshadowColor, 1.dp, 1.dp, 13.dp, 70.dp, 70.dp)
            .clip(shape)){
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically){
            if(text.isNotBlank()){
                Text(modifier = Modifier.padding(30.dp,0.dp),text = text , color = textColor ,fontFamily = Jost , fontWeight = fontWeight , fontSize = 25.sp)
               if(!spaceNeed)Spacer(modifier = Modifier.weight(.1f))
            }
            IconButton(onClick = onClick,modifier = Modifier.padding(4.dp,0.dp,0.dp,0.dp)
            ) {
                Image(painter = painterResource(id = id), contentDescription = null , modifier = Modifier.size(btnSize))
            }
        }
        Spacer(modifier = Modifier.weight(1f))

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightWithButtonRiffOff(spaceNeed:Boolean=false, height:Dp, boxcolor:Color, boxshadowColor:Color, shape: RoundedCornerShape = RoundedCornerShape(10.dp,10.dp,10.dp,10.dp),
                     text:String="", textColor:Color, onClick:()->Unit, id:Int, btnSize:Dp ,asyncc:Any? = false
){
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = shape ,
        modifier = Modifier
            .widthIn(60.dp, if (!spaceNeed) 300.dp else 230.dp)
            .height(height)
            .shadow(boxshadowColor, 1.dp, 1.dp, 13.dp, 70.dp, 70.dp)
            .clip(shape)){
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically){
            if(text.isNotBlank()){
                Text(modifier = Modifier.padding(30.dp,0.dp),text = text , color = textColor ,fontFamily = Jost , fontWeight = FontWeight.Bold , fontSize = 25.sp)
                if(!spaceNeed)Spacer(modifier = Modifier.weight(.1f))
            }
            IconButton(onClick = onClick,modifier = Modifier
            ) {
                MiniImage(onProfileClick = onClick, imgId = id, size = btnSize.value.toInt(),asyncc)
            }
        }
        Spacer(modifier = Modifier.weight(1f))

    }
}



@Composable
fun NewTopBar(icon1:Int = R.drawable.left_chevron,icon2:Int=R.drawable.more,onClickFirst:()->Unit,onClickLast:()->Unit,title:String ="Sample",
              isHome:Boolean=false , extra:extrasForDP = extrasForDP() , fontWeight: FontWeight = FontWeight.Normal , list : List<Pair<String,()->Unit>> = listOf()
){
    Row(modifier= Modifier
        .fillMaxWidth()
        .background(MidnightBlue)
        .padding(0.dp, 5.dp, 0.dp, 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
        val context = LocalContext.current
        BrightWithButton(height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f), textColor = Periwinkle,
            shape = RoundedCornerShape(0.dp,40.dp,40.dp,0.dp), onClick = onClickFirst,id = icon1, btnSize = 30.dp)
        Spacer(modifier=Modifier.width(0.dp))
        if(!isHome){
            if(icon2 != R.drawable.more) {
                BrightWithButton(text = title,height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f), textColor = Periwinkle,
                    shape = RoundedCornerShape(40.dp,0.dp,0.dp,40.dp), onClick = { onClickLast();  },id = icon2, btnSize = 26.dp,
                    fontWeight = fontWeight
                )
            }
            else{
                var expanded by rememberSaveable{ mutableStateOf(false) }
                Log.d("qwerty","Toggled at line 459 $expanded")
                Box(){
                    BrightWithButton(text = title,height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .2f), textColor = Periwinkle,
                        shape = RoundedCornerShape(40.dp,0.dp,0.dp,40.dp), onClick = { expanded = !expanded },id = icon2, btnSize = 30.dp, fontWeight = fontWeight
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false } , Modifier.background(
                        Brush.horizontalGradient(greyPeriList)) , offset = DpOffset(1000.dp,0.dp)) {
                        Column{
                            list.forEach{
                                when(it.first){
                                    "DELETE" ->  ItemOption(str = it.first , it.second , true , "Delete Event","Are you Sure ?")

                                    "UPDATE" ->  ItemOption(str = it.first , it.second , true , "Update Event","Are you Sure ?")

                                    else ->  ItemOption(str = it.first , it.second)
                                }
                            }
                        }

                    }
                }
            }
        }
        else{
            BrightWithButtonRiffOff(text = title,height = 45.dp, boxcolor = MidnightBlue, boxshadowColor = Color.White.copy(alpha = .3f), textColor = Periwinkle,
                shape = RoundedCornerShape(40.dp,0.dp,0.dp,40.dp), onClick = onClickLast,id = icon2, btnSize = 38.dp ,asyncc = extra)
        }

    }

}
