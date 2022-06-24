package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.ui.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.ui.utils.Resource
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment:Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter:NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel=(activity as NewsActivity).viewModel
        setUpRecyclerView()

        var job: Job?=null
        newsAdapter.setOnItemClickListener {
            var bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
        etSearch.addTextChangedListener {
            job?.cancel()
            job= MainScope().launch {
                delay(500L)
                it?.let {
                    if(it.toString().isNotEmpty()){
                        viewModel.searchNewsResponse=null
                        viewModel.searchNews(it.toString())
                    }
                }
            }


        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response->
            when(response){
                is Resource.Success ->{
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        val totalPages = it.totalResults / 20 +2
                        isLastPage = totalPages == viewModel.searchNewsPage
                        if(isLastPage){

                        }
                    }
                }

                is Resource.Error ->{
                    hideProgressBar()
                    response.message?.let {
                        Log.e("Doxx","Error: $it")
                    }
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }

        })

    }
    private fun hideProgressBar() {
        paginationProgressBar.visibility=View.GONE
        isLoading=false
    }
    private fun showProgressBar() {
        paginationProgressBar.visibility=View.VISIBLE
        isLoading = true
    }


    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object  : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling=true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount  =layoutManager.itemCount

            val isNotLoadingAndLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPos + visibleItemCount >= totalItemCount
            val isNotAtBeginning=firstVisibleItemPos>=0
            val isTotalMoreThanVisible = totalItemCount>= 20
            val shouldPaginate = isNotLoadingAndLastPage && isAtLastItem &&
                    isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if(shouldPaginate){
                viewModel.searchNews(etSearch.text.toString() )
                isScrolling=false
            }



        }
    }
    private fun setUpRecyclerView() {
        newsAdapter= NewsAdapter()
        rvSearchNews.apply {
            adapter=newsAdapter
            layoutManager= LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)

        }
    }
}