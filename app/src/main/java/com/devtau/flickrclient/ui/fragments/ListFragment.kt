package com.devtau.flickrclient.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.devtau.database.DBSubscriberFragment
import com.devtau.database.listeners.ImagesListener
import com.devtau.database.listeners.LastSearchQueryListener
import com.devtau.database.listeners.SearchHistoryListener
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.CustomGridLayoutManager
import com.devtau.flickrclient.ImagesAdapter
import com.devtau.flickrclient.R
import com.devtau.flickrclient.util.AppUtils
import com.devtau.flickrclient.util.PreferencesManager
import com.devtau.rest.model.Image
import com.devtau.rest.util.Logger
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class ListFragment: DBSubscriberFragment() {

    private var listener: Listener? = null
    private var adapter: ImagesAdapter? = null
    private var searchViewDisposable: Disposable? = null
    private var autoCompleteStrings: List<String>? = null
    private var prefs: PreferencesManager? = null

    private var authorize: View? = null
    private var searchView: AutoCompleteTextView? = null
    private var textNoImages: View? = null
    private var recyclerView: RecyclerView? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Listener) listener = context
        else throw RuntimeException(context?.toString() + " must implement ListFragment Listener")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager.getInstance(context)
        adapter = ImagesAdapter(null, object: ImagesAdapter.Listener {
            override fun onImageSelected(image: Image, imageView: View) {
                listener?.showDetails(image, imageView)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        authorize = root.findViewById(R.id.authorize)
        searchView = root.findViewById(R.id.searchView)
        textNoImages = root.findViewById(R.id.textNoImages)
        recyclerView = root.findViewById(R.id.listView)
        initList(recyclerView)
        authorize?.setOnClickListener { listener?.authorize() }
        updateSearchVisibility()
        return root
    }

    override fun onStart() {
        super.onStart()
        val searchView = searchView ?: return
        dataSource?.getLastSearchQuery(object: LastSearchQueryListener {
            override fun processQuery(query: String?) {
                searchView.setText(query)
                searchView.setSelection(query?.length ?: 0)
            }
        })
        searchViewDisposable = RxTextView.textChanges(searchView)
            .debounce(AppUtils.CLICKS_DEBOUNCE_RATE_MS, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .skip(1)
            .map(CharSequence::trim)
            .map(CharSequence::toString)
            .subscribe(this::searchIfPossible)
            { throwable -> Logger.e(LOG_TAG, "error in searchView subscription: " + throwable.message) }

        searchView.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchIfPossible(v?.text?.toString())
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onStop() {
        super.onStop()
        if (searchViewDisposable?.isDisposed == false) searchViewDisposable?.dispose()
    }

    override fun provideDBName(): String = BuildConfig.DATABASE_NAME

    override fun restartLoaders() {
        dataSource?.getImages(object: ImagesListener {
            override fun processImages(list: List<Image>?) {
                adapter?.updateList(list)
                textNoImages?.visibility = if (list?.isNotEmpty() == true) View.GONE else View.VISIBLE
                recyclerView?.visibility = if (list?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
        })
        dataSource?.getSearchHistory(object: SearchHistoryListener {
            override fun processQueries(list: List<String>?) {
                autoCompleteStrings = list
            }
        })
    }


    fun changeContent() {/*содержимое обновится как только в бд поступит новый список картинок*/}

    fun updateSearchVisibility() {
        val authorized = prefs?.token != null
        authorize?.visibility = if (authorized) View.GONE else View.VISIBLE
        searchView?.visibility = if (authorized) View.VISIBLE else View.GONE
    }


    private fun initList(recyclerView: RecyclerView?) {
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = CustomGridLayoutManager(context, COLUMNS_COUNT)
    }

    private fun initSearchHistoryList(queries: List<String>) {
        val context = context ?: return
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, queries)
        searchView?.setAdapter(adapter)
    }

    private fun searchIfPossible(query: String?) {
        if (TextUtils.isEmpty(query)) return
        val filtered = ArrayList<String>()
        if (autoCompleteStrings != null)
            for (next in autoCompleteStrings!!)
                if (!TextUtils.equals(next, query) && next.startsWith(query!!))
                    filtered.add(next)
        initSearchHistoryList(filtered)

        dataSource?.saveSearchQuery(query!!)
        listener?.search(query!!)
    }


    interface Listener {
        fun showDetails(selectedImage: Image, imageView: View)
        fun search(searchQuery: String)
        fun authorize()
    }


    companion object {
        const val FRAGMENT_TAG = "com.devtau.flickrclient.ui.fragments.ListFragment"
        private const val COLUMNS_COUNT = 3
        private const val LOG_TAG = "ListFragment"

        fun newInstance(): ListFragment {
            val fragment = ListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}