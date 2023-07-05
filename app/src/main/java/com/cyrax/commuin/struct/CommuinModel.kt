package com.cyrax.commuin.struct

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrax.commuin.KtorRTimeMsgClient
import com.cyrax.commuin.Message2
import com.cyrax.commuin.RealtimeMessagingClient
import com.cyrax.commuin.Room
import com.cyrax.commuin.Utils.convert12HrTo24Hr
import com.cyrax.commuin.Utils.convertTimeStringToTimestamp
import com.cyrax.commuin.Utils.splitString
import com.cyrax.commuin.functions.downloadFromByteArray
import com.cyrax.commuin.functions.uriToArrPdf
import com.cyrax.commuin.functions.uritoArr
import com.cyrax.commuin.sections.getFileExtension
import com.cyrax.commuin.sections.getTime
import com.cyrax.commuin.sections.timeToID
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.ConnectException
import java.util.Stack

class CommuinModel :ViewModel(){
    lateinit var DB : db
    lateinit var context : Context
    var uiState : AppData by mutableStateOf( AppData.Default )
    fun updateUiState(query : AppData){
       uiState = query
    }

    var Curr = mutableStateOf(Screen.HOME)
        private  set
    fun setCurr(ind:Screen){
        Curr.value = ind
    }

    var dAndDObj : createMemState by mutableStateOf( createMemState.Default )
    fun resetTemp(a:createMemState){dAndDObj = a }
    fun checkOrg(orgID:String){
        CreatorRepository().checkOrg(orgID) { ele -> dAndDObj = ele } //This is lambda arg
    }

    var memberExist by mutableStateOf<String>( "" )
    suspend fun checkMember(memID:String){
        CreatorRepository().checkMember(memID,mem.value,{ ele -> memberExist = ele})
    }

    var org = MutableStateFlow( classOrgSignUp() )
    var genratedOTP = ""
    fun orgSignUpUpdate(query:String,posn:Int){
        //99 is a unique case when we want to create and account
        if(posn==99){
            if(query == genratedOTP) {
                CreatorRepository().addOrganisation(org.value)
                updateLoginState(Login.Success)
            }
            else{
                updateLoginState(Login.Error("Incorrect OTP"))
            }
        }
        //1,2,3 are case where we want to update orgID,orgName, orgEmail from the object that holds the state
        //Else resets the fields to default
        else{
            org.update {
                when(posn){
                    1->it.copy(orgID = query)
                    2->it.copy(orgName = query)
                    3->it.copy(email = query)
                    4->it.copy(password = query)
                    else -> it.copy(orgID="",orgName="",email = "",password = "")
                }

            }
        }

    }

    var mem = MutableStateFlow( classMemSignUp() )
        private set
    var passwordState:RequestStatus by mutableStateOf( RequestStatus.Default )
        private set
    fun memSignUpUpdate(query:String,posn:Int){
        //99 is a unique case when we want to create and account
        if(posn==99){
            CreatorRepository().addMember(mem.value)
//            viewModelScope.launch {
//                DB.image().insertImage(ImageEntity(mem.value.memberID+" "+"dp", UriToByteArray( Uri.parse("https://firebasestorage.googleapis.com/v0/b/commu-in-e20d8.appspot.com/o/placeholder.jpg?alt=media&token=3aff3d77-e734-41fb-9916-1ad9acbaf120"), context)))
//            }

        }
        //90 is when we want to match passwords entered by user
        else if(posn == 90){
            CreatorRepository().matchPassword(mem.value.password,query , { newState-> passwordState = newState})
        }
        //1,2,3 are case where we want to update orgID,orgName, orgEmail from the object that holds the state
        //Else resets the fields to default
        else{
            mem.update {
                when(posn){
                    1->it.copy(orgID = query)
                    2->it.copy(dept = query)
                    3->it.copy(designation = query)
                    4->it.copy(memberID = query)
                    5->it.copy(password = query)
                    else -> it.copy(orgID="",dept="",designation = "", memberID = "")
                }

            }
        }
    }

    var LoginState:Login by mutableStateOf( Login.Default )
    fun updateLoginState(state:Login){
        LoginState = state
    }
    var login = MutableStateFlow( ClassLoginForm() )
    fun loginFormUpdate(query:String,posn:Int  ){
        //99 is a unique case when we want to Login Member 100 for OrgLogin
        if(posn==99){  CreatorRepository().orgLogin(login.value,{it-> LoginState = it} , { uiState  = AppData.Organisation(it);Log.d(TAG,uiState.toString())} )  }
        else if(posn==98){ CreatorRepository().memberLogin(login.value,  {it-> LoginState = it} ,  {  uiState  =  AppData.Member(it) } , {myOrganisation.value = it}) }
        else{
            login.update {
                Log.d(TAG,"${login.value}")
                when(posn){
                    1->it.copy(orgID = query)
                    2->it.copy(memID = query)
                    3->it.copy(Password = query)
                    else -> {
                        LoginState = Login.Default
                        it.copy(orgID = "", Password = "", memID = "")

                    }
                }
            }
        }
    }

