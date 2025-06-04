package com.example.eduquest.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ArticleDao {

    @Insert
    void insert(Article article);

    @Delete
    void delete(Article article);

    @Query("SELECT * FROM articles ORDER BY id DESC")
    List<Article> getAllArticles();

    @Insert
    void insertAll(Article article);
}
