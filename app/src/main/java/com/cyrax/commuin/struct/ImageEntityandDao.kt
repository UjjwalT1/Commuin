package com.cyrax.commuin.struct

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query


@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey//(autoGenerate = true) val id: Int = 0,
    val name: String,
    val uri : String?=""
)

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(imageEntity: ImageEntity)

    @Query("SELECT * FROM images WHERE name = :ID")
    suspend fun obtainImage(ID:String): ImageEntity?

    @Query("DELETE FROM images WHERE name = :ID")
    suspend fun deleteImage(ID:String)
}


@Entity(tableName = "documents")
data class DocEntity(
    @PrimaryKey//(autoGenerate = true) val id: Int = 0,
    val name: String,
    val uri : String?=""
)

@Dao
interface DocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(docEntity:DocEntity)

    @Query("SELECT * FROM documents WHERE name = :ID")
    suspend fun obtainDoc(ID:String): DocEntity?

    @Query("DELETE FROM documents WHERE name = :ID")
    suspend fun deleteDoc(ID:String)
}
