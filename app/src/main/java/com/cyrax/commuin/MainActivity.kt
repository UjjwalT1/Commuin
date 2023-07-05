package com.cyrax.commuin


import android.os.Build
import  android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cyrax.commuin.Utils.FullImage
import com.cyrax.commuin.functions.shadow
import com.cyrax.commuin.sections.BrightBox
import com.cyrax.commuin.sections.Circle
import com.cyrax.commuin.sections.CustomTextField
import com.cyrax.commuin.sections.DateField
import com.cyrax.commuin.sections.EventForm
import com.cyrax.commuin.sections.GreyCard
import com.cyrax.commuin.sections.TextColumn
import com.cyrax.commuin.sections.a
import com.cyrax.commuin.sections.timeToID
import com.cyrax.commuin.struct.FirstHalf
import com.cyrax.commuin.ui.theme.BlueBerry
import com.cyrax.commuin.ui.theme.CommuinTheme
import com.cyrax.commuin.ui.theme.MidnightBlue
import com.cyrax.commuin.ui.theme.Periwinkle
import com.google.type.DateTime



class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommuinTheme {
               FirstHalf()
            }
        }
    }
}


@Preview(showBackground =false, showSystemUi = true)
@Composable
fun DefaultPreview() {
    CommuinTheme {
    }
}
/*
* Adding functinality when we click on a person button in chat
* Now to do that you also have to create composable for sender and receiver messages
* Following that you have to modify the topbar to display the Person's header during the chat
* And you also have to replace the Bottombar with the Textfield where the user will type their message
*
* You also have to create the UI for displaying the eventdetails when its clicked
*
* Then you can setup realtime database to implement chat features
*
*  */


/*
*  a single user has Organisation ID , Name , Organisation name that they belong , Org Position that they belong, reference to feeds , events
*   OrgID -> Self -> (Password,OrgName, OrgDepartments->(DeptNames SUB Designation) , Dept Name -> { Designation :{ MemberID:{ID,Name,Address-nullable,Contact , Email}} } , OrgPosts , OrgMessages)
*          -> MemberID -> { Password, MemberPropertiesInherited from organisation like (ID,Name,Address,Contact,Mail,Designation) , Messages ,EventsLists , EvensPosted by U }
*          -> FeedPool ->  FeedID: {Time, ByID  , description  }
*          -> EventsPool -> EventID{Time , ByID , Heading , Attachments , Duration , Description
* }
*
* */