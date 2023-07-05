package com.cyrax.commuin.functions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.cyrax.commuin.struct.DocEntity
import com.cyrax.commuin.struct.ImageEntity
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.db
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL



fun _to12Hr(time:String):String{
    var a = time[0].digitToInt()*10
    a+=time[1].digitToInt()
    if(a==12 || a==24)return "12"+time.substring(2)
    a %= 12
    return a.toString()+time.substring(2)

}

fun UriToByteArray(uri: Uri? , context: Context): Flow<ByteArray> {
    return callbackFlow{
        GlobalScope.launch (Dispatchers.IO){
            withContext(Dispatchers.IO) {
                val stream1 = URL(uri.toString()).openStream()
                val bitmap  =  BitmapFactory.decodeStream(stream1)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                trySend(stream.toByteArray())
            }
        }

        awaitClose {  }
    }

}

fun uritoArr(uri: Uri? , context: Context):ByteArray{
    val source = ImageDecoder.createSource(context.contentResolver, uri!!)
    val bitmap =  ImageDecoder.decodeBitmap(source)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val byteArray = stream.toByteArray()

    return byteArray
}

fun uriToArrPdf(context: Context, uri: Uri): ByteArray? {
    val inputStream = context.contentResolver.openInputStream(uri)
    return inputStream?.buffered()?.use { it.readBytes() }
}

fun UriToBitmap(uri: Uri? , context: Context): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri!!)
    val bitmap =  ImageDecoder.decodeBitmap(source)
    return bitmap
}

fun BitmapToByteArray(bitmap :Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val byteArray = stream.toByteArray()

    return byteArray
}


fun bitmapToString(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun byteArrayToBitmap(byteArray:ByteArray) =  (BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))

fun byteArrayToStringBase64(byteArray:ByteArray) =  Base64.encodeToString(byteArray, Base64.DEFAULT)

fun stringBase64toBitmap(base64String:String) = run {
    val byteArray = Base64.decode(base64String, Base64.DEFAULT)
    byteArrayToBitmap(byteArray)
}

fun createFolder(context: Context, folderName: String , subPath:String ) {
    val resolver: ContentResolver = context.contentResolver

    // Define the storage location and the folder name
    val volumeName = MediaStore.VOLUME_EXTERNAL_PRIMARY
    val parentDir = Environment.DIRECTORY_DOCUMENTS
    val fullPath = "$parentDir/$subPath/$folderName"

    val file = File(fullPath)
    if(file.exists()){return}

    // Check if the folder already exists
    val collection = MediaStore.Files.getContentUri(volumeName)
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
    val selectionArgs = arrayOf(fullPath)
    val sortOrder = null

    resolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        if (cursor.count > 0) {
            // Folder already exists, no need to create a new one
            Log.d("SUCCESS", "Folder already exists")
            return
        }
    }

    // Create the ContentValues object with the folder details
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, folderName)
        put(MediaStore.MediaColumns.MIME_TYPE, "vnd.android.document/directory")
        put(MediaStore.MediaColumns.RELATIVE_PATH, fullPath)
    }

    // Insert the folder using MediaStore
    val uri: Uri? = resolver.insert(collection, values)

    // Check if the folder was created successfully
    if (uri != null) {
        Log.d("SUCCESS", "Folder created successfully")
    } else {
        Log.d("SUCCESS", "Failed to create folder")
    }
}


fun createFolderUnder(context: Context, folderName: String) {
    val appDataDir = context.getExternalFilesDir(null)?.parentFile

    // Define the parent directory path
    val parentDirPath = "${appDataDir?.path}/$folderName"

    // Create the parent directory
    val parentDir = File(parentDirPath)
    if (!parentDir.exists()) {
        parentDir.mkdirs()
    }

    // Check if the folder was created successfully
    if (parentDir.exists()) {
        Log.d("SUCCEESS" , "DONE")
    } else {
        Log.d("SUCCEESS" , "Shit")
    }
}






