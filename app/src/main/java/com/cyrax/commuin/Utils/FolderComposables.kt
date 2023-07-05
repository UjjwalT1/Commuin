package com.cyrax.commuin.Utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.cyrax.commuin.R
import com.cyrax.commuin.functions.deleteFileInDirectory
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.functions.uriToArrPdf
import com.cyrax.commuin.functions.uritoArr
import com.cyrax.commuin.sections.CustomTextField
import com.cyrax.commuin.sections.GreyCard
import com.cyrax.commuin.sections.ItemOption
import com.cyrax.commuin.sections.MiniImage
import com.cyrax.commuin.sections.MiniText
import com.cyrax.commuin.sections.NewTopBarSearch
import com.cyrax.commuin.sections.SendDoc
import com.cyrax.commuin.sections.SendImg
import com.cyrax.commuin.sections.TOAST
import com.cyrax.commuin.sections.mapToArrayforMember
import com.cyrax.commuin.sections.openFileFromByteArray
import com.cyrax.commuin.sections.openPdf
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.db
import com.cyrax.commuin.struct.documentMime
import com.cyrax.commuin.struct.folderIcons
import com.cyrax.commuin.struct.greyPeriList
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.RainForest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.io.File
import java.io.FileNotFoundException
import java.util.Stack


