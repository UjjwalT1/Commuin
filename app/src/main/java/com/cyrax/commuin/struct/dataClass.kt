package com.cyrax.commuin.struct

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cyrax.commuin.R
import com.cyrax.commuin.ui.theme.Periwinkle
import kotlinx.serialization.Serializable

/*          val dbref = Firebase.firestore
            dbref.collection("temp").document("b").get().addOnSuccessListener {
               Log.d(TAG,"----------------------${ it.toObject(m::class.java)?.b }---------------------")
            }*/




@Serializable
data class classOrgSignUp(var orgID:String="", var orgName:String="", var email:String="",var password:String="",
                          var dp:String="",
                          var orgDepartments:MutableMap<String,Int> = mutableMapOf(),var designations:MutableMap<String,Int> = mutableMapOf(),
//                          var feedIDs:MutableMap<String,Int> =mutableMapOf(),var chats:MutableMap<String,Int> =mutableMapOf(),
//                          var events:MutableMap<String,Int> =mutableMapOf(),var groups:MutableMap<String,Int> =mutableMapOf(),
                          var deptMember:MutableMap<String,MutableMap<String,MutableMap<String,classOrgMember>>> = mutableMapOf() // DeptName->Designation->MemberID->Details
)
@Serializable
data class classMemSignUp(var orgID:String="", var dept:String="", var designation:String="", var memberID:String="", var password:String="",
                          var profileDetail:classOrgMember= classOrgMember()

)

@Serializable
data class classOrgMember(var organName:String="",var memberID:String="",var name:String="",var email:String="",var contact:String = "",var designation:String="",var dept:String="")

@Serializable
data class ClassLoginForm(var orgID:String="", val memID:String="",val Password:String="")

@Serializable
data class dAndD(var depts:MutableList<String> = mutableListOf(),var designations:MutableList<String> = mutableListOf())

enum class Screen( val iconInactive: ImageVector,val iconActive:ImageVector , var address:String){
    HOME(Icons.Outlined.Home , Icons.Filled.Home , "HOME"),
    CHATS(Icons.Outlined.Chat,Icons.Filled.Chat , "CHATS"),
    GroupConv(Icons.Outlined.Chat,Icons.Filled.Chat , "GroupConvList"),
    GROUPS(Icons.Outlined.Group,Icons.Filled.Group , "GROUPS"),
    CGROUPS(Icons.Outlined.Group,Icons.Filled.Group , "Create Group"),
    EVENTS(Icons.Outlined.Event,Icons.Filled.Event , "EVENTS"),
    CEVENTS(Icons.Outlined.Event,Icons.Filled.Event , "Create Event"),
    EEVENTS(Icons.Outlined.Event,Icons.Filled.Event , "Posted EventP"),
    PROFILE(Icons.Filled.MoreVert,Icons.Filled.ArrowBackIosNew , "PROFILE"),
    Members( Icons.Outlined.ManageAccounts,Icons.Filled.ManageAccounts , "Members"),
    Departments( Icons.Outlined.Domain,Icons.Filled.Domain , "Department" ),
    Designations( Icons.Outlined.Badge,Icons.Filled.Badge  , "Designations"),
    LogOut( Icons.Outlined.Logout,Icons.Filled.Logout  , "LogOut"),
    Files( Icons.Outlined.FileOpen,Icons.Filled.FileOpen  , "Files"),
    MyOrganisation(Icons.Outlined.CorporateFare,Icons.Filled.CorporateFare , "My Organisation"),
    Themes(Icons.Outlined.Brush,Icons.Filled.Brush , "Themes"),
}

data class Dateobj(var DD:String="", var MM:String="", var YYYY:String="", var HH:String="11", var MN:String="59", var ap:String="p")
//data class Dateobj(var DD:String="", var MM:String="", var YYYY:String="", var HH:String="", var MN:String="", var ap:String="")

val supplemntIcon : Map<String , Int> = mapOf("Themes" to R.drawable.brush ,"My Organisation" to R.drawable.company ,"LogOut" to R.drawable.sign_out , "Files" to R.drawable.file)


var sidebarItemsOrg = listOf( Screen.Files , Screen.Members , Screen.Departments , Screen.Designations , Screen.LogOut  )
var sidebarItemsMem = listOf( Screen.Files , Screen.MyOrganisation, Screen.Themes ,Screen.LogOut  )


data class myOrg(val members : MutableMap<String,MutableMap<String,MutableMap<String,classOrgMember>>> = mutableMapOf())


@Serializable
data class MessageToSend(var uniqueID:String="", val msg:String="", val date:String="", val time:String="", val room:String="",val type:String="",val extra:String="" , val extn:String="")

@Serializable
data class MessageToSendGP(var uniqueID:String="", val msg:String="", val date:String="", val time:String="", val grpID:String="",val type:String="",val extra:String="" , val extn:String="",
                            var whoSendDept :String= "", var whoSendDesig:String="",val whoSendID:String="",)

fun MessageGrp.toMessageToSendGP():MessageToSendGP{
    return MessageToSendGP(this.uniqueID, this.msg, this.date, this.time, this.grpID, this.type, this.extra, this.extn, this.whoSendDept, this.whoSendDesig, this.whoSendID)
}

