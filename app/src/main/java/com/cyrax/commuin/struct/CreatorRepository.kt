package com.cyrax.commuin.struct

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ActivityScenario.launch
import com.cyrax.commuin.sections.getTime
import com.cyrax.commuin.sections.mapToArray
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


//Interface that holds the stats when a member user organisationID
sealed interface createMemState{
    object Loading:createMemState //Default
    data class Error(val error:kotlin.Exception):createMemState
    object Default:createMemState
    data class Success(val dandD:dAndD=dAndD()):createMemState
}

sealed interface RequestStatus{
    object Default:RequestStatus
    object Loading:RequestStatus
    data class Success(val link:String):RequestStatus //Default
    data class Error(val error:String):RequestStatus
}

sealed interface AppData{
    data class Organisation(var appState :classOrgSignUp = classOrgSignUp() ):AppData
    data class Member(var appState :classMemSignUp = classMemSignUp() ) : AppData
    object Default:AppData
}

sealed interface Login{
    object Success: Login
    object Loading : Login
    data class Error(val error:String = "ID or Password Incorrect"):Login
    object Default:Login
}


class CreatorRepository{
    fun checkOrg(orgID:String,callback:(createMemState)->Unit){
        if(orgID.isNotBlank()) {
            Firebase.firestore.collection(orgID).limit(1).get().addOnSuccessListener{
                if(it.size()==0) { callback( createMemState.Error(Exception("OrgID Doesn't Exist"))) }
                else{
                    Firebase.firestore.collection(orgID).document(orgID).get().addOnSuccessListener { snapshot->
                        var obj = snapshot.toObject(classOrgSignUp::class.java)
                        var deptList = mapToArray(obj?.orgDepartments?: mutableMapOf())
                        var desigList = mapToArray(obj?.designations?: mutableMapOf())
                        callback(
                            createMemState.Success(
                               dAndD(depts= deptList,designations = desigList )
                            )
                        )
                    }
                }
            }
        }
        else  callback(createMemState.Error(Exception("OrgID cannot be left empty !")))
    }

    suspend fun checkMember(memID:String, mem : classMemSignUp, callback:(String)->Unit){
        val temp = Firebase.firestore.collection(mem.orgID).document(mem.orgID).get().await()
        try{
           val temp2 =  temp.toObject(classOrgSignUp::class.java)?.deptMember?.get(mem.dept)?.get(mem.designation)?.get(mem.memberID)
            if(temp2 != null){
                if(checkIfAlreadyMemAccountCreated(mem.orgID,mem.memberID)) callback("Already")
                    else  callback("${temp2.email}")
            }
            else callback("Member Doesn't Exists")
         //  Log.d("TAG",obj.toString())
        }catch (_:Error){ }
    }

    suspend fun checkIfAlreadyMemAccountCreated(orgID:String,memID:String):Boolean{
        val exists = Firebase.firestore.collection(orgID).document(memID).get().await()
        if(exists.data != null) {
            Log.d("qwerty","true : ${exists.data}")
            return true
        }
        return false
    }

    fun addMember(mem : classMemSignUp){
        val dbref = Firebase.firestore
        dbref.collection(mem.orgID).document(mem.orgID).get().addOnSuccessListener {
            val obj=it.toObject(classOrgSignUp::class.java)?.deptMember?.get(mem.dept)?.get(mem.designation)?.get(mem.memberID)!!
            mem.profileDetail = obj
           // callback(obj)  //Extracting member detail from organisation and setting its data into members obj and adding to DBASE
            dbref.collection(mem.orgID).document(mem.memberID).set(mem)
        }
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child("${mem.orgID}/${mem.memberID}/dp").setValue("https://firebasestorage.googleapis.com/v0/b/commu-in-e20d8.appspot.com/o/placeholder.jpg?alt=media&token=3aff3d77-e734-41fb-9916-1ad9acbaf120")

    }

    fun addOrganisation(org : classOrgSignUp){
        Firebase.firestore.collection(org.orgID).document(org.orgID).set(org)
    }

