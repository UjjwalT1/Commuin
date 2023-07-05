package com.cyrax.commuin.struct

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "savedMember")
data class SavedMember(
    @ColumnInfo("orgID") val orgID:String="",
    @PrimaryKey @ColumnInfo("memberID") val memberID:String="",
    @ColumnInfo("name") var name:String="",
    @ColumnInfo("profileUri") var uri:String="",
    @ColumnInfo("password") var pswd:String="",
)

@Dao
interface savedMemDao{
    @Query("SELECT * FROM savedMember")
    suspend fun getMemProfiles():List<SavedMember>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(member:SavedMember)

    @Query("DELETE FROM savedMember WHERE memberID = :memberID")
    fun delProfile(memberID:String)
}

@Entity(tableName = "savedOrg")
data class SavedOrganisation(
    @PrimaryKey @ColumnInfo("orgID") val orgID:String="",
    @ColumnInfo("name") var name:String="",
    @ColumnInfo("profileUri") var uri:String="",
    @ColumnInfo("password") var pswd:String="",
)


@Dao
interface savedOrgDao{
    @Query("SELECT * FROM savedOrg")
    suspend fun getOrgProfiles():List<SavedOrganisation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(org:SavedOrganisation)

    @Query("DELETE FROM savedOrg WHERE orgID = :orgID")
    fun delProfile(orgID:String)
}