suspend fun storePdfFromByteArray(context: Context,  folderName: String, fileName: String) {
    withContext(Dispatchers.IO) {
        val byteArray  = FirebaseStorage.getInstance().reference.child("placeholder.jpg").getBytes(104857600).await()
        // Define the parent directory path
        val appDataDir = context.getExternalFilesDir(null)?.parentFile
        val parentDirPath = "${appDataDir?.path}/$folderName"

        // Create the parent directory if it doesn't exist
        val parentDir = File(parentDirPath)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        // Create a local file to store the PDF
        val file = File(parentDir, fileName)

        // Write the byte array to the file
        FileOutputStream(file).use { outputStream ->
            outputStream.write(byteArray)
        }
    }

}

@SuppressLint("SuspiciousIndentation")
suspend fun downloadFromByteArray(
    context: Context,
    pathToDwnld: String,
    pathFromDwnld:String="",
    fileName: String ,
    byteArr : ByteArray? = null ,
    fileType:String ="",
    nameForDBRef:String ,
):String?{
    return withContext(Dispatchers.IO) {
        Log.d("qwerty","-------Download called from line 239 $fileName $fileType-\n")
        val byteArray = try {
            byteArr ?: FirebaseStorage.getInstance().reference.child(pathFromDwnld).getBytes(104857600).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if(byteArray == null || pathToDwnld.isBlank()) return@withContext null
            // Define the parent directory path
        val downloadsDir  = File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), pathToDwnld ).apply { mkdirs() }

        val file = File(downloadsDir, fileName)

        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArray)
            fileOutputStream.close()

                // File downloaded successfully
                // You can perform any further actions here
            val DB = db.getDatabase(context)
            when(fileType){
                    "--::--img" ->  DB.image().insertImage(ImageEntity(nameForDBRef,Uri.fromFile(file).toString()))
                    "--::--doc"->   DB.doc().insertDoc(DocEntity(nameForDBRef,Uri.fromFile(file).toString()))
                    "--::--vid"-> {}
                    else -> {}
                }
                Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                // Error occurred during file download
                // Handle the error accordingly
                null
            }
    }
}


fun deleteDirectory(path:String) {
    val pathWithSpace = Uri.decode(path).replace("%20", " ")
    val file = File( pathWithSpace.substring(7))
    Log.d("qwerty",file.canonicalPath)
    try {
        if (file.exists()) {
            if(file.deleteRecursively())
                Log.d("qwerty","Success")
            else{
                Log.d("qwerty","Error")
            }
        } else {
            Log.d("qwerty","No file")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("qwerty",e.message?:"Exception")
    }
}

fun deleteFileInDirectory(path:String) {
    val pathWithSpace = Uri.decode(path).replace("%20", " ")
    val file = File( pathWithSpace.substring(7))
   // val file = File( "/storage/emulated/0/Documents/Commuin/testOrg_001/Ujjwal_15039/Events/TPO declaration/COMM_DOC_06061685853194159.pdf")
    Log.d("qwerty",file.canonicalPath)
    try {
        if (file.exists()) {
            if(file.delete())
            Log.d("qwerty","Success")
            else{
                Log.d("qwerty","Error")
            }
        } else {
            Log.d("qwerty","No file")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("qwerty",e.message?:"Exception")
    }
}


fun Modifier.shadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    edgeRadiusX:Dp = 0.dp,
    edgeRadiusY:Dp = 0.dp,

) = then(
    drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter = (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
            }
            frameworkPaint.color = color.toArgb()

            val leftPixel = offsetX.toPx()
            val topPixel = offsetY.toPx()
            val radiusX = edgeRadiusX.toPx()
            val radiusY = edgeRadiusY.toPx()
            val rightPixel = size.width + topPixel
            val bottomPixel = size.height + leftPixel

            canvas.drawRoundRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                paint = paint,
                radiusX = radiusX,
                radiusY = radiusY
            )

        //    canvas.drawOval()
        }
    }
)


fun Modifier.shadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    size:Int = 0,
    ) = then(
    drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter = (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
            }
            frameworkPaint.color = color.toArgb()

            val oX = ((size / 2).dp+offsetX).toPx()               //The formula that i have set aligns the circle formed to the centre of the Button of Parent composable
            val oY = ((size / 2).dp+offsetY).toPx()                   // If it werent there the circle drawn was off size and positon with huge margin.
            val radii = (size - (.5 * size)).dp.toPx()

            canvas.drawCircle(
                radius=radii,
                center = Offset(oX,oY),
                paint = paint,
            )

            //    canvas.drawOval()
        }
    }
)