    suspend fun addDepartmentOrDesignation( orgID:String,option:String ,whereTo:String){
        if(whereTo == "dept") CreatorRepository().addDepartment(orgID,option) {
            (uiState as AppData.Organisation).appState.orgDepartments[it] = 1
            (dAndDObj as createMemState.Success).dandD.depts.add(it)
        }
        else CreatorRepository().addDesignation(orgID,option) {
            (uiState as AppData.Organisation).appState.designations[it] = 1
            (dAndDObj as createMemState.Success).dandD.designations.add(it)
        }
    }
    suspend fun delDepartmentOrDesignation( orgID:String,newMap: MutableMap<String,Int> ,whereTo:String){
        if(whereTo == "dept") CreatorRepository().delDepartment(orgID,newMap)// {
          //  (uiState as AppData.Organisation).appState.orgDepartments.add(it)
           // (dAndDObj as createMemState.Success).dandD.depts.add(it)
      //  }
        else CreatorRepository().delDesignation(orgID,newMap) //{
          //  (uiState as AppData.Organisation).appState.designations.add(it)
            // (dAndDObj as createMemState.Success).dandD.designations.add(it)
      //  }
    }

    var memberDetail = MutableStateFlow( classOrgMember() )
    fun addMemberToOrg(query:String,posn:Int){
        if(posn == 99 ){
            memberDetail.value.organName = (uiState as AppData.Organisation).appState.orgName
            CreatorRepository().addMemberInOrg( query,  memberDetail.value)
        } else{
            memberDetail.update {
                when(posn){
                    1->it.copy(memberID = query)
                    2->it.copy(name = query)
                    3->it.copy(email = query)
                    4->it.copy(contact = query)
                    5->it.copy(dept = query)
                    6->it.copy(designation = query)
                    else -> it.copy( memberID = "" , name="", email="",contact="",dept="" , designation = "" )
                }

            }
        }

    }
    var requestState:RequestStatus by mutableStateOf( RequestStatus.Default )
    //@OptIn(DelicateCoroutinesApi::class)
    suspend fun set(uri: Uri,path:String){

        ClassImageRep().addImageToStorage(uri,path )
    }
    fun resetAll(){
        resetTemp(createMemState.Default) ;
        orgSignUpUpdate("Resetting the Org object", 90)
        addMemberToOrg("Resetting the Org object", 90)
        memSignUpUpdate("Resetting the Mem object", 89)
        loginFormUpdate("Resetting the form", 90)
        updateLoginState(Login.Default)
        requestState = RequestStatus.Default
        updateUiState(AppData.Default)
    }


    var backup:MutableList<String> = mutableListOf()
    var myOrganisation  = MutableStateFlow( myOrg() )

}

class MemberModel :ViewModel(){
    var myData = MutableStateFlow(classMemSignUp())
    fun setMyData(obj:classMemSignUp){
        myData.value = obj
    }
    var myDataAsSender = ChatList()

    var Curr = mutableStateOf(Screen.HOME)
        private  set
    fun setCurr(ind:Screen){
        prevCurr = Curr.value
        Curr.value = ind
        if(prevCurr == Screen.HOME) { notiJob?.cancel() }
    }
    var prevCurr  = Screen.HOME

    var allAccountName = MutableStateFlow(mapOf<String,Int>())
    fun populateAccName(){
        memberRepository().populateMembers ({
            allAccountName.value = it; /*Log.d("TAG ","${it.toString()} LINE 255")*/
        } , myData.value.orgID)
    }

    var myOrganisation  = MutableStateFlow( myOrg() )

    var currentChatOpened:MutableStateFlow<classOrgMember?> = MutableStateFlow( null )
        private set
    fun setCurrChatOpened(obj : classOrgMember?){
        notiJob?.cancel()
        currentChatOpened.value = obj
        markAsRead(obj)
    }
    @SuppressLint("SuspiciousIndentation")
    fun markAsRead(obj : classOrgMember?){
        if(obj!=null)
        viewModelScope.launch(Dispatchers.IO) {
            memberRepository().otherNMsgToMeID(myData.value,obj!!.memberID)
        }
    }

