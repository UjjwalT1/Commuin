package com.cyrax.commuin.Utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.cyrax.commuin.functions._to12Hr
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun getFileNameAndLength(context: Context, uri: Uri): Pair<String?, Long> {
    val contentResolver: ContentResolver = context.contentResolver

    val documentFile: DocumentFile? = DocumentFile.fromSingleUri(context, uri)

    val fileName: String? = documentFile?.name
    val fileLength: Long = documentFile?.length() ?: 0

    return Pair(fileName, fileLength)
}

fun convertTimeStringToTimestamp(timeString: String): Long {
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val date = format.parse(timeString)
   // Log.d("qwerty","Line 29 Time String : $timeString to TS : ${date?.time ?: 0L}")
   // convertTimestampToDate(date?.time ?: 0L)
   // Log.d("qwerty","Line 31 CUrrTime Milllis : ${System.currentTimeMillis()}")
   // convertTimestampToDate(System.currentTimeMillis())
    return date?.time ?: 0L
}

fun convertTimestampToDate(timestamp: Long):String{
    val datet = Date(timestamp)
    val formatter1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = formatter1.format(datet)
    val inputFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
    val time = _to12Hr(inputFormat.format(datet) )
  //  Log.d("qwerty","Line 39 TS : $timestamp to TimeString : $date $time")
    return "$date   $time"
}

fun convert12HrTo24Hr(str: String): String {
    val temp = str.toInt() + 12
  //  Log.d("qwerty","Line 45 Temp is $temp")
    if(temp == 24) return "00"
    return temp.toString()

}

fun splitString(str:String):Pair<String,String>{

    val a = try{Pair( str.substring(0,str.indexOf(" ")) ,str.substring(str.indexOf(" ")+1))}
    catch(_:Exception){
        Pair(str,str)
    }
 //   Log.d("qwerty","Line 60MoreFunc ${a.first} ${a.second}")
    return a
}

