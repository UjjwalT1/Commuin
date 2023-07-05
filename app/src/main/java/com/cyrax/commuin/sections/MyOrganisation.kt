package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cyrax.commuin.R
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.Screen
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.struct.myOrg
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.RainForest



@Composable
fun MyOrganisationComposable(navController2: NavHostController , memberModel: MemberModel){
    val context = LocalContext.current
    val myOrgObj = memberModel.myOrganisation.collectAsState().value
    var query by rememberSaveable{ mutableStateOf("")}
    val arr = remember { mutableStateListOf<classOrgMember>().apply {
        addAll(mapToArrayforMember(memberModel.myOrganisation.value))
    } }
    Column(Modifier.fillMaxSize().background(MidnightBlue),horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(50),value = query,
            onValueChange = {
                if(it.length == 20 && query.length==19 ) TOAST(context,"Query length reached!")
                else if(it.isNotBlank()){
                    query = it ;
                    arr.clear();
                    arr.addAll(mapToArrayforMember(memberModel.myOrganisation.value , queryNo = 5 , byIDorName = query.lowercase()))}
                else{
                    query = it ;
                    arr.clear();
                    arr.addAll(mapToArrayforMember(memberModel.myOrganisation.value))
                }

            },
            placeholder = { Text("Search")}

        )
        if(query.isNotBlank())PersonList(memberModel = memberModel, arr = arr , navController2)
        else DefaultView(memberModel = memberModel, obj =  myOrgObj ,  navController2 = navController2)
    }


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DefaultView( navController2: NavHostController , memberModel: MemberModel  ,obj : myOrg ){
    LazyColumn{
        itemsIndexed(obj.members.keys.toList()){i,deptName->
            val expand = rememberSaveable{ mutableStateOf(false) }
            Card(onClick = { expand.value = !expand.value },modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 2.dp)
                .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation= 3.dp),
                colors = CardDefaults.cardColors(RainForest.surface)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier= Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 7.dp)
                ){
                    Spacer(Modifier.width(10.dp))
                    Text(text =deptName , style = MaterialTheme.typography.titleLarge  , fontWeight = FontWeight.ExtraBold , fontFamily = Jost )
                    Spacer(Modifier.weight(1f))
                    if(!expand.value) Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null )
                    else Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null )
                    Spacer(Modifier.width(5.dp))
                }
                AnimatedContent(targetState = expand.value,
                    transitionSpec = {  scaleIn() + fadeIn() with  scaleOut(tween(100)) + fadeOut(
                        tween(100)
                    ) }){
                    if(it){
                        Column{
                            (obj.members[deptName]!!.keys.toList()).forEach{designatn->
                                val temp = obj.members[deptName]!![designatn]!!
                                val expand2 = rememberSaveable{ mutableStateOf(false) }
                                Card(onClick = { expand2.value = !expand2.value },modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp, 3.dp)
                                    .animateContentSize(),
                                    elevation = CardDefaults.cardElevation(defaultElevation= 3.dp),
                                    colors = CardDefaults.cardColors(RainForest.outline)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier= Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp, 6.dp)
                                    ){
                                        Spacer(Modifier.width(10.dp))
                                        MiniText(name = designatn, style1 = MaterialTheme.typography.titleLarge , weight1=FontWeight.Normal,
                                            position = "${temp.size} People"  , style2 = MaterialTheme.typography.bodyLarge ,
                                            department = ""
                                        )

                                        Spacer(Modifier.weight(1f))
                                        if(!expand2.value) Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null )
                                        else Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null )
                                        Spacer(Modifier.width(5.dp))
                                    }
                                    if(expand2.value){
                                        DefaultPersonList(memberModel  = memberModel, arr = temp.values.toList() , navController2 =  navController2)
                                    }
                                    Spacer(Modifier.height(5.dp))
                                }

                            }
                            Spacer(Modifier.height(8.dp))
                        }

                    }
                }


            }

        }
    }
}

@Composable
fun DefaultPersonList(  memberModel: MemberModel  , arr : List<classOrgMember> , navController2: NavHostController ){
    Column{
        (arr).forEachIndexed{ _, ele->
            SingleMember(ele  = ele , memberModel = memberModel , navController2 = navController2)
        }
    }
}