    var messagesOnScreen = mutableStateListOf(Message())
    fun populateOnScreenMsg(context:Context){
        val DB = db.getDatabase(context)
        messagesOnScreen.clear()
        viewModelScope.launch (Dispatchers.IO){
            messagesOnScreen.addAll(DB.chat().populateMsg(myData.value.memberID+" "+currentChatOpened.value!!.memberID,currentChatOpened.value!!.memberID+" "+myData.value.memberID))
        }
    }

    var CurrChatJob :Job? = null
    fun collectMsgAndStore(context:Context){
        val DB = db.getDatabase(context)
        CurrChatJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("qwerty",CurrChatJob.toString())
               memberRepository().messageFlow(recipientID = currentChatOpened.value!!.memberID, sender = myData.value)
                   .collect{

                        if(it is List<*>){
                            (it as List<Message>).forEach {msg->
                                DB.chat().insertMsg(msg)
                                messagesOnScreen.add(msg)
                            }
                        }
                    }
            }
        }

    }

 //   val selectedMsgs = mutableStateMapOf<String,Boolean>()
   // val startSel = boolean

    fun sendMsg(msg:MessageToSend){
        memberRepository().sendMsg(msg = msg,recipientID = currentChatOpened.value!!.memberID, sender = myData.value)
    }

    var doesListNNeedToRecompose = 0  //I am using this variable to use in my composable for LaunchedEffect and whenever new data will be collect we will change its value so that LaunchedEffect can execute
    var peopleListOnScreen = mutableStateListOf(ChatList())
    var queriedPeopleListOnScreen = mutableStateListOf(ChatList())
    fun updateOnScreenPeople(){
        viewModelScope.launch (Dispatchers.IO){
            memberRepository().peopleListFlow(myData.value)
                .collect(){
                    if(it is MutableMap<*,*>){
                        peopleListOnScreen.clear()
                        doesListNNeedToRecompose = (doesListNNeedToRecompose+1)%5
                        it.forEach {pair ->
                            peopleListOnScreen.add(pair.value as ChatList)
                        }
                        peopleListOnScreen.forEach {
                            Log.d("LIST PRINTOUT", it.toString() )
                        }

                    }
                }

        }
    }
    fun addPersonToServerList(recipientDetail:ChatList){
        memberRepository().addPerson( sender  = myDataAsSender, recipientDetail =  recipientDetail , orgID = myData.value.orgID )
    }

    //Altough this function is named add image but it adds both img and docs which is sent through a message
    fun addImage(uri:Uri , context: Context ,  pathfrom:String = "" , pathTo:String="",ID : String = myData.value.memberID+" "+"dp" , pathForRTDB :String , filetype:String , extension:String , isGroup:Boolean=false){
      //  val DB = db.getDatabase(context)
        viewModelScope.launch {
            val newUri = if(filetype == "img") async {
                Log.d("qwerty","-------339 Comm Model-\n")
                downloadFromByteArray(context , pathTo ,pathfrom ,ID+".${extension}" , uritoArr(uri ,context), "--::--${filetype}" ,ID)
            }.await()
            else {
                async {
                    Log.d("qwerty","-------345-\n")
                    downloadFromByteArray(context , pathTo ,pathfrom ,ID+".${extension}" , uriToArrPdf(context,uri), "--::--${filetype}" ,ID)
                }.await()
            }
            if(newUri!=null){
                ClassImageRep().addImageToStorageSiMple(Uri.parse(newUri) , pathForRTDB )
                Log.d("qwerty","-------me up 341-\n")
                if((pathForRTDB.substring(pathForRTDB.length-2))!="dp"){
                    if(isGroup){
                        //TODO nothing currently
                    }
                    else{
                        val recipient = currentChatOpened.value!!
                        Log.d("qwerty","-------you up 344-\n")
                        when(filetype){
                            "doc"-> ClassImageRep().addImageToStorageSiMple(Uri.parse(newUri) , "${myData.value.orgID}/${recipient.memberID}/documents/$ID" )
                            "img"->ClassImageRep().addImageToStorageSiMple(Uri.parse(newUri) , "${myData.value.orgID}/${recipient.memberID}/images/$ID" )
                        }
                    }


                }
            }


        }
    }

    // var myDp  = MutableStateFlow<String?>(null)
     //fun resetDp(valu:String?){ myDp.value = valu }
    //This function fetches the profile image
     suspend fun getImage( context: Context , ID : String, pathfrom:String = "" , pathTo:String="",filetype:String , extension:String):String?{
         return withContext(Dispatchers.IO){
             var imgUri :String? = ""
             val DB = db.getDatabase(context)
             val uri = async{ DB.image().obtainImage(ID) }.await()?.uri
             if(uri != null && doesExists(context,uri)){
                 imgUri = uri  }
             else{
                 imgUri = async {
                         downloadFromByteArray(context , pathTo ,pathfrom ,ID+".${extension}" , null , "--::--${filetype}" ,ID)
                     }.await()
             }

             imgUri
         }

    }

    suspend fun refreshImage( context: Context , ID : String, pathfrom:String = "" , pathTo:String="",filetype:String , extension:String):String?{
        return withContext(Dispatchers.IO){
            var imgUri :String? = ""
            imgUri = async {
                    downloadFromByteArray(context , pathTo ,pathfrom ,ID+".${extension}" , null , "--::--${filetype}" ,ID)
            }.await()
            imgUri
        }
    }

    //Currently this function is only being used to delete profile pic form Firestorage and Phone db
    fun deleteImageFormCloud(context:Context,path:String = "${myData.value.orgID}/${myData.value.memberID}/dp"){
        ClassImageRep().deleteImage(path)
        viewModelScope.launch{
            val DB = db.getDatabase(context)
            DB.image().deleteImage("${myData.value.memberID} dp")
        }

    }


    //This function fetches the other images and doc although it is just named imageeach
    fun getImageEach( context: Context , ID : String = myData.value.memberID+" "+"dp" , pathfrom:String = "" , pathTo:String="" , filetype:String , extension:String) :StateFlow<String?>{
        val state= MutableStateFlow<String?>(null)
        viewModelScope.launch{
            val DB = db.getDatabase(context)
            val uri = if(filetype == "img") async{DB.image().obtainImage(ID)?.uri}.await()
                        else if(filetype == "doc") async { DB.doc().obtainDoc(ID)?.uri}.await() else null
            if(uri != null){
                when(filetype){
                    "img" -> {
                        if(doesExists(context , uri)){
                            state.value = uri
                        }
                        else{
                            DB.image().deleteImage(ID)
                            Log.d("qwerty","Non existant : $uri , $ID+.${extension}")
                            state.value = null
                        }
                    }
                    "doc" -> {
                        if(doesExists(context , uri)){
                            state.value = uri
                        }
                        else{
                            DB.doc().deleteDoc(ID)
                            Log.d("qwerty","Non existant : $uri , $ID+.${extension}")
                            state.value = null
                        }
                    }
                }

                // var byteArray = uritoArr(Uri.parse(uri),context)
            }
            else{
                state.value = async {
                    downloadFromByteArray(context , pathTo ,pathfrom ,ID+".${extension}" , null , "--::--${filetype}" ,ID)
                }.await()
            }
        }
        return state
    }

    //Stack that manages navigation for file tab
    var stk: Stack<Pair<String, String>> = Stack<Pair<String,String>>()
    var topBarQuery = MutableStateFlow("")
    fun setTBQuery(query:String){ topBarQuery.value = query }

    //Group form
    var groupform = MutableStateFlow( groupDetailObj() )
    @SuppressLint("SuspiciousIndentation")
    fun updateGform(query:String, posn:Int){
        if(posn == 99 ) {//99 means we want to create a group
            val grpID = "GRPid_${ timeToID(System.currentTimeMillis()) }"
            groupform.value.whoUploaded = myData.value.profileDetail
            groupform.value.groupID = grpID
            groupform.value.timestamp = getTime().TS
            groupform.value.dateCreated = getTime().date
            viewModelScope.launch(Dispatchers.Default){
                memberRepository().createGroup(groupform.value,myData.value.orgID)
                acceptGroup(grpID)
            }
            viewModelScope.launch(Dispatchers.Default){
                val exceptionsToInsert = mutableMapOf<String,Boolean>()
                visibleTo.forEach { t, valueLvl ->
                    when(valueLvl){
                        "Department" ->{
                            myOrganisation.value.members[t]!!.forEach { t, u ->
                                u.forEach { t, u ->
                                    if(t != myData.value.memberID){
                                        exceptionsToInsert[t] = true
                                        memberRepository().pushNotification(Notification("","Unread",getTime().TS,"${myData.value.memberID} ${myData.value.dept} ${myData.value.designation}",groupform.value.title,grpID,"NGroup"),"${myData.value.orgID}/$t/Notifications")

                                    }
                                }
                            }
                        }
                        "Designation" ->{
                            myOrganisation.value.members[splitString(t).first]!![splitString(t).second]!!.forEach { t, u ->
                                if(!exceptionsToInsert.containsKey(t) && t != myData.value.memberID){
                                    memberRepository().pushNotification(Notification("","Unread",getTime().TS,"${myData.value.memberID} ${myData.value.dept} ${myData.value.designation}",groupform.value.title,grpID,"NGroup"),"${myData.value.orgID}/$t/Notifications")
                                }
                            }
                        }
                        "Member" ->{
                            if(!exceptionsToInsert.containsKey(t) && t != myData.value.memberID){
                                    memberRepository().pushNotification(Notification("","Unread",getTime().TS,"${myData.value.memberID} ${myData.value.dept} ${myData.value.designation}",groupform.value.title,grpID,"NGroup"),"${myData.value.orgID}/$t/Notifications")
                            }
                        }
                    }

                }
                groupform = MutableStateFlow( groupDetailObj() )
            }
        }
        else
        groupform.update {
            when(posn){
                1 -> it.copy(title= query)
                2 -> it.copy(description = query)
                else -> it.copy(title= "", description = "")
            }
        }
    }
    var notificationList = mutableStateListOf<Notification>()
    var refresh = mutableStateOf<Boolean>(true)
    var notiJob :Job? = null
    @SuppressLint("SuspiciousIndentation")
    fun getNotifn(){
        refresh.value = true
        if(notiJob?.isActive != true){
            notiJob = viewModelScope.launch(Dispatchers.Default) {
                memberRepository().notificationFlow(myData.value).collect{
                    if(it){
                        val temp = memberRepository().SidebarNotificationsFetch(myData.value).reversed()
                        notificationList.clear()
                        notificationList.addAll(temp)
                        refresh.value = false
                    }
                }
            }
        }

    }
