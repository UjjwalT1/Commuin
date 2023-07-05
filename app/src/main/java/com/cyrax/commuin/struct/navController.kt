package com.cyrax.commuin.struct

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyrax.commuin.sections.ChatHolder
import com.cyrax.commuin.sections.ConvList
import com.cyrax.commuin.sections.EventHolder
import com.cyrax.commuin.sections.FeedHolder
import com.cyrax.commuin.sections.GroupHolder
import com.cyrax.commuin.sections.MemberLogin
import com.cyrax.commuin.sections.MemberSignUp
import com.cyrax.commuin.sections.MyOrganisationComposable
import com.cyrax.commuin.sections.Option
import com.cyrax.commuin.sections.OrgLogin
import com.cyrax.commuin.sections.OrgProfile
import com.cyrax.commuin.sections.OrgSignUp
import com.cyrax.commuin.sections.SideBarMem
import com.cyrax.commuin.sections.SideBarOrg
import com.cyrax.commuin.sections.mapToArray


@Composable
fun FirstHalf(appModel:CommuinModel= viewModel() , memberModel : MemberModel = viewModel()){
    var navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Option"){
        composable(route= "Option"){
            Option( cb1= { navController.navigate("OrgLogin") } ,
                cb2={ navController.navigate("MemberLogin") },
                cb3={ navController.navigate("OrgSignUp") },
                cb4={ navController.navigate("MemberSignUp") } ,
                appModel = appModel,
                onSuccess = {
                    appModel.loginFormUpdate("Reset Form",90);
                    appModel.updateLoginState(Login.Default)
                    navController.navigate("ToMember"){
                        popUpTo(navController.currentBackStackEntry?.destination?.route!!){
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(route= "OrgSignUp"){
            OrgSignUp(appModel , appModel.org.collectAsState().value,callback = {query,stat-> appModel.orgSignUpUpdate(query,stat)} ,
                onVerifyClick = {
                    appModel.orgSignUpUpdate(it , 99)
                },
                onCancelClick = { navController.popBackStack()
                    appModel.orgSignUpUpdate("Resetting the form" , 90) }
            )
        }
        composable(route= "OrgLogin"){
            OrgLogin(
                appModel = appModel,
                onBackClick = {
                    appModel.loginFormUpdate("Reset Form", 90);
                    navController.popBackStack()
                },
                onSuccess = {
                    appModel.loginFormUpdate("Reset Form",90);
                    val temp = (appModel.uiState as AppData.Organisation).appState                                             // Now i am updating the object in our view model that holds the data for the current organisation's Depts and Desigs using dAndD()
                    appModel.resetTemp(createMemState.Success(dAndD(mapToArray(temp.orgDepartments) , mapToArray(temp.designations))))                 // Here I extracted the data returned by uiState-- Success and extracting the dept and desig and feeding it do dandD obj state
                    appModel.updateLoginState(Login.Default)
                    navController.navigate("ToOrg"){
                        popUpTo(navController.currentBackStackEntry?.destination?.route!!){
                            inclusive = true
                        }
                    }
                },
                navController = navController
            )
        }
        composable(route= "MemberLogin"){

            MemberLogin(
                appModel = appModel,
                onBackClick = {
                    appModel.loginFormUpdate("Reset Form", 90);
                    navController.popBackStack()
                },
                onSuccess ={
                    appModel.loginFormUpdate("Reset Form",90);
                    appModel.updateLoginState(Login.Default)
                    navController.navigate("ToMember"){
                        popUpTo(navController.currentBackStackEntry?.destination?.route!!){
                            inclusive = true
                        }
                    }
                },
                navController = navController
            )
        }
        composable(route= "MemberSignUp"){
            MemberSignUp(
                appmodel = appModel ,
                onCreateClick = {
                    appModel.memSignUpUpdate("Create",99)
                },
                onCancelClick = {
                    navController.popBackStack()
                    appModel.memSignUpUpdate("Reset the fields",89)
                    appModel.resetTemp(createMemState.Default)
                }
            )
        }

        composable("ToOrg"){
            SideBarOrg(
                options = sidebarItemsOrg,
                onItemSelected = {
                    appModel.setCurr(it)
                },
                onExitClick = {
                    appModel.setCurr(Screen.HOME)
                    navController.popBackStack(navController.currentBackStackEntry?.destination?.route!!, true )
                },
                appModel = appModel,
                navController = navController

            )

        }
        composable("ToMember"){
            memberModel.setMyData((appModel.uiState as AppData.Member).appState)
            SideBarMem(
                options = sidebarItemsMem,
                onItemSelected = {
                    appModel.setCurr(it)
                },
                onExitClick = {
                    appModel.setCurr(Screen.HOME)
                    navController.popBackStack(navController.currentBackStackEntry?.destination?.route!!, true )
                },
                appModel = appModel,
                navController = navController ,
                memberModel = memberModel

            )

        }
        composable("OrgProfile"){
            val obj = when(appModel.uiState){
                is AppData.Organisation -> (appModel.uiState as AppData.Organisation).appState
                else -> AppData.Organisation().appState
            }
            OrgProfile(appModel = appModel ,obj = obj)
        }






    }
}