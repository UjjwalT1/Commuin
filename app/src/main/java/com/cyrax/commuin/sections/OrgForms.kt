package com.cyrax.commuin.sections

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cyrax.commuin.R
import com.cyrax.commuin.struct.AppData
import com.cyrax.commuin.struct.CommuinModel
import com.cyrax.commuin.struct.createMemState
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.RainForest
import com.google.firebase.components.Lazy
import kotlinx.coroutines.launch
import java.util.Collections.addAll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMember(appmodel: CommuinModel){
    val context  = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false)  }
    val mem = appmodel.memberDetail.collectAsState().value
    Column(modifier = Modifier
        .fillMaxSize()){
        Card(colors = CardDefaults.cardColors( containerColor= Color.Cyan),
            elevation= CardDefaults.cardElevation(10.dp),modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .padding(5.dp)
                .animateContentSize()) {
            Row( modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(15.dp, 5.dp)){
                Text("Add Member" ,fontFamily = Jost , fontSize = 25.sp,)
                Spacer(Modifier.weight(1f))
            }

            AnimatedVisibility(visible = isExpanded,
                enter = fadeIn()+slideInVertically(),
                exit = slideOutVertically() + shrinkVertically() + fadeOut()) {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth() , horizontalAlignment = Alignment.CenterHorizontally){
                    item{
                        Divider(thickness = 2.dp , color = Color.Black)
                        Spacer(Modifier.height(15.dp))
                        OutlinedTextField(value = mem.memberID , onValueChange = {appmodel.addMemberToOrg(it,1)}, label = { Text("Member ID") })
                        OutlinedTextField(value = mem.name , onValueChange = {appmodel.addMemberToOrg(it,2)}, label = { Text("Name") })
                        OutlinedTextField(value = mem.email , onValueChange = {appmodel.addMemberToOrg(it,3)}, label = { Text("Email") })
                        OutlinedTextField(value = mem.contact , onValueChange = {appmodel.addMemberToOrg(it,4)}, label = { Text("Contact") } , keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                        DropDown(valueOnDisplay= mem.dept,appmodel = appmodel , query = "Department" , callback = { appmodel.addMemberToOrg(it,5) })
                        DropDown( valueOnDisplay= mem.designation , appmodel = appmodel ,query = "Designation" , callback = { appmodel.addMemberToOrg(it,6) })
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            if(mem.memberID.isNotBlank() && mem.name.isNotBlank()&& mem.email.isNotBlank()&& mem.contact.isNotBlank()&& mem.dept.isNotBlank()&&mem.designation.isNotBlank()) {
                                appmodel.addMemberToOrg(
                                    (appmodel.uiState as AppData.Organisation).appState.orgID,
                                    99
                                ) // 99 is case that now is time to add the data to server side
                                appmodel.addMemberToOrg("Reset the fields", 90)
                                TOAST(context, "MEMBER ADDED")
                            }
                            else   TOAST(context, "Empty Fields")
                        } , modifier = Modifier
                            .fillMaxWidth()
                            .padding(56.dp, 0.dp)) {
                            Text("Add",color = Color.White)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.clickable { isExpanded = !isExpanded }){
                            Spacer(Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null)
                            Spacer(Modifier.weight(1f))
                        }
                    }


                }

            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = "", onValueChange = { },
            placeholder = {Text( "Search Members" )},


            )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDept(appmodel: CommuinModel){
    val context  = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false)  }
    var str by rememberSaveable { mutableStateOf("")  }
    val scope = rememberCoroutineScope()
    val refToState = (appmodel.uiState as AppData.Organisation).appState
    val arr = remember{ mutableStateListOf<String>().apply{ addAll(mapToArray(refToState.orgDepartments))} }
    LaunchedEffect(key1 = Unit){
        appmodel.backup.clear()
        appmodel.backup.addAll(arr)
    }


    Column(modifier = Modifier
        .fillMaxSize() , horizontalAlignment = Alignment.CenterHorizontally){
        Card(colors = CardDefaults.cardColors( containerColor= Color.Cyan),
            elevation= CardDefaults.cardElevation(10.dp),modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .padding(5.dp)
                .animateContentSize()) {
            Row( modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(15.dp, 5.dp)){
                Text("Add Department" ,fontFamily = Jost , fontSize = 25.sp,)
                Spacer(Modifier.weight(1f))
            }

            AnimatedVisibility(visible = isExpanded,
                enter = fadeIn()+slideInVertically(),
                exit = slideOutVertically() + shrinkVertically() + fadeOut()) {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth() , horizontalAlignment = Alignment.CenterHorizontally){
                    item{
                        Divider(thickness = 2.dp , color = Color.Black)
                        Spacer(Modifier.height(15.dp))
                        OutlinedTextField(value = str , onValueChange = { str = it}, label = { Text("Dept Name") })
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            if(str.isBlank()) {
                                TOAST(context , "Empty Fields")
                            }else{
                                scope.launch {
                                    appmodel.addDepartmentOrDesignation(refToState.orgID, str , "dept") // 99 is case that now is time to add the data to server side
                                    arr.add(str)
                                    appmodel.backup.add(str)
                                    str = ""
                                    TOAST(context , "Department Added")
                                }

                            }
                        } , modifier = Modifier
                            .fillMaxWidth()
                            .padding(56.dp, 0.dp)) {
                            Text("Add",color = Color.White)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.clickable { isExpanded = !isExpanded }){
                            Spacer(Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null)
                            Spacer(Modifier.weight(1f))
                        }
                    }


                }

            }
        }
        Spacer(Modifier.height(10.dp))
        var query by rememberSaveable { mutableStateOf("")  }
        OutlinedTextField(
            modifier = Modifier.width(310.dp).height(63.dp),
            shape = RoundedCornerShape(50) ,
            value = query, onValueChange = {
                query = it;
                if(it.isBlank()){
                    arr.clear(); arr.addAll(appmodel.backup)
                }else{ arr.clear(); arr.addAll(filter(it,appmodel.backup))
                }
            },
            placeholder = {Text( "Search Depts" )},
            )
        LazyColumn(modifier = Modifier.animateContentSize()){
            items(arr){ value->
                AnimatedVisibility(visible = true,
                    enter = fadeIn()+slideInVertically(),
                    exit = slideOutHorizontally() + shrinkHorizontally() + fadeOut()) {
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 1.dp)
                        .animateContentSize(),
                        elevation = CardDefaults.cardElevation(defaultElevation= 0.dp),
                        colors = CardDefaults.cardColors(RainForest.onSurface)){
                        Row(verticalAlignment = Alignment.CenterVertically, modifier= Modifier
                            .fillMaxWidth()
                            .padding(2.dp, 1.dp)
                        ){
                            Spacer(Modifier.width(10.dp))
                            Text(value, style = MaterialTheme.typography.titleLarge , fontWeight= FontWeight.Normal, )
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    refToState.orgDepartments.remove(value)
                                    arr.remove(value)
                                    appmodel.backup.remove(value)
                                    (appmodel.dAndDObj as createMemState.Success).dandD.depts.remove(
                                        value
                                    )
                                    scope.launch {
                                        appmodel.delDepartmentOrDesignation(
                                            refToState.orgID,
                                            refToState.orgDepartments,
                                            "dept"
                                        ) // 99 is case that now is time to add the data to server side
                                    }
                                }
                            ){ Image(painterResource(id = R.drawable.trash), contentDescription = null  , modifier = Modifier.size(30.dp))}
                        }
                    }
                }

            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDesignation(appmodel: CommuinModel ){
    val context  = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false)  }
    var str by rememberSaveable { mutableStateOf("")  }
    val refToState = (appmodel.uiState as AppData.Organisation).appState
    val arr = remember{ mutableStateListOf<String>().apply{ addAll(mapToArray(refToState.designations))} }
    LaunchedEffect(key1 = Unit){
        appmodel.backup.clear()
        appmodel.backup.addAll(arr)
    }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
        Card(colors = CardDefaults.cardColors( containerColor= Color.Cyan),
            elevation= CardDefaults.cardElevation(10.dp),modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .padding(5.dp)
                .animateContentSize()) {
            Row( modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(15.dp, 5.dp)){
                Text("Add Designation" ,fontFamily = Jost , fontSize = 25.sp)
                Spacer(Modifier.weight(1f))
            }

            AnimatedVisibility(visible = isExpanded,
                enter = fadeIn()+slideInVertically(),
                exit = slideOutVertically() + shrinkVertically() + fadeOut()) {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth() , horizontalAlignment = Alignment.CenterHorizontally){
                    item{
                        Divider(thickness = 2.dp , color = Color.Black)
                        Spacer(Modifier.height(15.dp))
                        OutlinedTextField(value = str , onValueChange = { str = it}, label = { Text("Designation Name") })
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            if(str.isBlank()) {
                                TOAST(context , "Empty Fields")
                            }else{
                                scope.launch {
                                    appmodel.addDepartmentOrDesignation(refToState.orgID, str , "designtion") // 99 is case that now is time to add the data to server side
                                    arr.add(str)
                                    appmodel.backup.add(str)
                                    str = ""
                                    TOAST(context , "Designation Added")
                                }

                            }
                        } , modifier = Modifier
                            .fillMaxWidth()
                            .padding(56.dp, 0.dp)) {
                            Text("Add",color = Color.White)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.clickable { isExpanded = !isExpanded }){
                            Spacer(Modifier.weight(1f))
                            Icon(imageVector = Icons.Filled.ExpandLess, contentDescription = null)
                            Spacer(Modifier.weight(1f))
                        }
                    }


                }

            }
        }
        Spacer(Modifier.height(10.dp))
        var query by rememberSaveable { mutableStateOf("")  }

        OutlinedTextField(
            modifier = Modifier.width(310.dp).height(63.dp),
            shape = RoundedCornerShape(50) ,
            value = query, onValueChange = {
                query = it;
                if(it.isBlank()){
                    arr.clear(); arr.addAll(appmodel.backup)
                }else{ arr.clear(); arr.addAll(filter(it,appmodel.backup))
                }
                                           },
            placeholder = {Text( "Search Designation" )},
        )
        LazyColumn(modifier = Modifier.animateContentSize()){
            items(arr){ value->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp, 1.dp)
                    .animateContentSize(),
                    elevation = CardDefaults.cardElevation(defaultElevation= 0.dp),
                    colors = CardDefaults.cardColors(RainForest.onSurface)){
                    Row(verticalAlignment = Alignment.CenterVertically, modifier= Modifier
                        .fillMaxWidth()
                        .padding(2.dp, 1.dp)
                    ){
                        Spacer(Modifier.width(10.dp))
                        Text(value, style = MaterialTheme.typography.titleLarge , fontWeight= FontWeight.Normal, )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                refToState.designations.remove(value)
                                arr.remove(value)
                                appmodel.backup.remove(value)
                                (appmodel.dAndDObj as createMemState.Success).dandD.designations.remove(
                                    value
                                )
                                scope.launch {
                                    appmodel.delDepartmentOrDesignation(
                                        refToState.orgID,
                                        refToState.designations,
                                        "desig"
                                    ) // 99 is case that now is time to add the data to server side
                                }
                            }
                        ){ Image(painterResource(id = R.drawable.trash), contentDescription = null  , modifier = Modifier.size(30.dp))}
                    }
                }
            }
        }



    }
}



@SuppressLint("SuspiciousIndentation")
fun TOAST(context: Context, msg:String, len:String = "Short"){
    if(len == "Short")
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, msg,Toast.LENGTH_SHORT).show()
    }
    else
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, msg,Toast.LENGTH_LONG).show()
    }

}

fun filter(query:String , arr:List<String>):List<String>{
    val temp = mutableListOf<String>()
    for(ele in arr){
        if(ele.contains(query , ignoreCase = true))
            temp.add(ele)
    }
    return temp
}


fun mapToArray(m:MutableMap<String,Int>):MutableList<String>{
    var temp : MutableList<String> = mutableListOf()
    m.forEach { t, _ ->
        temp.add(t)
    }
    return temp
}