    suspend fun addDepartment(orgID:String , query:String , callBack:(String)->Unit) {
        val docRef = Firebase.firestore.collection(orgID).document(orgID)
        val snapshot = docRef.get().await()
        val obj = snapshot.toObject(classOrgSignUp::class.java)?.orgDepartments!!
        obj.set(query,1)
        docRef.update("orgDepartments",obj).await()
        callBack(query)
    }
    suspend fun addDesignation(orgID:String , query: String, callBack: (String) -> Unit) {
        val docRef = Firebase.firestore.collection(orgID).document(orgID)
        val snapshot = docRef.get().await()
        val obj = snapshot.toObject(classOrgSignUp::class.java)?.designations!!
        obj.set(query,1)
        docRef.update("designations", obj).await()
        callBack(query)
    }
    suspend fun delDepartment(orgID:String , newMap: MutableMap<String,Int>) {
        val docRef = Firebase.firestore.collection(orgID).document(orgID)
        docRef.update("orgDepartments",newMap).await()
    }
    suspend fun delDesignation(orgID:String , newMap: MutableMap<String,Int>) {
        val docRef = Firebase.firestore.collection(orgID).document(orgID)
        docRef.update("designations", newMap).await()
    }

    fun addMemberInOrg(orgID:String , memberDetail:classOrgMember){
        val orgRef = Firebase.firestore.collection(orgID).document(orgID)
        orgRef.get().addOnSuccessListener{
            val temp1 = it.toObject(classOrgSignUp::class.java)?.deptMember

            if( temp1?.get(memberDetail.dept) != null ){
                if(temp1[memberDetail.dept]!![memberDetail.designation] !=null ) {
                    val temp2 = temp1[memberDetail.dept]!![memberDetail.designation]!!  //Extracting the designation map locally as we need it to update the DBASE as we can only update a map in firestore if it contains a respective key else we locally take the map modify it and reupdate it to the firestore
                    temp2[memberDetail.memberID] = memberDetail  //Adding new member
                    orgRef.update("deptMember.${memberDetail.dept}.${memberDetail.designation}", temp2 )
                }
                else{
                    val temp2 = temp1[memberDetail.dept]!!
                    temp2[memberDetail.designation] = mutableMapOf(memberDetail.memberID to memberDetail) //Adding new member
                    orgRef.update("deptMember.${memberDetail.dept}", temp2 )
                }
            }
            else{
                temp1!![memberDetail.dept] = mutableMapOf(memberDetail.designation to mutableMapOf(memberDetail.memberID to memberDetail))
                orgRef.update("deptMember" , temp1)
            }

        }
    }

    fun matchPassword(ps1:String , ps2:String , callback: (RequestStatus) -> Unit){
        if(ps1 == ps2) callback(RequestStatus.Success(""))
        else callback(RequestStatus.Error("UnMatch"))
    }

    fun orgLogin(mem : ClassLoginForm , callBack:(Login)->Unit , setData :(classOrgSignUp)->Unit){
        if(mem.orgID.isBlank() || mem.Password.isBlank()) callBack(Login.Error("Empty Fields"))
        else {
            val dbref = Firebase.firestore.collection(mem.orgID)
            dbref.limit(1).get().addOnSuccessListener{
                Log.d(TAG,it.toString())
                if(it.size() == 0){
                    callBack(Login.Error("Incorrect Credentials !"))
                }
                else{
                    dbref.document(mem.orgID).get().addOnSuccessListener {
                        val obj = it.toObject(classOrgSignUp::class.java)!!
                        if(mem.Password  == obj.password) {
                            callBack(Login.Success)
                            setData(obj)
                        }
                        else callBack(Login.Error("Incorrect Credentials !"))
                    }
                }

            }
        }

    }
    fun memberLogin(mem : ClassLoginForm , callBack:(Login)->Unit, setData :(classMemSignUp)->Unit , feedMyorg:(myOrg)->Unit ){
        Log.d(TAG,"Again CAllED")
        if(mem.orgID.isBlank() || mem.Password.isBlank() || mem.memID.isBlank() ) callBack(Login.Error("Empty Fields"))
        else {
            val dbref = Firebase.firestore.collection(mem.orgID)
            dbref.get().addOnSuccessListener{arr->

                if(arr.size() == 0){
                    callBack(Login.Error("Incorrect OrgID !"))
                }
                else{
                    dbref.document(mem.orgID).get().addOnSuccessListener {
                        val objorg = it.toObject(classOrgSignUp::class.java)!!
                        dbref.document(mem.memID).get().addOnCompleteListener {
                            if(it.isSuccessful){
                                val obj = it.result.toObject(classMemSignUp::class.java)!!
                                if(mem.Password  == obj.password) {
                                    feedMyorg(myOrg(objorg.deptMember))
                                    callBack(Login.Success)
                                    setData(obj)
                                }
                                else callBack(Login.Error("Incorrect Credentials !"))
                            }else callBack(Login.Error("Incorrect Credentials !"))

                        }
                    }.addOnFailureListener { callBack(Login.Error("Some Error Occured")) }


                }

            }
        }



    }

}


