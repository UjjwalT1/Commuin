package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cyrax.commuin.R
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cyrax.commuin.Utils.getFileNameAndLength
import com.cyrax.commuin.Utils.splitString
import com.cyrax.commuin.Utils.uriToImageBitmap
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.documentMime
import com.cyrax.commuin.struct.eventIcons
import com.cyrax.commuin.struct.littleIcons
import com.cyrax.commuin.ui.theme.IndigoInk
import com.cyrax.commuin.ui.theme.RainForest
import com.cyrax.commuin.ui.theme.Spinner
import com.google.firebase.database.FirebaseDatabase


@SuppressLint("SuspiciousIndentation")
@Composable
fun EventHolder(memberModel: MemberModel = viewModel(),navController2 : NavHostController = rememberNavController()){
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit){
        memberModel.getEvents()
    }
    val ar = memberModel.eventsList
    Column(Modifier.fillMaxWidth().background(MidnightBlue).padding(0.dp,0.dp,4.dp,0.dp)){
        Spacer(Modifier.height(2.dp))
        Row(Modifier.fillMaxWidth()){
            Spacer(Modifier.weight(1f))
            if(memberModel.eRefresh.value) Spinner(20.dp)
            else Image(painterResource(R.drawable.reload),null,Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        if(ar.isEmpty()){
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MidnightBlue)
                    .wrapContentSize(Alignment.Center)) {
                Image(painter = painterResource(id = R.drawable.blankdir), contentDescription = null , Modifier.size(200.dp) )
            }
        }
        else
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)){
                items(ar){
                    GreyCard(Modifier.clickable {
                        memberModel.setCurrEvent(it);
                        memberModel.eventNotification =  it
                        Log.d("qwerty",memberModel.eventNotification.toString())
                        Screen.EEVENTS.address = it.title
                        memberModel.setCurr(Screen.EEVENTS);

                        navController2.navigate("EventView")

                    }, paddingH = 20.dp, paddingV = 5.dp , backColor = RainForest.onSurface) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically){
                            MiniText("${it.title}" , MaterialTheme.typography.titleLarge , FontWeight.Medium,
                                "${it.whoUploaded.dept} | ${it.whoUploaded.designation}" , MaterialTheme.typography.titleSmall,Modifier,
                                "Uploaded By : ${it.whoUploaded.name}" )
                        }
                    }
                }
            }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Timer(modifier: Modifier, width: Dp =0.dp, height: Dp, boxcolor: Color, boxshadowColor: Color,
              text:String, textColor: Color, textSize: TextUnit, fontWeight: FontWeight=FontWeight.Normal,
              shape: RoundedCornerShape = RoundedCornerShape(0.dp,10.dp,10.dp,0.dp)
){
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = shape ,
        modifier = modifier
            .widthIn(50.dp, 250.dp)
            .height(height)
            .shadow(boxshadowColor, 1.dp, 1.dp, 18.dp, 0.dp, 10.dp)
            .clip(shape)){

    }
}

//  1 Day = 86400 sec ot TSU
//  1 Hr = 3600 sec
// 1 Min = 60