fun MessageToSendGP.toMessageToSend():MessageToSend{
    return MessageToSend(this.uniqueID, this.msg, this.date, this.time , room = grpID,type = type , extra, extn)
}
fun MessageToSendGP.toMessageGrp():MessageGrp{
    return MessageGrp(uniqueID = this.uniqueID, msg = this.msg, date =this.date, time=this.time, grpID =this.grpID,type= this.type,extra= this.extra,extn= this.extn,whoSendDept= this.whoSendDept, whoSendDesig=this.whoSendDesig, whoSendID=this.whoSendID)
}

@Serializable
data class uploadableEvent(var whoUploaded: classOrgMember = classOrgMember(), var eventID:String = "",
                           var title :String= "", var description:String="",
                           var Sdate:Long = 0, var Edate:Long = 0,
                           var docUri:MutableList<Pair<String,String>> = mutableListOf(), var imgUri:MutableList<String> = mutableListOf())

@Serializable
data class groupDetailObj(var timestamp:Long=0,var title :String= "", var description:String="", var whoUploaded: classOrgMember = classOrgMember(),
                          var groupID:String = "", var gpCoLeader : MutableMap<String,Boolean> = mutableMapOf() , var dateCreated:String="")

@Serializable
data class PairRipOff(val first:String="",val second:String="")

@Serializable
data class Notification(val notificationID:String="", val status:String=""/*TODO can be unread,accepted , declined */, val timestamp: Long=0, val idby:String="", val nameOrTitle:String="",
                        val extra:String= "", val type:String=""/*TODO can be NEvent, NGroup , NMsg , NGMsg */)

val notifn : Map<String , Pair<String,Int>> = mapOf(
    "NEvent" to Pair( "Event",R.drawable.calendar),"NMsg" to Pair( "Message",R.drawable.newmsg),
    "NGroup" to Pair( "Group Invite",R.drawable.invite), "NGMsg" to Pair( "Group Message",R.drawable.newmsg)
)

@Serializable
data class PairRipGroup(val first:String="",val second:groupDetailObj = groupDetailObj())

fun MessageToSend.toMessage():Message{
    return  Message(uniqueID = this.uniqueID , msg = this.msg , date = this.date , time = this.time , room =  this.room , type = this.type , extra = this.extra ,extn = this.extn)
}
fun Message.toMessageToSend():MessageToSend{
    return  MessageToSend(uniqueID = this.uniqueID , msg = this.msg , date = this.date , time = this.time , room =  this.room ,type = this.type , extra = this.extra , extn = this.extn)
}

fun ChatList.toClassOrgMember():classOrgMember{
    return classOrgMember(organName = this.orgName , this.memberID , this.name , this.email , this.contact , this.designation , this.dept)
}

fun classMemSignUp.toChatList(TS:Long):ChatList{
    return ChatList(TS , this.memberID ,this.profileDetail.organName ,this.profileDetail.name,this.profileDetail.email, this.profileDetail.contact ,this.designation , this.dept , path = "")
}


val folderIcons : Map<String , Int> = mapOf("Documents" to R.drawable.doc_folder ,"Images" to R.drawable.img_folder,"Videos" to R.drawable.vid_folder , "Audios" to R.drawable.aud_folder ,
    "pdf" to R.drawable.pdff,"ppt" to R.drawable.powerpoint, "doc" to R.drawable.docum,"apk" to R.drawable.apk,
    "csv" to R.drawable.csv, "xls" to R.drawable.xls , "html" to R.drawable.html)

val littleIcons : Map<String , Int> = mapOf(
    "pdf" to R.drawable.pdf,"ppt" to R.drawable.ppt, "doc" to R.drawable.doc,"apk" to R.drawable.apk_small,
    "csv" to R.drawable.csvsmall, "xls" to R.drawable.xlssmall ,"html" to R.drawable.html_small , "npe" to R.drawable.unrecognised_doc )

val documentMime : Map<String , Pair<String,String>> = mapOf(
    "pdf" to Pair( "application/pdf",".pdf"),"ppt" to Pair( "application/vnd.ms-powerpoint",".ppt"), "doc" to Pair( "application/msword",".doc"),"apk" to Pair( "",".apk"),
    "csv" to Pair( "text/csv",".csv"), "xls" to Pair( "application/vnd.ms-excel",".xls") ,"html" to Pair( "",".html") ,"webp" to Pair( "",".webp"), "npe" to Pair( "none","none"),
    "img" to Pair( "img/*",".png"))


val bottomBarIcons : Map<String , Int> = mapOf(
    "HOME" to R.drawable.home,"CHATS" to R.drawable.chatc, "EVENTS" to R.drawable.calendarc,"GROUPS" to R.drawable.groupc)

val eventIcons : Map<String , Int> = mapOf(
    "Visibility" to R.drawable.view,"GroupMembers" to R.drawable.groupc,"Title" to R.drawable.title,"Description" to R.drawable.personal, "Duration" to R.drawable.duration,
    "Attachments" to R.drawable.attachment)


val greyPeriList = listOf(Periwinkle,Color(0xfff4ebdb), )

val formIcons:Map<String , Int> = mapOf(
"Org ID" to R.drawable.id,"MemberID" to R.drawable.id,"Member ID" to R.drawable.id,"Password" to R.drawable.key,"Confirm Password" to R.drawable.key
    , "Org Name" to R.drawable.name,
"OTP" to R.drawable.otp,"Org Email" to R.drawable.email,"Mem Email" to R.drawable.email ,"Department" to R.drawable.dept , "Designation" to R.drawable.desig)

data class extrasForDP(val ID:String="",val  pathTo:String = "",val pathFrom:String="")