@SuppressLint("SuspiciousIndentation")
@Composable
fun Directory(memberModel: MemberModel  , onBackClick: () -> Unit){
    var refresh by remember{ mutableStateOf("")}
    val context = LocalContext.current
    val meForFolder = memberModel.myData.collectAsState().value
    var currPath by rememberSaveable{ mutableStateOf("${meForFolder.profileDetail.organName}_${meForFolder.orgID}/${meForFolder.profileDetail.name}_${meForFolder.memberID}") }
    val fileList = remember { mutableStateListOf<File>() }
    val filesArray = if(memberModel.topBarQuery.collectAsState().value.isBlank())listOfFiles(currPath) else { filterListOfFiles(currPath,memberModel.topBarQuery.collectAsState().value) }
    filesArray?.let {
            files -> fileList.clear() ;
        fileList.addAll(files)
    }
    var currDir by rememberSaveable{ mutableStateOf("${meForFolder.profileDetail.name}_${meForFolder.memberID}")}
    val stk:Stack<Pair<String,String>> = memberModel.stk
    BackHandler(true) {  if(currDir != "${meForFolder.profileDetail.name}_${meForFolder.memberID}") {
        currPath = stk.peek().first;
        currDir = stk.peek().second;
        stk.pop()
    } else { onBackClick() } }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(
            brush =
            Brush.linearGradient(listOf(Color(0xff1e1f26), Color(0xff283655)))
        )){
        GreyCard(
            Modifier
                .height(50.dp)
                .shadow(Color.White.copy(alpha = .5f), 1.dp, 1.dp, 5.dp, 100.dp, 100.dp),shape = RoundedCornerShape(35.dp)) {
            TopbarDir(memberModel = memberModel, str = currDir , path = "", onBackClick = {
                if(currDir != "${meForFolder.profileDetail.name}_${meForFolder.memberID}"){
                    currPath = stk.peek().first;
                    currDir = stk.peek().second;
                    stk.pop()
                } else {  onBackClick() }
            } , {} , onBackClick)
        }
        if(fileList.size == 0){
            Column(
                Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)) {
                    Image(painter = painterResource(id = R.drawable.blankdir), contentDescription = null , Modifier.size(200.dp) )
            }
        }
        else
        LazyVerticalGrid(columns = GridCells.Adaptive(140.dp),verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .background(Color.Transparent) ){
            items(fileList){
                if(it.isDirectory())
                    Folder(it.name ,  {
                        memberModel.setTBQuery("")
                        stk.push(Pair(currPath,currDir))
                        currPath += "/${it.name}";
                        currDir = it.name
                                      } , it.name )
                else{
                    val extension = it.name.substring(it.name.length-3)
                    when(extension){
                        "pdf","ppt","doc","csv","xls" -> Ffile(memberModel,it.name , { uri-> openFileFromByteArray(context, uriToArrPdf(context, Uri.parse(uri))!! ,it.name ,
                            documentMime[extension]!!.first, documentMime[extension]!!.second)} , extension , { refresh += "a" })
                        "webp" -> {}
                        "html" -> {}
                        "apk" -> {}
                        "jpg","jpeg","png" -> ImageFile(memberModel,it.name , {})
                        else -> {}
                    }
                }
            }
        }
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Folder(name:String ="Images", onClick:()->Unit = {} , folderIcon:String){
    val context = LocalContext.current
    Card(onClick = onClick, Modifier.size(160.dp,200.dp)) {
        Box(){
//            Row(){
//                Spacer(Modifier.weight(1f))
//                IconButton(modifier = Modifier,onClick = { /*TODO*/ }) {
//                    Image(painter = painterResource(id = R.drawable.more_vertical) , null , modifier = Modifier.size(28.dp))
//                }
//            }
            Column(modifier = Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(25.dp))
                Image(painter = painterResource(id =  folderIcons[folderIcon]?:R.drawable.folder) , null , modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.CenterHorizontally) )
                Spacer(Modifier.height(10.dp))
                Text(modifier = Modifier.padding(9.dp,0.dp,9.dp,0.dp),text = name , fontWeight = FontWeight.SemiBold, fontSize = 20.sp  , maxLines = 2 , overflow = TextOverflow.Ellipsis)

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ffile(memberModel: MemberModel,name:String , onClick:(String)->Unit = {} , folderIcon:String , refresh:()->Unit){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var moreOptionsExpand:Boolean by rememberSaveable { mutableStateOf(false) }
    var uri by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(key1 = Unit){
        val DB = db.getDatabase(context)
        scope.launch(Dispatchers.Default) {
            uri = DB.doc().obtainDoc(fileNameWithExtnRemoved(name))?.uri
        }
    }

    Card(onClick = {
        if(uri!=null){
            Log.d("qwerty" , uri!!)
            memberModel.setTBQuery("")
            onClick(uri!!)
        }}, modifier = Modifier.size(160.dp,200.dp)) {
        Box(){
            Row(){
                Spacer(Modifier.weight(1f))
                Box(){
                IconButton(modifier = Modifier,onClick = { moreOptionsExpand = !moreOptionsExpand }) {
                    Image(painter = painterResource(id = R.drawable.more_vertical) , null , modifier = Modifier.size(28.dp))
                }
                DropdownMenu(
                    modifier = Modifier.background(Brush.horizontalGradient(greyPeriList)),
                    expanded = moreOptionsExpand,
                    onDismissRequest = { moreOptionsExpand = false },
                    properties = PopupProperties(focusable=false),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .background(
                            Brush.horizontalGradient(greyPeriList)
                        )){
                        ItemOption("DELETE" , onClick =
                        { if (uri != null)  deleteFileInDirectory(uri!!) } , dialog = true , "Delete File" , "Sure you want to delete : $name"  )
                        ItemOption("SHARE",{})
                        ItemOption("SHARE VIA",
                            { memberModel.share(context, uri!!, folderIcon) }
                            )
                    }

                }}
            }
            Column(modifier = Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(25.dp))
                Image(painter = painterResource(id = folderIcons[folderIcon]?:R.drawable.folder) , null , modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.CenterHorizontally) )
                Spacer(Modifier.height(10.dp))
                Text(modifier = Modifier.padding(9.dp,0.dp,9.dp,0.dp),text = name , fontSize = 15.sp  , maxLines = 2 , overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageFile(memberModel: MemberModel,name:String ="COMM_DOC_fff237483745623575323.pdf", onClick:()->Unit = {}){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uri:String? by rememberSaveable { mutableStateOf(null) }
    var moreOptionsExpand:Boolean by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit){
        val DB = db.getDatabase(context)
        scope.launch(Dispatchers.Default) {
            uri = DB.image().obtainImage(fileNameWithExtnRemoved(name))?.uri
        }
    }
    var fullview by rememberSaveable { mutableStateOf(false) }
    if(fullview){
        FullImage(uri!! , { fullview = false})

    }
    Card(onClick = { onClick.invoke() }, modifier = Modifier.size(160.dp,200.dp)) {
        Box(){
            Row(){
                Spacer(Modifier.weight(1f))
                Box(){
                    IconButton(modifier = Modifier,onClick = { moreOptionsExpand = !moreOptionsExpand }) {
                        Image(painter = painterResource(id = R.drawable.more_vertical) , null , modifier = Modifier.size(28.dp))
                    }
                    DropdownMenu(
                        modifier = Modifier.background(Brush.horizontalGradient(greyPeriList)),
                        expanded = moreOptionsExpand,
                        onDismissRequest = { moreOptionsExpand = false },
                        properties = PopupProperties(focusable=false),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(
                            Brush.horizontalGradient(greyPeriList))){
                            ItemOption("DELETE" , onClick =
                            { if (uri != null)  deleteFileInDirectory(uri!!) } , dialog = true , "Delete File" , "Sure you want to delete : $name"  )
                            ItemOption("SHARE",{})
                            ItemOption("SHARE VIA",{memberModel.share(context, uri!!,"img")})
                            ItemOption("View In Gallery",{ openFileFromByteArray(context,uritoArr(Uri.parse(uri),context),"$name","image/*","png") })
                        }

                    }}
            }
            Column(modifier = Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(35.dp))

               if(uri!=null)AsyncImage(model = uri , null , modifier = Modifier
                   .size(100.dp)
                   .clip(RoundedCornerShape(10.dp))
                   .clickable { fullview = true }
                   .align(Alignment.CenterHorizontally) , contentScale = ContentScale.Crop)
                Spacer(Modifier.height(10.dp))
                Text(modifier = Modifier.padding(9.dp,0.dp,9.dp,0.dp),text = name , fontSize = 15.sp  , maxLines = 2 , overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
fun FullImage(uri:String="",onDismiss:()->Unit){
    var zoomed by remember { mutableStateOf(false) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    Dialog(onDismissRequest = onDismiss ) {
        AsyncImage(model = uri, contentDescription =null , modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        zoomOffset = if (zoomed) Offset.Zero else Offset(8f, 8f)
                        zoomed = !zoomed
                    }
                )
            }
            .graphicsLayer {
                scaleX = if (zoomed) 2f else 1f
                scaleY = if (zoomed) 2f else 1f
                translationX = zoomOffset.x
                translationY = zoomOffset.y
            }
        )

    }
}


@Composable
fun TopbarDir(
    memberModel : MemberModel,
    str :String ,
    path:String ,
    onBackClick:()->Unit,
    onSearchClick:()->Unit,
    onCloseClick:()->Unit,
){
    var search by rememberSaveable{ mutableStateOf(false) }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val query = memberModel.topBarQuery.collectAsState().value
    //if(memberModel.topBarQuery.collectAsState().value.isBlank())LaunchedEffect(key1 = Unit){search =false}
    LargeTopAppBar(
        colors = TopAppBarDefaults.largeTopAppBarColors(  containerColor = Color(0xFF1e1f26)),
        modifier = Modifier
            .height(52.dp)
            .shadow(Color(0xffd0e1f9).copy(alpha = .4f), 0.dp, 0.dp, 10.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        title = {},
        navigationIcon = {
            Row{

                IconButton(onClick = { if(!search)onBackClick() else {
                    memberModel.setTBQuery("")
                    search = false
                }} ) {
                    if(!search)Image(painter = painterResource(id = R.drawable.left_chevron), contentDescription = null,modifier = Modifier.size(41.dp))
                    else Image(painter = painterResource(id = R.drawable.closetwo), contentDescription = null,modifier = Modifier.size(21.dp))
                }
                Spacer(modifier = Modifier.width(30.dp))
                if(search== false)Row(modifier = Modifier.fillMaxHeight(),verticalAlignment = Alignment.CenterVertically) {
                    Text(str, style = MaterialTheme.typography.titleLarge , fontWeight = FontWeight.SemiBold , color = Color(0xFFd0e1f9))
                }

            }},
        actions={
            Row(verticalAlignment = Alignment.CenterVertically){
                if(search){
                Row(modifier = Modifier
                    .width(200.dp)
                    .padding(8.dp, 0.dp)
                    .border(width = 1.5.dp, color = Color.Black, RoundedCornerShape(50))){
                    CustomTextField(value = query,
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(greyPeriList),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(8.dp, 0.dp, 6.dp, 0.dp)
                            .focusRequester(focusRequester)
                        ,
                        onValueChange = {
                            if(it.length == 20 && query.length==19  ) TOAST(context,"Query length reached!")
                            else if(it.isNotBlank()){
                                memberModel.setTBQuery(it) ;
                            }
                            else{
                                memberModel.setTBQuery(it) ;
                            } },
                        placeholder = "Search"
                    )

                }
            }
                IconButton(onClick = {
                    search = !search
                    if(search == false) memberModel.setTBQuery("")
                    scope.launch {
                        delay(100)
                        focusRequester.requestFocus()
                    }
                } ) {
                    Image(painter = painterResource(id = R.drawable.search), contentDescription =null , modifier = Modifier.size(28.dp) )
                }
                IconButton(onClick =  onCloseClick   ) {
                    Image(painter = painterResource(id = R.drawable.close), contentDescription =null , modifier = Modifier.size(38.dp) )
                }
            } }
    )

}

fun listOfFiles(path:String): Array<out File>? {
    return File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Commuin/${path}").listFiles()
}

fun filterListOfFiles(path:String,query:String): Array<out File>? {
    return File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Commuin/${path}").listFiles { dir, name ->
        name.lowercase().contains(query.lowercase())
    }
}

fun fileNameWithExtnRemoved(str:String):String{
    var ans = "" ; var i=0
    while(str[i]!='.'){
           ans+=str[i]
        i++
    }
    return ans
}

fun uriToImageBitmap(uri: Uri,context: Context): ImageBitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val drawable = Drawable.createFromStream(inputStream, uri.toString())
        val bitmap = drawable!!.toBitmap()
        bitmap.asImageBitmap()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}