fun aa(start:Long ,curr:Long, end:Long): Flow<Pair<Long,Float>> = flow{
    (curr.. end).forEach {
        delay(1000)
        val temp= ((it-start).toFloat()/(end-start).toFloat())
      //  Log.d("qwerty",temp.toString())
        emit(Pair(it,temp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Circle(start:Long , curr:Long = 1685435190,end:Long , size:Dp=200.dp  , boxcolor:Color = MidnightBlue ,boxshadowColor: Color = Color.Black.copy(alpha  = .7f) , texts:Boolean = true){
   // Log.d("qwerty","Line 152 Start : $start || curr: $curr || end: $end ||\n Diff E-S : ${end-start}\n" +" Diff E-C : ${end - curr}")

    var progress by rememberSaveable{ mutableStateOf((((if(curr>end)end else curr)-start).toFloat()/(end-start).toFloat())) }
    var progressInTSU by rememberSaveable{ mutableStateOf( if(curr>end)end else curr) }
    var listOfColor = listOf(
        Color(0xFFff8ddb),
        Color(0xFF6DBCF5),
        Color(0xFF4299D8),
        Color(0xFFf048c6),
        Color(0xFFff8ddb),
    )
    val scope = rememberCoroutineScope()
    if(curr<end)
    LaunchedEffect(key1 = Unit){
        scope.launch(Dispatchers.IO) {
            aa(start,curr,end).collect{
                progressInTSU = it.first
                progress = it.second
            }
        }
    }
    else{
        listOfColor = listOf(
            Color(0xFFFD1F4C),
            Color(0xFFF7127D),
            Color(0xFFF840BB),
            Color(0xFFEE0083),
            Color(0xFFFF3139),
        )
    }
   // val transition by animateFloatAsState(targetValue = progress)
    Card(colors = CardDefaults.cardColors(containerColor = boxcolor),
        shape = RoundedCornerShape(15.dp) ,
        modifier = Modifier
            .padding(6.dp)
            .size(size)
            .shadow(boxshadowColor, 0.dp, 0.dp, 30.dp, 10.dp, 10.dp)){
        Box(modifier = Modifier
            .size(size)){
            Canvas(modifier = Modifier.size(size)){
                val canvasX = this.size.width
                val canvasY = this.size.height
                drawCircle(
                    brush = Brush.sweepGradient(listOfColor
                    ),
                    radius = (canvasY)-(canvasY/1.4f),
                    style = Stroke(((canvasY)-(canvasY/1.5f))*.26391f,cap = StrokeCap.Round),
                )
                drawArc(
                    color = MidnightBlue,
                    // radius = (canvasY)-(canvasY/1.62f),
                    startAngle = -90f,
                    sweepAngle = -360f+(360f*progress),
                    useCenter=true,
                    topLeft = Offset(canvasX/9,canvasY/9),
                    size = Size((canvasY)-(canvasY/5.43f),(canvasY)-(canvasY/5.43f))

                )

            }
            // String.format("%.3f",progress*100)
            if(texts){
                if(curr<end){
                    // Log.d("qwerty","------${((end-start) - (progressInTSU-start))}")
                    val day = ((end-start) - (progressInTSU-start))/86400
                    val hr = (((end-start) - (progressInTSU-start)) % 86400)/3600
                    // val min = ((((end-start) - (progressInTSU-start)) % 86400)%3600)/60
                    // val sec = ((((end-start) - (progressInTSU-start)) % 86400)%3600)%60
                    Column(modifier = Modifier.align(  Alignment.Center)) {
                        Text("${day}D ${hr}H" , color = Color.White , fontSize = 20.sp )
                        Text("left" , color = Color.White , fontSize = 16.sp ,modifier = Modifier.align(  Alignment.CenterHorizontally))
                    }
                }
                else{
                    Text("0D 0H" , color = Color.White , fontSize = 20.sp ,modifier = Modifier.align(  Alignment.Center))
                }
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)) {
                    Text(String.format("%.2f ",progress*100)+"% Passed" , color = Color.White , fontSize = 14.sp,modifier = Modifier.align(  Alignment.CenterHorizontally) )
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp) )
                }
            }


        }

    }
}


@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value:String,
    onValueChange:(String)->Unit,
    placeholder : String,
    paddingLeadingIconEnd: Dp = 0.dp,
    paddingTrailingIconStart: Dp = 0.dp,
    leadingIcon: (@Composable() () -> Unit)? = null,
    trailingIcon: (@Composable() () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    keyboardActions: KeyboardActions = KeyboardActions(),
    visualTransformation : VisualTransformation  = VisualTransformation.None,
    maxLines : Int = 10
) {
   // val state = rememberSaveable{ mutableStateOf("") }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = paddingLeadingIconEnd, end = paddingTrailingIconStart)
        ) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth().padding(0.dp,5.dp),
                value = value,
                onValueChange = { onValueChange(it) },
                textStyle = TextStyle(fontSize = 18.sp , color = Color.Black),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation =  visualTransformation,
                maxLines = maxLines
            )
            if (value.isEmpty()) {
                Text( text = placeholder ,  Modifier.fillMaxWidth().padding(0.dp,5.dp),color = Color.Gray )
            }
        }
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

