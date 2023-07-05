package com.cyrax.commuin.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cyrax.commuin.struct.MemberModel
import com.cyrax.commuin.struct.classOrgMember
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.Jost
import com.cyrax.commuin.ui.theme.RainForest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(onNo:()->Unit , memberModel: MemberModel , navController2 : NavHostController = rememberNavController()){
    val arr = remember { mutableStateListOf<classOrgMember>().apply {
        addAll(mapToArrayforMember(memberModel.myOrganisation.value))
    } }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.8f))){
        Dialog(onDismissRequest = {} ) {
            Card( modifier = Modifier
                .size(500.dp, 750.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BlueBerry)){
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .size(500.dp, 750.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BlueBerry)
                ){
                    Row(verticalAlignment = Alignment.CenterVertically){
                        var search by rememberSaveable{ mutableStateOf(false) }
                        var query by rememberSaveable{ mutableStateOf("") }
                        IconButton(onClick = { search = !search;
                            scope.launch {
                            delay(100)
                            focusRequester.requestFocus()
                        } }  ) {
                            Icon(Icons.Outlined.FindInPage,null)
                        }
                        if(search)
                            Row(modifier = Modifier.weight(1f).border(width = 1.5.dp, color = Color.Black, RoundedCornerShape(50))){
                            BasicTextField(value = query,
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .padding(8.dp,5.dp,6.dp,0.dp)
                                .focusRequester(focusRequester)
                                 ,
                            textStyle = TextStyle.Default.copy(fontSize = 19.sp),
                                onValueChange = {
                                    if(it.length == 20 && query.length==19  ) TOAST(context,"Query length reached!")
                                    else if(it.isNotBlank()){
                                        query = it ;
                                    arr.clear();
                                    arr.addAll(mapToArrayforMember(memberModel.myOrganisation.value , queryNo = 5 , byIDorName = query.lowercase()))}
                                    else{
                                        query = it ;
                                        arr.clear();
                                        arr.addAll(mapToArrayforMember(memberModel.myOrganisation.value))
                                    } }
                            )

                        }
                        if(!search) Spacer( modifier = Modifier.weight(1f))
                        IconButton(onClick = { navController2.popBackStack() } ) {
                            Icon(Icons.Outlined.Cancel,null)
                        }
                    }
                    Column( modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp)) {
                        PersonList(  memberModel = memberModel , arr = arr, navController2 = navController2  )
                    }

                }
            }

        }
    }


}