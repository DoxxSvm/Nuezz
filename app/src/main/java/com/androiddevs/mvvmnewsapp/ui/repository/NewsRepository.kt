package com.androiddevs.mvvmnewsapp.ui.repository

import com.androiddevs.mvvmnewsapp.ui.api.RetrofitInstance
import com.androiddevs.mvvmnewsapp.ui.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.ui.models.Article

class NewsRepository(
    val db :ArticleDatabase
) {
    suspend fun getBreakingNews(countryCode:String, pageNumber: Int)=
        RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)

    suspend fun searchNews(searchQuery: String,pageNumber: Int)=
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)


    suspend fun upsert(article: Article)=db.getArticleDao().upsert(article)
    fun getSavedNews()=db.getArticleDao().getAllArticles()
    suspend fun deleteArticles(article: Article)=db.getArticleDao().deleteArticle(article)

}