@Composable
fun TextColumn(modifier:Modifier = Modifier,str1:String,str2:String,FW1:FontWeight,FW2:FontWeight,FSize1:TextUnit,FSize2:TextUnit){
    Column(modifier = modifier) {
        Text(str1, fontFamily = Jost, fontWeight = FW1, fontSize = FSize1 , lineHeight = 17.sp, maxLines = 2 , overflow = TextOverflow.Ellipsis,color = Color.Black)
        Text(str2, fontFamily = Jost, fontWeight = FW2, fontSize = FSize2,lineHeight = 10.sp , maxLines = 1 , overflow = TextOverflow.Ellipsis,color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreyCard(modifier:Modifier = Modifier ,paddingH:Dp=0.dp,paddingV:Dp=0.dp,backColor:Color = Color(0xffEDEDED),
             shape:RoundedCornerShape = RoundedCornerShape(15.dp),
             content: @Composable() (ColumnScope.() -> Unit)){
    val color by animateColorAsState(targetValue = backColor)
    Column(modifier = Modifier
      //  .fillMaxHeight()
        .padding(0.dp, 4.dp),verticalArrangement = Arrangement.Center){
        Card(shape = shape , colors =CardDefaults.cardColors(color),modifier = modifier){
           // Spacer(Modifier.weight(1f))
            Column(modifier = Modifier
                .fillMaxHeight()
                .padding(paddingH, paddingV)
                .animateContentSize()){
                content()
            }
           // Spacer(Modifier.weight(1f))

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBtn(text:String ,FS:TextUnit,onClick:()->Unit,backColor:Color = MidnightBlue,
             shape:RoundedCornerShape = RoundedCornerShape(10.dp)){
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(backColor),modifier = Modifier
        .heightIn(30.dp, 50.dp)
        .widthIn(70.dp, 150.dp), shape = shape
        ){
        Text(text, fontSize = FS , color = Color.White,fontWeight = FontWeight.ExtraBold , modifier = Modifier.padding(0.dp,0.dp))
    }

}

@Composable
fun RowForEvent(text:String ,FS:TextUnit,FW : FontWeight= FontWeight.Bold,onClick:()->Unit,backColor:Color =MidnightBlue,
             shape:RoundedCornerShape = RoundedCornerShape(14.dp), expand:Boolean,H:Dp,V:Dp ){

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(H, V)
        .background(Color.Transparent), verticalAlignment = Alignment.CenterVertically){
        Image(painter = painterResource(id = eventIcons[splitString(text).first]!!),null,modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(30.dp))
        Text(splitString(text).first , fontSize = FS , fontFamily = Jost , fontWeight = FW , color =  Color.Black)
        Spacer(Modifier.weight(1f))
        if(expand)Icon(Icons.Filled.ExpandMore , null ,modifier = Modifier
            .size(30.dp, 50.dp)
            .clickable { onClick() },tint = Color.Black)
        else when(splitString(text).second){
            "expandLess" -> Icon(Icons.Filled.ExpandLess , null ,modifier = Modifier
                .size(30.dp, 50.dp)
                .clickable { onClick() },tint = Color.Black)
            "Visibility" -> EventBtn(text = "SET" , FS = 15.sp, onClick = onClick,shape = shape, backColor = backColor)
            "Attachments" -> EventBtn(text = "CHOOSE" , FS = 15.sp, onClick = onClick,shape = shape, backColor = backColor)
            else ->  EventBtn(text = "SAVE" , FS = 15.sp, onClick = onClick , shape = shape, backColor = backColor)
        }



    }
}



@Composable
fun EventForm(memberModel: MemberModel , navController2: NavHostController){

    val eventCache = memberModel.event.collectAsState().value
    BackHandler(true) {
        memberModel.resetEventForm();
        memberModel.setCurr(Screen.EVENTS)
        navController2.popBackStack()
    }
    val context = LocalContext.current
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(IndigoInk) , horizontalAlignment = Alignment.CenterHorizontally){
        item {
            var color by remember{ mutableStateOf(Periwinkle) }
            var currExpand by rememberSaveable { mutableStateOf("") }
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH = 0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand == "Visibility")Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("Visibility" ,FS = 20.sp,FW = FontWeight.Medium,
                    onClick = {   if( currExpand != "Visibility") {
                        currExpand = "Visibility";
                        color = Color.White
                        /*TODO WHEN SET IS CLICKED*/
                    } else {currExpand = ""; color = Periwinkle}
                    }   ,
                    expand =  currExpand != "Visibility",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "Visibility"  , enter = expandVertically() ,exit = shrinkVertically()) {
                    Column(Modifier.heightIn(0.dp,400.dp)){
                        VisibiltyParent(memberModel)
                    }
                }
            }
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
                            placeholder = " Event Heading comes here !",
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
                            value = eventCache.title,
                            onValueChange = {
                                memberModel.setEventLiterals(it,1)
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
                            BasicTextField(value = eventCache.description, onValueChange = { memberModel.setEventLiterals(it ,2) } ,
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(300.dp, 1000.dp),
                            textStyle = TextStyle( fontSize = 16.sp)
                                )
                        }
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)){
                            GreyCard(Modifier.clickable {  }, paddingH = 4.dp, paddingV = 1.dp, backColor = Color(0xffd4d4d4), shape = RoundedCornerShape(14.dp)
                            ) {
                                Spacer(Modifier.height(10.dp))
                                Icon(Icons.Filled.FormatBold,null)
                            }
                            GreyCard(Modifier.clickable {  }, paddingH = 4.dp, paddingV = 1.dp, backColor = Color(0xffd4d4d4), shape = RoundedCornerShape(14.dp)
                            ) {
                                Spacer(Modifier.height(10.dp))
                                Icon(Icons.Filled.FormatItalic,null)
                            }
                            GreyCard(Modifier.clickable {  }, paddingH = 4.dp, paddingV = 1.dp, backColor = Color(0xffd4d4d4), shape = RoundedCornerShape(14.dp)
                            ) {
                                Spacer(Modifier.height(10.dp))
                                Icon(Icons.Filled.FormatColorText,null)
                            }
                            GreyCard(Modifier.clickable {  }, paddingH = 5.dp, paddingV = 0.dp, backColor = Color(0xffd4d4d4), shape = RoundedCornerShape(14.dp)
                            ) {
                                Spacer(Modifier.height(10.dp))
                                Text("24", fontSize = 16.sp)
                            }
                            GreyCard(Modifier.clickable {  }, paddingH = 9.dp, paddingV = 1.dp, backColor = Color(0xffd4d4d4), shape = RoundedCornerShape(15.dp)
                            ) {
                                Spacer(Modifier.height(10.dp))
                                Text("Cooperlithic", fontSize = 15.sp)
                            }
                        }
                    }

                }
            }
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH = 0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand == "Duration")Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {
                RowForEvent("Duration" ,FS = 20.sp, FontWeight.Medium,onClick = {
                    if( currExpand != "Duration") {
                        currExpand = "Duration";
                        color = Color.White
                        /*TODO WHEN SET IS CLICKED*/
                    } else {currExpand = ""; color = Periwinkle}
                } ,
                    expand = currExpand != "Duration",H=15.dp,V=0.dp)
                AnimatedVisibility(visible = currExpand == "Duration"  , enter = expandVertically() ,exit = shrinkVertically()) {
                    Column(){
                        DateField(memberModel,true)
                        Spacer(Modifier.height(5.dp))
                        DateField(memberModel,false)
                        Spacer(Modifier.height(10.dp))
                    }

                }
            }
            val docUri = memberModel.docUri
            val imgUri = memberModel.imgUri
            val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) {
                  docUri.addAll(it.map{
                          uri-> Log.d("qwerty",uri.toString()); uri.toString()
                  })
            }
            val launcher2 = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) {
                imgUri.addAll(it.map{
                        uri-> Log.d("qwerty",uri.toString()); uri.toString()
                })
            }
            GreyCard(
                Modifier.fillMaxWidth(.95f),
                paddingH = 0.dp,
                paddingV = 3.dp,
                backColor = if(currExpand == "Attachments")Color.White else Periwinkle,
                shape = RoundedCornerShape(30.dp)
            ) {
                var moreOptionsExpand by rememberSaveable{ mutableStateOf(false) }
                Box(Modifier.fillMaxWidth()){
                    RowForEvent("Attachments" ,FS = 20.sp, FontWeight.Medium,onClick = {
                        if( currExpand != "Attachments") {
                            currExpand = "Attachments";
                            color = Color.White
                        } else {
                            /*TODO Here we open intent to select multiple document*/
                            // currExpand = ""; color = Periwinkle
                            moreOptionsExpand = !moreOptionsExpand

                        } } ,

                        expand = currExpand != "Attachments",H=15.dp,V=0.dp)
                    DropdownMenu(
                        offset = DpOffset(650.dp,0.dp),
                        modifier = Modifier
                            .background(Color.White)
                            .widthIn(100.dp, 150.dp),
                        expanded = moreOptionsExpand,
                        onDismissRequest = { moreOptionsExpand = false },
                        properties = PopupProperties(focusable=false),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(150.dp)){
                            ItemOption("DOCUMENT", onClick = {
                                launcher.launch( arrayOf("application/pdf", "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint", "text/csv"))
                            }
                            )
                            ItemOption("IMAGES" , onClick = { launcher2.launch("image/*") })
                        }

                    }

                }

                AnimatedVisibility(visible = currExpand == "Attachments"  ,enter = expandVertically() ,exit = shrinkVertically()) {
                    Column(Modifier.heightIn(0.dp,500.dp)){
                        LazyColumn{
                            itemsIndexed(docUri){index,it->
                                val pair = getFileNameAndLength(context ,Uri.parse(it))
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
                                        .clickable { /*TODO when DOc is clicked*/ openLocalFile(
                                            context, Uri.parse(it),
                                            documentMime[getFileExtension(it)]!!.first
                                        )
                                        },
                                    paddingH = 10.dp,
                                    paddingV = 4.dp,
                                    shape= RoundedCornerShape(25.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                                        Image(painterResource(littleIcons[getFileExtension(it)]!!),null,Modifier.size(50.dp,50.dp))
                                        Spacer(Modifier.width(10.dp))
                                        TextColumn(Modifier.widthIn(0.dp,240.dp),str1 = pair.first!! , str2 = bytesToMegabytes(pair.second)+" MB" , FW1 = FontWeight.Normal, FW2 = FontWeight.Bold , FSize1 = 16.sp, FSize2 = 14.sp)
                                        Spacer(Modifier.width(5.dp))
                                        IconButton(onClick = { docUri.removeAt(index) }) {
                                            Image(painterResource(R.drawable.trash),null,Modifier.size(50.dp,50.dp))
                                        }
                                    }
                                }
                              //  Spacer(Modifier.height(1.dp))
                            }
                            itemsIndexed(imgUri){index,it->
                                val pair = getFileNameAndLength(context ,Uri.parse(it))
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
                                        .clickable { /*TODO when DOc is clicked*/ openLocalImage(
                                            context, Uri.parse(it)
                                        )
                                        },
                                    paddingH = 10.dp,
                                    paddingV = 4.dp,
                                    shape= RoundedCornerShape(25.dp)
                                    ) {
                                    Row(modifier = Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.SpaceBetween){
                                        Image(bitmap = uriToImageBitmap(Uri.parse(it),context)!!,null,
                                            Modifier
                                                .size(50.dp, 54.dp)
                                                .clip(
                                                    RoundedCornerShape(13.dp)
                                                ), contentScale = ContentScale.Crop)
                                        Spacer(Modifier.width(10.dp))
                                        TextColumn(Modifier.widthIn(0.dp,240.dp),str1 = pair.first!! , str2 = bytesToMegabytes(pair.second)+" MB" , FW1 = FontWeight.Normal, FW2 = FontWeight.Bold , FSize1 = 16.sp, FSize2 = 14.sp)
                                        Spacer(Modifier.width(5.dp))
                                        IconButton(onClick = { imgUri.removeAt(index)  }) {
                                            Image(painterResource(R.drawable.trash),null,Modifier.size(50.dp,50.dp))
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

fun openLocalFile(context: Context, uri: Uri , mimeType:String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.d("qwerty","not fintdf")
    }
}

fun openLocalImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.d("qwerty","not fimage") // Image viewer app not found, handle the exception
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DateField(memberModel: MemberModel , start:Boolean){

    val data = if(start)memberModel.Sdate.collectAsState().value else memberModel.Edate.collectAsState().value
    Row(modifier = Modifier
        .fillMaxWidth(1f)
        .height(60.dp)
        .clip(RoundedCornerShape(25.dp))
        .background(
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xffd4d4d4), Periwinkle
                )
            )
        )
        , verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Spacer(Modifier.width(10.dp))
        Image(painter = painterResource(id = if(start)R.drawable.startline else R.drawable.finishh),null , Modifier.size(30.dp))
        Spacer(Modifier.width(10.dp))
        Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically){
            Tfield(value = data.DD, onValueChange = {memberModel.setDate(it,1,start)}, w = 43, h = 42,placeHolder = "DD",len =2)
            Text(modifier = Modifier, text = "." , color = Color.Black , fontSize = 20.sp)
            Tfield(value = data.MM, onValueChange = {memberModel.setDate(it,2,start)}, w = 43, h = 42,placeHolder = "MM",len =2)
            Text(modifier = Modifier, text = "." , color = Color.Black , fontSize = 20.sp)
            Tfield(value = data.YYYY, onValueChange = {memberModel.setDate(it,3,start)}, w = 64, h = 42,placeHolder = "YYYY",len =4)
            Spacer(Modifier.width(10.dp))
            Tfield(value = data.HH, onValueChange = {memberModel.setDate(it,4,start)}, w = 42, h = 42,placeHolder = "HH",len =2)
            Text(modifier = Modifier, text = ":" , color = Color.Black , fontSize = 20.sp)
            Tfield(value = data.MN, onValueChange = {memberModel.setDate(it,5,start)}, w = 43, h = 42,placeHolder = "MM",len =2)
            Spacer(Modifier.width(5.dp))
            Tfield(value = data.ap, onValueChange = {memberModel.setDate(it,6,start)}, w = 46, h = 42,placeHolder = "A/P" , "Alp",len =1)

        }
        Spacer(Modifier.width(10.dp))
    }
}

@Composable
fun Tfield(value:String , onValueChange: (String) -> Unit , w:Int,h:Int , placeHolder: String , KbType :String ="Num", len:Int){
    var colorr by remember { mutableStateOf(Color.Gray) }
    val color by animateColorAsState(targetValue = colorr)
    Box(contentAlignment = Alignment.Center ,
        modifier = Modifier
            .size(w.dp, h.dp)
            .border(1.dp, color, RoundedCornerShape(16.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {

        BasicTextField(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp, 0.dp),
            value = value,
            onValueChange = {
                colorr = if(it.isNotBlank() && KbType!="Num" && !(it[it.length-1]=='a' || it[it.length-1]=='A'||it[it.length-1]=='P'||it[it.length-1]=='p')) {
                    Color.Red
                }  else Color.Gray
                if(it.length<=len)onValueChange(it) },
            textStyle = TextStyle(fontSize = 20.sp , color = Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = if(KbType=="Num") KeyboardType.Number else KeyboardType.Text , imeAction = ImeAction.Next),

            )
        if (value.isEmpty()) {
            Text(modifier = Modifier.align(Alignment.Center) , text = placeHolder , color = Color.Gray , fontSize = 17.sp)
        }
    }

}