//    suspend fun getSingleNotifiation(notificationID:String,recipient : classMemSignUp):Notification{
//       return withContext(Dispatchers.Default) { memberRepository().getSingleNotifiation(notificationID, recipient) }
//    }

    fun deleteNotification(notiID:String){
        memberRepository().deleteSideBarNot( notiID,myData.value )
    }
    fun sendNotification(noti:Notification,path:String){
        memberRepository().pushNotification(noti,path)
    }
    fun sendNotification(noti:Notification,path:String,recipient:classOrgMember ,senderID:classMemSignUp){
        viewModelScope.launch(Dispatchers.IO) {
            memberRepository().pushNotification(noti,path,recipient, senderID)
        }

    }
    fun changeNotificationStat(notiID:String,stat:String){
        memberRepository().changeNotificationStat( notiID,myData.value,stat )
    }
    fun acceptGroup(grpID:String){
        memberRepository().addStringToRTDB( grpID,  "${myData.value.orgID}/${myData.value.memberID}/Groups/$grpID"  )
    }
    suspend fun getGroup(grpID:String):groupDetailObj{
        return memberRepository().getGroup(grpID,myData.value.orgID)!!
    }
    fun getPerson(triString:String):classOrgMember{
        Log.d("qwerty",triString+"at line 521 commmodel")
        if(triString.isBlank())return classOrgMember()
        val firstBlank = triString.indexOf(" ");
        val id = triString.substring(0,firstBlank)
        val secondBlank = triString.indexOf(" ",firstBlank+1);
        val dept = triString.substring(firstBlank+1,secondBlank)
        val desig = triString.substring( secondBlank+1 )
       return myOrganisation.value.members[dept]!![desig]!![id]!!
    }



    //EVENT FOrm
    var event = MutableStateFlow( uploadableEvent() )
    var docUri = mutableStateListOf<String>()
    var imgUri = mutableStateListOf<String>()
        fun setEventLiterals(query:Any,posn:Int){
            event.update {
                when (posn) {
                    0 -> it.copy(eventID = query as String)
                    1 -> it.copy(title = query as String)
                    2 -> it.copy(description = query as String)
                    3 -> it.copy(Sdate = query as Long)
                    4 -> it.copy(Edate = query as Long)
                    else -> it.copy(eventID = "", title = "" , description = "" , Sdate = 0 , Edate = 0)
                }
            }
        }
    var Sdate  = MutableStateFlow( Dateobj() )
    var Edate  = MutableStateFlow( Dateobj() )
        fun setDate(query:String,posn:Int,S:Boolean){
            val temp = if(S)Sdate else Edate
             temp.update {
                when(posn){
                    1 -> it.copy(DD = query)
                    2->it.copy(MM = query)
                    3->it.copy(YYYY = query)
                    4->it.copy(HH = query)
                    5->it.copy(MN = query)
                    6->it.copy(ap = query)
                    else -> it.copy(DD="",MM="",YYYY="",HH="",MN="",ap="")
                }

            }

        }
    var visibleTo = mutableStateMapOf<String,String>()
        fun updateVisibleTo(K:String,V:String? = null){
            if(V != null)visibleTo[K] = V
            else{
                visibleTo.remove(K)
            }
        }
    var visibilityStk :Stack<Pair<String,String>> = Stack<Pair<String,String>>()

    fun resetEventForm(){
        setDate("Resetting the Sdates",100,true)
        setDate("Resetting the Edates",100,false)
        visibilityStk = Stack<Pair<String,String>>()
        visibleTo = mutableStateMapOf()
        docUri = mutableStateListOf()
        imgUri = mutableStateListOf()
        event = MutableStateFlow( uploadableEvent() )
    }

    fun uploadEvent(){
        Log.d("qwerty","S Entered : ${Sdate.value.DD}.${Sdate.value.MM}.${Sdate.value.YYYY} ${Sdate.value.HH}:${Sdate.value.MN}")
        Log.d("qwerty","E Entered :${Edate.value.DD}.${Edate.value.MM}.${Edate.value.YYYY} ${Edate.value.HH}:${Edate.value.MN}")
        event.value.Sdate = if(((Sdate.value.ap == "a" || Sdate.value.ap == "A") && Sdate.value.HH != "12") || ((Sdate.value.ap == "p" || Sdate.value.ap == "P") && Sdate.value.HH == "12") )convertTimeStringToTimestamp("${Sdate.value.DD}.${Sdate.value.MM}.${Sdate.value.YYYY} ${Sdate.value.HH}:${Sdate.value.MN}")
        else convertTimeStringToTimestamp("${Sdate.value.DD}.${Sdate.value.MM}.${Sdate.value.YYYY} ${convert12HrTo24Hr(Sdate.value.HH)}:${Sdate.value.MN}")
        Log.d("qwerty","Start Time as time Stamp ${event.value.Sdate}")
        event.value.Edate = if(((Edate.value.ap == "a" || Edate.value.ap == "A") && Edate.value.HH != "12") || ((Edate.value.ap == "p" || Edate.value.ap == "P") && Edate.value.HH == "12"))convertTimeStringToTimestamp("${Edate.value.DD}.${Edate.value.MM}.${Edate.value.YYYY} ${Edate.value.HH}:${Edate.value.MN}")
        else convertTimeStringToTimestamp("${Edate.value.DD}.${Edate.value.MM}.${Edate.value.YYYY} ${convert12HrTo24Hr(Edate.value.HH)}:${Edate.value.MN}")
        Log.d("qwerty","End Time as time Stamp ${event.value.Edate}")
        viewModelScope.launch(Dispatchers.IO) {
            //Adding all the documents to storage
            val tempEventID:String = "EVENTiD_${ timeToID(System.currentTimeMillis()) }"
            event.value.eventID = tempEventID   //Inserting my genrated eventing to the obj that I will store in RTDB
            docUri.forEach {
                val tempID = "COMM_DOC_${ timeToID(System.currentTimeMillis()) }"
                event.value.docUri.add(Pair(tempID, getFileExtension(it)!!))
               ClassImageRep().addImageToStorageSiMple(Uri.parse(it) , "${myData.value.orgID}/Events/docsAndImg/${tempEventID}/${tempID}" )
            }
            imgUri.forEach {
                val tempID = "COMM_IMG_${ timeToID(System.currentTimeMillis()) }"
                event.value.imgUri.add(tempID)
                ClassImageRep().addImageToStorageSiMple(Uri.parse(it) , "${myData.value.orgID}/Events/docsAndImg/${tempEventID}/${tempID}" )
            }

            event.value.whoUploaded = myData.value.profileDetail
            memberRepository().addDataToRTDB(event.value , "${myData.value.orgID}/Events/${tempEventID}")
            val exceptionsToInsert = mutableMapOf<String,Boolean>()
            visibleTo.forEach { t, valueLvl ->
                when(valueLvl){
                    "Department" ->{
                        myOrganisation.value.members[t]!!.forEach { t, u ->
                            u.forEach { t, u ->

                                exceptionsToInsert[t] = true
                                memberRepository().pushDataToRTDB(tempEventID , "${myData.value.orgID}/$t/Events")
                                if(t != myData.value.memberID){
                                    sendNotification(Notification("","Unread",getTime().TS,"${myData.value.memberID} ${myData.value.dept} ${myData.value.designation}",event.value.title,tempEventID,"NEvent"),"${myData.value.orgID}/$t/Notifications")
                                }

                            }
                        }
                    }
                    "Designation" ->{
                        myOrganisation.value.members[splitString(t).first]!![splitString(t).second]!!.forEach { t, u ->
                            if(!exceptionsToInsert.containsKey(t)){
                                memberRepository().pushDataToRTDB(tempEventID , "${myData.value.orgID}/$t/Events")
                                if(t != myData.value.memberID){
                                    sendNotification(Notification("","Unread",getTime().TS,"${myData.value.memberID} ${myData.value.dept} ${myData.value.designation}",event.value.title,tempEventID,"NEvent"),"${myData.value.orgID}/$t/Notifications")
                                }
                            }
                        }

                    }
                    "Member" ->{
                        if(!exceptionsToInsert.containsKey(t)){
                            memberRepository().pushDataToRTDB(tempEventID , "${myData.value.orgID}/$t/Events")
                            if(t != myData.value.memberID){
                                sendNotification(Notification("","Unread",getTime().TS,"${myData.value.memberID} ${myData.value.dept} ${myData.value.designation}",event.value.title,tempEventID,"NEvent"),"${myData.value.orgID}/$t/Notifications")
                            }
                        }
                    }
                }

            }
            resetEventForm()
        }
    }
    var currentEventOpened = MutableStateFlow( uploadableEvent() )
        fun setCurrEvent(a:uploadableEvent){
            currentEventOpened.value = a
        }
    var eRefresh =  mutableStateOf(true)
    var eventsList = mutableStateListOf<uploadableEvent>()
    fun getEvents(){
        eRefresh.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val a = memberRepository().getEvent("${myData.value.orgID}/${myData.value.memberID}" , myData.value.orgID)
            eventsList.clear()
            eventsList.addAll(a)
            eRefresh.value = false
        }
    }

    suspend fun getSingleEvent(eventID:String):uploadableEvent{
        return withContext(Dispatchers.Default){
            memberRepository().getSingleEvent(eventID,myData.value.orgID)
        }
    }


    fun deleteEvent(eventID:String){
        viewModelScope.launch {
            memberRepository().deleteEventCompletely(eventID,myData.value.orgID)
        }

    }

    suspend fun checkAndFetch(context:Context , fileID :String = "COMM_DOC_59561684843625099" , fileType:String = "Document"/*Is it document or image*/):String?{
        var uriStr :String? = null
        val DB = db.getDatabase(context)
        Log.d("qwerty",fileID+" At line 649 model")
        when(fileType){
            "Document"->{
                uriStr = withContext(Dispatchers.Default) {
                    DB.doc().obtainDoc(fileID)
                }?.uri
                if(uriStr != null){       //If the uri fom db is NOT NULL then  we are checking that the file actually exists on that location. If it does then we do nothing else we clean up the miscellenious data from DB and reset uriStr  to null
                    if(!doesExists(context,uriStr)){
                        DB.doc().deleteDoc(fileID)
                        uriStr = null
                    }
                }
            }
            "Image"->{
                uriStr = withContext(Dispatchers.Default) {
                    DB.image().obtainImage(fileID)
                }?.uri
                if(uriStr != null){       //If the uri fom db is NOT NULL then  we are checking that the file actually exists on that location. If it does then we do nothing else we clean up the miscellenious data from DB and reset uriStr  to null
                    if(!doesExists(context,uriStr)){
                        DB.image().deleteImage(fileID)
                        uriStr = null
                    }
                }

            }
        }
        Log.d("qwerty",uriStr?:"NULL at line 574 CommuinModel")
        return uriStr
    }
    suspend fun downloadAndReturnUri(context: Context,pathTo: String,pathfrom: String,ID: String,extension: String,filetype: String):StateFlow<String?>{
        val uriStr = MutableStateFlow<String?>(null)

       viewModelScope.launch {
           uriStr.value = async {
               downloadFromByteArray(context , pathTo ,pathfrom ,ID+".${extension}" , null , "--::--${filetype}" ,ID)
           }.await()
       }
        return uriStr
    }

    fun doesExists(context: Context,uriStr: String):Boolean{
        val uri = Uri.parse(uriStr)
        try{
            val a = context.contentResolver.openInputStream(uri)
            a?.close()
        }catch(_:Exception){
            return false
        }
        Log.d("qwerty","$uriStr exits")
        return true
    }

    fun share(context:Context , uriStr:String, filetype:String){

        fun openFileFromByteArray(context: Context, byteArray:ByteArray, id :String  , suffix:String):Uri? {
            val tempFile = File.createTempFile(id, suffix, context.cacheDir)
            tempFile.outputStream().use { outputStream ->
                outputStream.write(byteArray)
            }

            return FileProvider.getUriForFile(context, "com.cyrax.commuin.fileprovider", tempFile)
        }
        var intent = Intent(Intent.ACTION_SEND).apply {
            this.putExtra(Intent.EXTRA_STREAM,openFileFromByteArray(context, uriToArrPdf(context,Uri.parse(uriStr))!!, "COMM" ,
                documentMime[filetype]!!.second)
            )
            this.type = documentMime[filetype]!!.first
        }

        context.startActivity(Intent.createChooser(
            intent,"Share via :"
        ))


    }

    var gRefresh =  mutableStateOf(true)
    var myGroups = mutableStateListOf<groupDetailObj>()
        private set
    suspend fun getMyGroups(){
        gRefresh.value = true
        viewModelScope.launch{
            val temp = memberRepository().getMyGroups(myData.value).values.reversed()
            myGroups.clear()
            myGroups.addAll(temp)
            gRefresh.value = false
        }

    }
    var currentGrpChatOpened:MutableStateFlow<groupDetailObj?> = MutableStateFlow( null )
        private set
    fun setCurrGrpChatOpened(obj : groupDetailObj?){
        currentGrpChatOpened.value = obj
    }

    var grpMessagesOnScreen = mutableStateListOf<MessageGrp>()
    var lastID :String  = "" // this variable is an indicatior that tells till where the messages have been taken
    fun populateOnScreenMsgGrp(context:Context){
        val DB = db.getDatabase(context)
        grpMessagesOnScreen.clear()
        viewModelScope.launch (Dispatchers.IO){
            Log.d("qwerty","Populating(latestID is : $lastID) at line 733")
            grpMessagesOnScreen.addAll(DB.groupChat().populateMsg(currentGrpChatOpened.value!!.groupID))
            lastID = try{grpMessagesOnScreen[grpMessagesOnScreen.size -1 ].uniqueID}catch(_:Exception){""}
            Log.d("qwerty","After Populating(latestID is : $lastID) at line 733")
        }
    }

    fun collectMsgAndStoreGrp(context:Context){
        val DB = db.getDatabase(context)
        CurrChatJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("qwerty",CurrChatJob.toString())
                memberRepository().messageFlowGroup(grpID = currentGrpChatOpened.value!!.groupID, sender = myData.value , lastID)
                    .collect{
                        if(it is List<*>){
                            (it as List<MessageGrp>).forEach {msg->
                                DB.groupChat().insertMsg(msg)
                                grpMessagesOnScreen.add(msg)
                            }
                        }
                    }
            }
        }

    }

    fun sendMsgGrp(msg:MessageToSendGP){
       memberRepository().sendMsgGrp(currentGrpChatOpened.value!!.groupID,msg,myData.value.orgID)
    }

    var eventNotification = uploadableEvent()



    fun resetAll(){
        eventNotification = uploadableEvent()
        setMyData(classMemSignUp())
        myDataAsSender = ChatList()
        setCurr(Screen.HOME)
        notiJob = null
        allAccountName = MutableStateFlow(mapOf<String,Int>())
        myOrganisation  = MutableStateFlow( myOrg() )
        setCurrChatOpened( null )
        CurrChatJob = null
        peopleListOnScreen = mutableStateListOf(ChatList())
        queriedPeopleListOnScreen = mutableStateListOf(ChatList())
        stk  = Stack<Pair<String,String>>()
        setTBQuery("")
        groupform = MutableStateFlow( groupDetailObj() )
        notificationList = mutableStateListOf<Notification>()
        refresh = mutableStateOf<Boolean>(true)
        event = MutableStateFlow( uploadableEvent() )
        resetEventForm()
        setCurrEvent(uploadableEvent())
        eRefresh =  mutableStateOf(true)
        eventsList = mutableStateListOf<uploadableEvent>()
        gRefresh =  mutableStateOf(true)
        myGroups = mutableStateListOf<groupDetailObj>()
        setCurrGrpChatOpened(null)
        grpMessagesOnScreen.clear()
        lastID = ""
    }



}




class OrgModel :ViewModel(){

    var Curr = mutableStateOf(Screen.HOME)
        private  set
    fun setCurr(ind:Screen){
        Curr.value = ind
    }
}


class LiveMsgModel:ViewModel(){
    var client: KtorRTimeMsgClient
    init{
        val clien = HttpClient(CIO){
            install(Logging)
            install(WebSockets)
        }

        client = KtorRTimeMsgClient(clien)
    }


    val state = client
        .getMessages()
        .onStart { Connecting.value = true }
        .onEach { Connecting.value = false }
        .catch {
            Log.d("httpreq",it.message?:"s")
            connError.value = it is ConnectException
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Room())


    val Connecting = MutableStateFlow(false)
    val connError = MutableStateFlow(false)

    fun sendMessage(msg: Message2){
        viewModelScope.launch {
            client.send(msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            client.close()
        }
    }


}