@Composable
fun PersonList(memberModel: MemberModel  , arr : SnapshotStateList<classOrgMember> ,  navController2: NavHostController  ){
    LazyColumn{
        itemsIndexed(arr){ _, ele->
            SingleMember(ele  = ele , memberModel = memberModel ,  navController2= navController2)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleMember(navController2 : NavHostController, ele : classOrgMember, memberModel: MemberModel , context : Context = LocalContext.current){
    val expand = rememberSaveable{ mutableStateOf(false) }
    Card(onClick = { expand.value = !expand.value },modifier = Modifier
        .fillMaxWidth()
        .padding(15.dp, 1.dp)
        .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation= 0.dp),
        colors = CardDefaults.cardColors(if(memberModel.myData.collectAsState().value.memberID == ele.memberID) RainForest.secondary else RainForest.onSurface)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier= Modifier
            .fillMaxWidth()
            .padding(2.dp, 1.dp)
        ){
            Spacer(Modifier.width(10.dp))
            MiniText(name = ele.name, style1 = MaterialTheme.typography.titleLarge , weight1=FontWeight.Normal,
                position = ele.designation  , style2 = MaterialTheme.typography.bodyLarge ,
                department = ele.dept
            )
            Spacer(Modifier.weight(1f))
            val hasActiveAcc = memberModel.allAccountName.collectAsState().value[ele.memberID] !=null
            IconButton(
                onClick = {
                    if(!hasActiveAcc) TOAST(context , "Member doesn't have an Account")
                    else{ memberModel.setCurrChatOpened(ele); // memberModel.setCurr(Screen.CONVERSATION);
                        navController2.navigate("currentChatOpened")
                        memberModel.collectMsgAndStore(context)
                        memberModel.populateOnScreenMsg(context)
                    }
                }
            ) { if(hasActiveAcc) Icon(imageVector = Icons.Filled.Message, contentDescription = null )
            else Icon(imageVector = Icons.Filled.Message, contentDescription = null ,  modifier = Modifier.alpha(.4f)  )  }

            Spacer(Modifier.width(10.dp))
            if(!expand.value) Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null )
            else Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null )
            Spacer(Modifier.width(5.dp))
        }
        if(expand.value){
            Spacer(Modifier.height(3.dp))
            Divider(color = Color.Black , thickness = 1.dp )
            Card(onClick = { expand.value = !expand.value },modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp, 4.dp)
                .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation= 0.dp),
                colors = CardDefaults.cardColors(Color.Transparent)) {
                Text("ID : ${ele.memberID}")
                Spacer(Modifier.height(3.dp))
                Text("Email Address : ${ele.email}")

            }
        }


        Spacer(Modifier.height(6.dp))
    }

}


fun mapToArrayforMember(obj : myOrg , queryNo :Int = 1 , keyDept:String="", keyDesig:String="" , byIDorName : String="") : List<classOrgMember>{
   val temp : MutableList<classOrgMember>  = mutableListOf()
    when(queryNo){
        1 ->{  //when 1 then returns all the members in the organisation
            for(dept in obj.members){
                for(design in dept.value){
                    for(member in design.value){
                        temp.add(member.value)
                    }
                }
            }
        }
        2->{ // Members by department
            for(dept in obj.members[keyDept]!!){
                for(member in dept.value){
                    temp.add(member.value)
                }
            }
        }
        3->{ // Members by designation
            for(dept in obj.members){
                for(member in dept.value[keyDesig]!!){
                        temp.add(member.value)
                }
            }
        }
        4 ->{ // Members by designation and dept
            for(member in obj.members[keyDept]!![keyDesig]!!){
                temp.add(member.value)
            }
        }
        5-> {   //Find elements by given name or ID
            for(dept in obj.members){
                for(design in dept.value){
                    for(member in design.value){
                        if(member.value.name.lowercase().contains(byIDorName))
                            temp.add(member.value)
                    }
                }
            }
        }

    }

    return temp.toList()
}


/*myOrg(mutableMapOf("CSE" to mutableMapOf("Student" to mutableMapOf("15039" to classOrgMember(name = "Ujjwal" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),
        "15040" to classOrgMember(name = "Shyam" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),"15047" to classOrgMember(name = "Ankit" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE")
    ),"Staff" to mutableMapOf("15039" to classOrgMember(name = "Ujjwal" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),
        "15040" to classOrgMember(name = "Shyam" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),"15047" to classOrgMember(name = "Ankit" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE")
    )
    ),
        "MEC" to mutableMapOf("Student" to mutableMapOf("15039" to classOrgMember(name = "Ujjwal" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),
            "15040" to classOrgMember(name = "Shyam" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),"15047" to classOrgMember(name = "Ankit" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE")
        ),"Staff" to mutableMapOf("15039" to classOrgMember(name = "Ujjwal" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),
            "15040" to classOrgMember(name = "Shyam" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),"15047" to classOrgMember(name = "Ankit" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE")
        )
        ),
        "ECE" to mutableMapOf("Student" to mutableMapOf("15039" to classOrgMember(name = "Ujjwal" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),
            "15040" to classOrgMember(name = "Shyam" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),"15047" to classOrgMember(name = "Ankit" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE")
        ),"Staff" to mutableMapOf("15039" to classOrgMember(name = "Ujjwal" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),
            "15040" to classOrgMember(name = "Shyam" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE"),"15047" to classOrgMember(name = "Ankit" , memberID = "15039" , email = "ut1msc@gmail.com" , designation = "Student" , dept = "CSE")
        )
        )
    ))*/