class ClassImageRep(){

    suspend fun addImageToStorageSiMple(Uri: Uri , path: String){
        Log.d("qwerty","-------uploadin-\n")
        val strRef : StorageReference = FirebaseStorage.getInstance().reference
        val a = strRef.child(path).putFile(Uri).await()
            .storage.downloadUrl.await()
    }

    suspend fun addImageToStorage(imageUri: Uri , path: String){
        val strRef : StorageReference = FirebaseStorage.getInstance().reference
        val a = strRef.child(path).putFile(imageUri).await()
            .storage.downloadUrl.await()
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(path).setValue(a.toString()).await()
    }
    fun deleteImage(path:String){
        val strRef : StorageReference = FirebaseStorage.getInstance().reference
        strRef.child(path).delete()
    }


}


class memberRepository{
    fun populateMembers(callBack: (Map<String,Int>) -> Unit , orgID:String = "001"){
        val map = mutableMapOf<String,Int>()
        val dbref = Firebase.firestore.collection(orgID)
        dbref.get().addOnSuccessListener{
            for(ele in it){
                map[ele.id] = 1
            }
            callBack(map)
        }
    }

    fun get( recipientID : String , sender : classMemSignUp){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        //   mDbRef.child("chats").push().setValue(Message(uniqueID = "0", msg = "Hey there")).await()
        val userRef = mDbRef.child(sender.orgID).child(sender.memberID).child("Chats").child(recipientID)
       userRef.limitToLast(1).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {  }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    fun messageFlow( recipientID : String , sender : classMemSignUp):Flow<Any>{
        return callbackFlow {
            val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
         //   mDbRef.child("chats").push().setValue(Message(uniqueID = "0", msg = "Hey there")).await()
            val userRef = mDbRef.child(sender.orgID).child(sender.memberID).child("Chats/Conv").child(sender.memberID+" "+recipientID)
             val listener =  userRef.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // messageList.clear()
                        var message  = MessageToSend()
                        val list = mutableListOf<Message>()
                        for (randomID in snapshot.children) {
                            message = randomID.getValue(MessageToSend::class.java)!!
                            list.add(message.toMessage())
                        }
                        GlobalScope.launch(Dispatchers.IO) {
                            userRef.removeValue()
                            trySend(list)
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        GlobalScope.launch(Dispatchers.IO) {
                            trySend(error.message)
                        }}
                })
            awaitClose { userRef.removeEventListener(listener) }
        }
    }

    fun sendMsg(msg:MessageToSend , recipientID : String , sender : classMemSignUp){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(sender.orgID).child(sender.memberID).child("Chats/Conv").child(sender.memberID+" "+recipientID).push().setValue(msg)
     if(sender.memberID != recipientID)
        mDbRef.child(sender.orgID).child(recipientID).child("Chats/Conv").child(recipientID+" "+sender.memberID).push().setValue(msg)
    }


    fun peopleListFlow( sender : classMemSignUp):Flow<Any>{
        return callbackFlow {
            val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
            val userRef = mDbRef.child(sender.orgID).child(sender.memberID).child("Chats").child("list") //list means the collection of people I have chatted with
            val listener =  userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listOfPeople:MutableMap<Long,ChatList> = mutableMapOf()
                    for(eachPeople in snapshot.children){
                        val tempObj = eachPeople.getValue(ChatList::class.java)!!
                        listOfPeople[tempObj.timestamp] = tempObj  //Inserting each person that msged us in a list
                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        trySend(listOfPeople.toSortedMap(compareByDescending { it }))
                    }


                }
                override fun onCancelled(error: DatabaseError) {
                    GlobalScope.launch(Dispatchers.IO) {
                        trySend(error.message)
                    }}
            })
            awaitClose { userRef.removeEventListener(listener) }
        }
    }

    fun addPerson(recipientDetail:ChatList  , sender : ChatList , orgID: String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(orgID).child(sender.memberID).child("Chats").child("list/${recipientDetail.memberID}").setValue(recipientDetail)
        mDbRef.child(orgID).child(recipientDetail.memberID).child("Chats").child("list/${sender.memberID}").setValue(sender)
    }

    fun addDataToRTDB(event:uploadableEvent  , path:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(path).setValue(event)
    }
    fun pushDataToRTDB(eventID : String  , path:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(path).push().setValue(eventID)
    }
    fun addStringToRTDB(str:String  , path:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(path).setValue(str)
    }

    suspend fun getEvent(path:String, orgID:String): MutableList<uploadableEvent> {
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val a = mDbRef.child(path).child("Events").get().await()
        val listEvent  = mutableListOf<uploadableEvent>()
        a.children.forEach {
            val temp = uploadableEvent()
            val arr = mDbRef.child(orgID).child("Events").child(it.value as String).get().await()
          //  Log.d("qwerty",it.value as String)
            if(arr.children.toList().isEmpty()){
                Log.d("qwerty",it.value as String)
                mDbRef.child(path).child("Events").child(it.key as String).removeValue().await()
            } else{
                arr.children.forEach {snapShot->
                    //   Log.d("qwerty",snapShot.key!!)
                    when(snapShot.key){
                        "title"->{  temp.title = snapShot.value as String  }
                        "description"->{ temp.description = snapShot.value as String }
                        "docUri"->{  snapShot.children.forEach { doc ->
                            val tempp = doc.getValue(PairRipOff::class.java)!!
                            temp.docUri.add(Pair(tempp.first , tempp.second))
                        } }
                        "sdate"->{  temp.Sdate = snapShot.value as Long  }
                        "edate"->{  temp.Edate = snapShot.value as Long  }
                        "eventID"->{  temp.eventID = snapShot.value as String  }
                        "imgUri"->{  snapShot.children.forEach { imgIDs ->
                            temp.imgUri.add(imgIDs.value as String)
                        } }
                        "whoUploaded"->{  temp.whoUploaded = snapShot.getValue(classOrgMember::class.java)!!  }

                    }
                }
                // Log.d("qwerty","$temp")
                listEvent.add(temp)
            }

        }

        return listEvent
    }

    suspend fun getSingleEvent(eventID:String,orgID:String):uploadableEvent{
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val temp = uploadableEvent()
        val arr = mDbRef.child(orgID).child("Events").child(eventID).get().await()
        arr.children.forEach {snapShot->
            when(snapShot.key){
                "title"->{  temp.title = snapShot.value as String  }
                "description"->{ temp.description = snapShot.value as String }
                "docUri"->{  snapShot.children.forEach { doc ->
                    val tempp = doc.getValue(PairRipOff::class.java)!!
                    temp.docUri.add(Pair(tempp.first , tempp.second))
                } }
                "sdate"->{  temp.Sdate = snapShot.value as Long  }
                "edate"->{  temp.Edate = snapShot.value as Long  }
                "eventID"->{  temp.eventID = snapShot.value as String  }
                "imgUri"->{  snapShot.children.forEach { imgIDs ->
                    temp.imgUri.add(imgIDs.value as String)
                } }
                "whoUploaded"->{  temp.whoUploaded = snapShot.getValue(classOrgMember::class.java)!!  }

            }
        }
        return temp
    }

    suspend fun deleteEventCompletely(eventID:String, orgID:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val strRef : StorageReference = FirebaseStorage.getInstance().reference

        mDbRef.child("$orgID/Events/$eventID").removeValue()
        strRef.child("$orgID/Events/docsAndImg/$eventID").listAll().await().items.forEach {
            it.delete()
        }
    }

    fun createGroup(grp:groupDetailObj,orgID:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child("$orgID/Groups/${grp.groupID}").setValue(grp)
    }

    suspend fun getGroup(grpID:String,orgID:String):groupDetailObj?{
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
       val a =  mDbRef.child("$orgID/Groups/${grpID}").get().await()
        return a.getValue(groupDetailObj::class.java)
    }
    @SuppressLint("SuspiciousIndentation")
    suspend fun getMyGroups(sender : classMemSignUp ):Map<Long,groupDetailObj>{
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val a =  mDbRef.child("${sender.orgID}/${sender.memberID}/Groups").get().await()
        val map = mutableMapOf<Long,groupDetailObj>()
        a.children.forEach {
            val tempObj = getGroup(it.value.toString(),sender.orgID)
            if(tempObj !=null)
            map[tempObj.timestamp] = tempObj
        }

        return map.toSortedMap()
    }

    fun messageFlowGroup( grpID : String , sender : classMemSignUp , lastMsgD:String ):Flow<Any>{
        return callbackFlow {
            var lastMsgID = lastMsgD
            Log.d("qwerty",lastMsgID)
            val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
            val userRef = mDbRef.child("${sender.orgID}/GroupChats/${grpID}")
            val listener =  userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = mutableListOf<MessageGrp>()
                    var flag = false
                    for (randomID in snapshot.children) {
                        var message = randomID.getValue(MessageToSendGP::class.java)!!
                        if(flag || lastMsgID.isBlank()) {
                            list.add(message.toMessageGrp())
                            lastMsgID = message.uniqueID
                        }
                        if(message.uniqueID == lastMsgID || lastMsgID.isBlank()) flag = true

                    }
                    GlobalScope.launch(Dispatchers.IO) {
                        trySend(list)
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                    GlobalScope.launch(Dispatchers.IO) {
                        trySend(error.message)
                    }}
            })
            awaitClose { userRef.removeEventListener(listener) }
        }
    }

    fun notificationFlow(  sender : classMemSignUp  ):Flow<Boolean>{
        return callbackFlow {
            val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
            val userRef = mDbRef.child("${sender.orgID}/${sender.memberID}/Notifications")
            val listener =  userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    GlobalScope.launch(Dispatchers.Default) {
                        trySend(true)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    GlobalScope.launch(Dispatchers.Default) {
                        trySend(false)
                    }}
            })
            awaitClose { userRef.removeEventListener(listener) }
        }
    }

    fun sendMsgGrp(grpID:String,obj:MessageToSendGP,orgID:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child("${orgID}/GroupChats/${grpID}").push().setValue(obj)
        mDbRef.child("${orgID}/Groups/${grpID}/timestamp").setValue(getTime().TS)
    }

    suspend fun SidebarNotificationsFetch(sender : classMemSignUp):List<Notification>{
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val notification = mDbRef.child(sender.orgID).child(sender.memberID).child("Notifications").get().await()
        val list:MutableList<Notification> = mutableListOf()
        notification.children.forEach {
                list.add(it.getValue(Notification::class.java)?: Notification())
        }
        return list
    }
    fun pushNotification(noti: Notification , path:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child(path).push()
        val data = Notification(mDbRef.key?:"Error",noti.status,noti.timestamp,noti.idby,noti.nameOrTitle,noti.extra,noti.type)
        mDbRef.setValue(data)
    }
    suspend fun pushNotification(noti: Notification , path:String , recipient:classOrgMember ,senderID:classMemSignUp){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child(path).push()
        val data = Notification(mDbRef.key?:"Error",noti.status,noti.timestamp,noti.idby,noti.nameOrTitle,noti.extra,noti.type)
        mDbRef.setValue(data)

        val notiID = myNMsgToThemID(senderID,recipient)
        if(notiID!=null) {
            val fetchedNoti = getSingleNotifiation(notiID, recipient, senderID.orgID)
            if(fetchedNoti.status == "Unread"){
                FirebaseDatabase.getInstance().reference.child(senderID.orgID).child(recipient.memberID).child("Notifications/$notiID").removeValue()
            }
        }
        setMyNMsgToThemID(senderID,recipient,data.notificationID)

    }

    fun deleteSideBarNot(notificationID:String,sender : classMemSignUp){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(sender.orgID).child(sender.memberID).child("Notifications/$notificationID").removeValue()
        Log.d("qwerty","line 498 deleted notification ")
    }
    fun changeNotificationStat(notificationID:String,sender : classMemSignUp,stat:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(sender.orgID).child(sender.memberID).child("Notifications/$notificationID/status").setValue(stat)
    }
    suspend fun getSingleNotifiation(notificationID:String,recipient : classOrgMember,orgID: String):Notification{
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val a = mDbRef.child(orgID).child(recipient.memberID).child("Notifications/$notificationID").get().await()

        return a.getValue(Notification::class.java)!!
    }
    suspend fun myNMsgToThemID(senderID:classMemSignUp , recipient : classOrgMember):String?{
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val a = mDbRef.child(senderID.orgID).child(recipient.memberID).child("NMsgID/${senderID.memberID}").get().await()
        return if(a.value == null)null else a.value.toString()
    }
    fun setMyNMsgToThemID(sender:classMemSignUp , recipient : classOrgMember , notificationID: String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        mDbRef.child(sender.orgID).child(recipient.memberID).child("NMsgID/${sender.memberID}").setValue(notificationID)
    }
    suspend fun otherNMsgToMeID(senderID:classMemSignUp , recipientID:String){
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val noti = mDbRef.child(senderID.orgID).child(senderID.memberID).child("NMsgID/${ recipientID}").get().await().value
        if(noti != null){
            val fetchedNoti = getSingleNotifiation(noti.toString(), senderID.profileDetail, senderID.orgID)
            if(fetchedNoti.status == "Unread"){
                changeNotificationStat(noti.toString(),senderID,"Accept")
            }
        }
    }





}



