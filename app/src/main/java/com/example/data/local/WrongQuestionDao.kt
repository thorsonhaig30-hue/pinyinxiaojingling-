package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongQuestionDao {
    @Query("SELECT * FROM wrong_questions ORDER BY timestamp DESC")
    fun getAllWrongQuestions(): Flow<List<WrongQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongQuestion(wrongQuestion: WrongQuestion)

    @Query("DELETE FROM wrong_questions WHERE id = :id")
    suspend fun deleteWrongQuestionById(id: Int)

    @Query("DELETE FROM wrong_questions")
    suspend fun deleteAllWrongQuestions()

    @Query("SELECT COUNT(*) FROM wrong_questions")
    fun getWrongQuestionsCount(): Flow<Int>
}
