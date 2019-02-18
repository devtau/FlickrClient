package com.devtau.flickrclient.ui.activities

import java.util.ArrayList
import java.util.LinkedList
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.database.SQLException
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TableRow.LayoutParams
import android.widget.TextView
import android.widget.Toast
import com.devtau.database.SQLHelper
import com.devtau.flickrclient.BuildConfig
import com.devtau.flickrclient.R
import com.devtau.rest.util.Logger

@Suppress("NAME_SHADOWING", "SetTextI18n", "ResourceType")
class DBViewerActivity: AppCompatActivity() {

    private var dbm: SQLHelper? = null

    private var numberOfPages = 0
    private var currentPage = 0
    private var tableName = ""
    private var mainCursor: Cursor? = null
    private var valueString: ArrayList<String?>? = null
    private var emptyTableColumnNames: ArrayList<String>? = null
    private var isEmpty: Boolean = false
    private var isCustomQuery: Boolean = false

    private var tableLayout: TableLayout? = null
    private var tableRowParams: TableRow.LayoutParams? = null
    private var tvMessage: TextView? = null
    private var previous: Button? = null
    private var next: Button? = null
    private var textView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbm = SQLHelper.getInstance(this, BuildConfig.DATABASE_NAME)

        val mainScrollView = ScrollView(this)
        //the main linear layout to which all tables spinners etc will be added
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        val padding = Math.round(resources.getDimension(R.dimen.margin_normal))
        mainLayout.setPadding(padding, padding, padding, padding)
        mainLayout.setBackgroundColor(Color.WHITE)
        mainLayout.isScrollContainer = true
        mainScrollView.addView(mainLayout)
        setContentView(mainScrollView)

        //the first row of layout which has a text view and spinner
        val firstRow = LinearLayout(this)
        firstRow.setPadding(0, 10, 0, 20)
        val firstRowParams = LinearLayout.LayoutParams(0, 150)
        firstRowParams.weight = 1f

        val mainText = TextView(this)
        mainText.text = "Select Table"
        mainText.textSize = 22f
        mainText.layoutParams = firstRowParams
        val selectTable = Spinner(this)
        selectTable.layoutParams = firstRowParams

        firstRow.addView(mainText)
        firstRow.addView(selectTable)
        mainLayout.addView(firstRow)

        //the second row of the layout which shows number of records in the table selected by user
        val secondRow = LinearLayout(this)
        secondRow.setPadding(0, 20, 0, 10)
        val secondRowParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        secondRowParams.weight = 1f
        val secondRowText = TextView(this)
        secondRowText.text = "No. Of Records : "
        secondRowText.textSize = 20f
        secondRowText.layoutParams = secondRowParams
        textView = TextView(this)
        textView?.textSize = 20f
        textView?.layoutParams = secondRowParams
        secondRow.addView(secondRowText)
        secondRow.addView(textView)
        mainLayout.addView(secondRow)

        //A button which generates a text view from which user can write custom queries
        val customQueryText = EditText(this)
        customQueryText.visibility = View.GONE
        customQueryText.hint = "Enter Your Query here and Click on Submit Query Button .Results will be displayed below"
        mainLayout.addView(customQueryText)

        val submitQuery = Button(this)
        submitQuery.visibility = View.GONE
        submitQuery.text = "Submit Query"
        submitQuery.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        mainLayout.addView(submitQuery)

        // the spinner which gives user a option to add new row, drop or delete table
        val spinnerTable = Spinner(this)
        mainLayout.addView(spinnerTable)

        val help = TextView(this)
        help.text = "Click on the row below to update values or delete the tuple"
        help.setPadding(0, 5, 0, 5)
        mainLayout.addView(help)

        tableLayout = TableLayout(this)
        tableLayout?.isHorizontalScrollBarEnabled = true
        val horizontalScrollView = HorizontalScrollView(this)
        horizontalScrollView.addView(tableLayout)
        horizontalScrollView.setPadding(0, 10, 0, 10)
        horizontalScrollView.isScrollbarFadingEnabled = false
        horizontalScrollView.scrollBarStyle = View.SCROLLBARS_OUTSIDE_INSET
        mainLayout.addView(horizontalScrollView)

        //the third layout which has buttons for the pagination of content from database
        val thirdRow = LinearLayout(this)
        thirdRow.setPadding(0, 10, 0, 10)

        previous = Button(this)
        previous?.text = "Previous"
        previous?.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        previous?.layoutParams = secondRowParams
        thirdRow.addView(previous)

        val tvBlank = TextView(this)
        tvBlank.layoutParams = secondRowParams
        thirdRow.addView(tvBlank)

        next = Button(this)
        next?.text = "Next"
        next?.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        next?.layoutParams = secondRowParams
        thirdRow.addView(next)
        mainLayout.addView(thirdRow)

        tvMessage = TextView(this)
        tvMessage?.text = "Error Messages will be displayed here"
        tvMessage?.textSize = 18f
        mainLayout.addView(tvMessage)

        val customQueryButton = Button(this)
        customQueryButton.text = "Custom Query"
        customQueryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        customQueryButton.setOnClickListener {
            //set drop down to custom Query
            isCustomQuery = true
            secondRow.visibility = View.GONE
            spinnerTable.visibility = View.GONE
            help.visibility = View.GONE
            customQueryText.visibility = View.VISIBLE
            submitQuery.visibility = View.VISIBLE
            selectTable.setSelection(0)
            customQueryButton.visibility = View.GONE
        }
        mainLayout.addView(customQueryButton)

        //when user enters a custom query in text view and clicks on submit query button display results in tableLayout
        submitQuery.setOnClickListener {
            tableLayout?.removeAllViews()
            customQueryButton.visibility = View.GONE

            val query = customQueryText.text.toString()
            Log.d("query", query)
            val cursors = getData(query)
            val cursor = cursors[0]
            val messagesCursor = cursors[1]
            messagesCursor?.moveToLast()

            //if the query returns results display the results in table layout
            if (messagesCursor?.getString(0).equals("Success", ignoreCase = true)) {
                tvMessage?.setBackgroundColor(Color.parseColor("#2ecc71"))
                if (cursor != null) {
                    tvMessage?.text = "Query Executed successfully.Number of rows returned :" + cursor.count
                    if (cursor.count > 0) {
                        mainCursor = cursor
                        refreshTable(1)
                    }
                } else {
                    tvMessage?.text = "Query Executed successfully"
                    refreshTable(1)
                }
            } else {
                //if there is any error we displayed the error message at the bottom of the screen
                tvMessage?.setBackgroundColor(Color.parseColor("#e74c3c"))
                tvMessage?.text = "Error:" + messagesCursor?.getString(0)
            }
        }
        //layout params for each row in the table
        tableRowParams = TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        tableRowParams?.setMargins(0, 0, 2, 0)

        // a query which returns a cursor with the list of tables in the database. We use this cursor to populate spinner in the first row
        val cursors = getData("SELECT name _id FROM sqlite_master WHERE type ='table'")
        val cursor = cursors[0]
        val messagesCursor = cursors[1]

        messagesCursor?.moveToLast()
        val msg = messagesCursor?.getString(0)
        Log.d("Message from sql = ", msg)

        val tableNames = ArrayList<String>()

        if (cursor != null) {
            cursor.moveToFirst()
            tableNames.add("click here")
            do {
                //add names of the table to tableNames array list
                tableNames.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        //an array adapter with above created arrayList
        val tableNamesAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tableNames) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setBackgroundColor(Color.WHITE)
                (view as TextView).textSize = 20f
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                view.setBackgroundColor(Color.WHITE)
                return view
            }
        }

        tableNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selectTable.adapter = tableNamesAdapter

        // when a table names is selected display the table contents
        selectTable.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (pos == 0 && !isCustomQuery) {
                    secondRow.visibility = View.GONE
                    horizontalScrollView.visibility = View.GONE
                    thirdRow.visibility = View.GONE
                    spinnerTable.visibility = View.GONE
                    help.visibility = View.GONE
                    tvMessage?.visibility = View.GONE
                    customQueryText.visibility = View.GONE
                    submitQuery.visibility = View.GONE
                    customQueryButton.visibility = View.GONE
                }
                if (pos != 0) {
                    secondRow.visibility = View.VISIBLE
                    spinnerTable.visibility = View.VISIBLE
                    help.visibility = View.VISIBLE
                    customQueryText.visibility = View.GONE
                    submitQuery.visibility = View.GONE
                    customQueryButton.visibility = View.VISIBLE
                    horizontalScrollView.visibility = View.VISIBLE

                    tvMessage?.visibility = View.VISIBLE

                    thirdRow.visibility = View.VISIBLE
                    cursor!!.moveToPosition(pos - 1)
                    //displaying the content of the table which is selected in the select_table spinner
                    Log.d("selected table name is", "" + cursor.getString(0))
                    tableName = cursor.getString(0)
                    tvMessage?.text = "Error Messages will be displayed here"
                    tvMessage?.setBackgroundColor(Color.WHITE)

                    //removes any data if present in the table layout
                    tableLayout?.removeAllViews()
                    val spinnerTableValues = ArrayList<String>()
                    spinnerTableValues.add("Click here to change this table")
                    spinnerTableValues.add("Add row to this table")
                    spinnerTableValues.add("Delete this table")
                    spinnerTableValues.add("Drop this table")
                    val spinnerArrayAdapter = ArrayAdapter(this@DBViewerActivity, android.R.layout.simple_spinner_dropdown_item, spinnerTableValues)
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

                    // a array adapter which add values to the spinner which helps in user making changes to the table
                    val adapter = object : ArrayAdapter<String>(this@DBViewerActivity, android.R.layout.simple_spinner_item, spinnerTableValues) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getView(position, convertView, parent)
                            view.setBackgroundColor(Color.WHITE)
                            (view as TextView).textSize = 20f
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            view.setBackgroundColor(Color.WHITE)
                            return view
                        }
                    }

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTable.adapter = adapter
                    val query = "select * from " + cursor.getString(0)
                    Log.d("", "" + query)
                    val cursor = getData(query)[0]
                    mainCursor = cursor

                    // if the cursor returned form the database is not null we display the data in table layout
                    if (cursor != null) {
                        val counts = cursor.count
                        isEmpty = false
                        Log.d("counts", "" + counts)
                        textView?.text = "" + counts

                        //the spinnerTable has the 3 items to drop , delete , add row to the table selected by the user
                        //here we handle the 3 operations.
                        spinnerTable.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                                (parentView.getChildAt(0) as TextView).setTextColor(Color.rgb(0, 0, 0))
                                //when user selects to drop the table the below code in if block will be executed
                                if (spinnerTable.selectedItem.toString() == "Drop this table") {
                                    runOnUiThread {
                                        if (!isFinishing) {
                                            // when user confirms by clicking on yes we drop the table by executing drop table query
                                            AlertDialog.Builder(this@DBViewerActivity)
                                                    .setTitle("Are you sure ?")
                                                    .setMessage("Pressing yes will remove $tableName table from database")
                                                    .setPositiveButton("yes") { _, _ ->
                                                        val cursor = getData("Drop table $tableName")[1]
                                                        cursor?.moveToLast()
                                                        Log.d("Drop table Message", cursor?.getString(0))
                                                        if (cursor?.getString(0).equals("Success", ignoreCase = true)) {
                                                            tvMessage?.setBackgroundColor(Color.parseColor("#2ecc71"))
                                                            tvMessage?.text = tableName + "Dropped successfully"
                                                            finish()
                                                            startActivity(intent)
                                                        } else {
                                                            //if there is any error we displayed the error message at the bottom of the screen
                                                            tvMessage?.setBackgroundColor(Color.parseColor("#e74c3c"))
                                                            tvMessage?.text = "Error:" + cursor?.getString(0)
                                                            spinnerTable.setSelection(0)
                                                        }
                                                    }
                                                    .setNegativeButton("No") { _, _ -> spinnerTable.setSelection(0) }
                                                    .create().show()
                                        }
                                    }
                                }
                                //when user selects to drop the table the below code in if block will be executed
                                if (spinnerTable.selectedItem.toString() == "Delete this table") {
                                    runOnUiThread {
                                        if (!isFinishing) {
                                            // when user confirms by clicking on yes we drop the table by executing delete table query
                                            AlertDialog.Builder(this@DBViewerActivity)
                                                    .setTitle("Are you sure?")
                                                    .setMessage("Clicking on yes will delete all the contents of $tableName table from database")
                                                    .setPositiveButton("yes") { _, _ ->
                                                        val cursor = getData("Delete  from $tableName")[1]
                                                        cursor?.moveToLast()
                                                        Log.d("Delete table Message", cursor?.getString(0))
                                                        if (cursor?.getString(0).equals("Success", ignoreCase = true)) {
                                                            tvMessage?.setBackgroundColor(Color.parseColor("#2ecc71"))
                                                            tvMessage?.text = "$tableName table content deleted successfully"
                                                            isEmpty = true
                                                            refreshTable(0)
                                                        } else {
                                                            tvMessage?.setBackgroundColor(Color.parseColor("#e74c3c"))
                                                            tvMessage?.text = "Error:" + cursor?.getString(0)
                                                            spinnerTable.setSelection(0)
                                                        }
                                                    }
                                                    .setNegativeButton("No") { _, _ -> spinnerTable.setSelection(0) }
                                                    .create().show()
                                        }
                                    }
                                }

                                //when user selects to add row to the table the below code in if block will be executed
                                if (spinnerTable.selectedItem.toString() == "Add row to this table") {
                                    //we create a layout which has textViews with column names of the table and editTexts where
                                    //user can enter value which will be inserted into the database.
                                    val addNewRowNames = LinkedList<TextView>()
                                    val addNewRowValues = LinkedList<EditText>()
                                    val scrollView = ScrollView(this@DBViewerActivity)
                                    val cursor = mainCursor
                                    if (isEmpty) {
                                        getColumnNames()
                                        if (emptyTableColumnNames != null) {
                                            for (i in emptyTableColumnNames!!.indices) {
                                                val cname = emptyTableColumnNames!![i]
                                                val tv = TextView(this@DBViewerActivity)
                                                tv.text = cname
                                                addNewRowNames.add(tv)
                                            }
                                        }
                                        for (i in addNewRowNames.indices) {
                                            val et = EditText(this@DBViewerActivity)
                                            addNewRowValues.add(et)
                                        }
                                    } else {
                                        for (i in 0 until cursor!!.columnCount) {
                                            val cname = cursor.getColumnName(i)
                                            val tv = TextView(this@DBViewerActivity)
                                            tv.text = cname
                                            addNewRowNames.add(tv)
                                        }
                                        for (i in addNewRowNames.indices) {
                                            val et = EditText(this@DBViewerActivity)
                                            addNewRowValues.add(et)
                                        }
                                    }
                                    val addNewLayout = RelativeLayout(this@DBViewerActivity)
                                    val addNewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                                    addNewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                    for (i in addNewRowNames.indices) {
                                        val tv = addNewRowNames[i]
                                        val et = addNewRowValues[i]
                                        val t = i + 400
                                        val k = i + 500
                                        val lid = i + 600

                                        tv.id = t
                                        tv.setTextColor(Color.parseColor("#000000"))
                                        et.setBackgroundColor(Color.parseColor("#F2F2F2"))
                                        et.setTextColor(Color.parseColor("#000000"))
                                        et.id = k
                                        val ll = LinearLayout(this@DBViewerActivity)
                                        val tvl = LinearLayout.LayoutParams(0, 100)
                                        tvl.weight = 1f
                                        ll.addView(tv, tvl)
                                        ll.addView(et, tvl)
                                        ll.id = lid

                                        Log.d("Edit Text Value", "" + et.text.toString())

                                        val rll = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                                        rll.addRule(RelativeLayout.BELOW, ll.id - 1)
                                        rll.setMargins(0, 20, 0, 0)
                                        addNewLayout.addView(ll, rll)
                                    }
                                    addNewLayout.setBackgroundColor(Color.WHITE)
                                    scrollView.addView(addNewLayout)
                                    Log.d("Button Clicked", "")
                                    //the above form layout which we have created above will be displayed in an alert dialog
                                    runOnUiThread {
                                        if (!isFinishing) {
                                            // after entering values if user clicks on add we take the values and run a insert query
                                            AlertDialog.Builder(this@DBViewerActivity)
                                                    .setTitle("values")
                                                    .setCancelable(false)
                                                    .setView(scrollView)
                                                    .setPositiveButton("Add") { _, _ ->
                                                        var query = "Insert into $tableName ("
                                                        for (i in addNewRowNames.indices) {
                                                            val tv = addNewRowNames[i]
                                                            query = if (i == addNewRowNames.size - 1)
                                                                query + tv.text.toString()
                                                            else
                                                                query + tv.text.toString() + ", "
                                                        }
                                                        query = "$query ) VALUES ( "
                                                        for (i in addNewRowNames.indices) {
                                                            val et = addNewRowValues[i]
                                                            query = if (i == addNewRowNames.size - 1)
                                                                query + "'" + et.text.toString() + "' ) "
                                                            else
                                                                query + "'" + et.text.toString() + "' , "
                                                        }
                                                        //this is the insert query which has been generated
                                                        Log.d("Insert Query", query)
                                                        val cursor = getData(query)[1]
                                                        cursor?.moveToLast()
                                                        Log.d("Add New Row", cursor?.getString(0))
                                                        if (cursor?.getString(0).equals("Success", ignoreCase = true)) {
                                                            tvMessage?.setBackgroundColor(Color.parseColor("#2ecc71"))
                                                            tvMessage?.text = "New Row added successfully to $tableName"
                                                            refreshTable(0)
                                                        } else {
                                                            tvMessage?.setBackgroundColor(Color.parseColor("#e74c3c"))
                                                            tvMessage?.text = "Error:" + cursor?.getString(0)
                                                            spinnerTable.setSelection(0)
                                                        }
                                                    }
                                                    .setNegativeButton("close") { _, _ -> spinnerTable.setSelection(0) }
                                                    .create().show()
                                        }
                                    }
                                }
                            }

                            override fun onNothingSelected(arg0: AdapterView<*>) {}
                        }

                        //display the first row of the table with column names of the table selected by the user
                        val tableHeader = TableRow(this@DBViewerActivity)
                        tableHeader.setBackgroundColor(Color.BLACK)
                        tableHeader.setPadding(0, 2, 0, 2)
                        for (i in 0 until cursor.columnCount) {
                            val cell = LinearLayout(this@DBViewerActivity)
                            cell.setBackgroundColor(Color.WHITE)
                            cell.layoutParams = tableRowParams
                            val tableHeaderColumns = TextView(this@DBViewerActivity)
                            tableHeaderColumns.setPadding(0, 0, 4, 3)
                            tableHeaderColumns.text = "" + cursor.getColumnName(i)
                            tableHeaderColumns.setTextColor(Color.parseColor("#000000"))
                            cell.addView(tableHeaderColumns)
                            tableHeader.addView(cell)
                        }
                        tableLayout?.addView(tableHeader)
                        cursor.moveToFirst()
                        paginateTable()
                    } else {
                        //if the cursor returned from the database is empty we show that table is empty
                        help.visibility = View.GONE
                        tableLayout?.removeAllViews()
                        getColumnNames()

                        val cell = LinearLayout(this@DBViewerActivity)
                        cell.setBackgroundColor(Color.WHITE)
                        cell.layoutParams = tableRowParams

                        val tableHeaderColumns = TextView(this@DBViewerActivity)
                        tableHeaderColumns.setPadding(0, 0, 4, 3)
                        tableHeaderColumns.text = "Table is empty"
                        tableHeaderColumns.textSize = 30f
                        tableHeaderColumns.setTextColor(Color.RED)
                        cell.addView(tableHeaderColumns)

                        val tableHeader = TableRow(this@DBViewerActivity)
                        tableHeader.setBackgroundColor(Color.BLACK)
                        tableHeader.setPadding(0, 2, 0, 2)
                        tableHeader.addView(cell)
                        tableLayout?.addView(tableHeader)
                        textView?.text = "0"
                    }
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }


    //get column names of the empty tables and save them in a array list
    fun getColumnNames() {
        val cursor = getData("PRAGMA table_info($tableName)")[0]
        isEmpty = true
        if (cursor != null) {
            isEmpty = true
            val emptyTableColumnNames = ArrayList<String>()
            cursor.moveToFirst()
            do {
                emptyTableColumnNames.add(cursor.getString(1))
            } while (cursor.moveToNext())
            this.emptyTableColumnNames = emptyTableColumnNames
        }
    }


    //displays alert dialog from which use can update or delete a row
    private fun updateDeletePopup() {
        val cursor = mainCursor
        // a spinner which gives options to update or delete the row which user has selected
        val spinnerArray = ArrayList<String>()
        spinnerArray.add("Click Here to Change this row")
        spinnerArray.add("Update this row")
        spinnerArray.add("Delete this row")

        //create a layout with text values which has the column names and
        //edit texts which has the values of the row which user has selected
        val valueStringLocal = valueString
        val columnNames = LinkedList<TextView>()
        val columnValues = LinkedList<EditText>()

        for (i in 0 until cursor!!.columnCount) {
            val cname = cursor.getColumnName(i)
            val tv = TextView(this)
            tv.text = cname
            columnNames.add(tv)
        }
        for (i in columnNames.indices) {
            val cv = valueStringLocal?.get(i)
            val et = EditText(this)
            valueStringLocal?.add(cv)
            et.setText(cv)
            columnValues.add(et)
        }

        val lastRId = 0
        // all text views , edit texts are added to this relative layout lp

        val scrollView = ScrollView(this)
        val linearLayout = LinearLayout(this)
        val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        linearLayoutParams.setMargins(0, 20, 0, 0)

        //spinner which displays update , delete options
        val crudAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setBackgroundColor(Color.WHITE)
                (view as TextView).textSize = 20f
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                view.setBackgroundColor(Color.WHITE)
                return view
            }
        }
        crudAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val crudDropdown = Spinner(this)
        crudDropdown.adapter = crudAdapter
        linearLayout.id = 299
        linearLayout.addView(crudDropdown, linearLayoutParams)

        val relativeLayout = RelativeLayout(this)
        relativeLayout.setBackgroundColor(Color.WHITE)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.BELOW, lastRId)
        relativeLayout.addView(linearLayout, params)

        for (i in columnNames.indices) {
            val tv = columnNames[i]
            val et = columnValues[i]
            val t = i + 100
            val k = i + 200
            val lid = i + 300

            tv.id = t
            tv.setTextColor(Color.parseColor("#000000"))
            et.setBackgroundColor(Color.parseColor("#F2F2F2"))

            et.setTextColor(Color.parseColor("#000000"))
            et.id = k
            Log.d("text View Value", "" + tv.text.toString())
            val ll = LinearLayout(this)
            ll.setBackgroundColor(Color.parseColor("#FFFFFF"))
            ll.id = lid
            val lpp = LinearLayout.LayoutParams(0, 100)
            lpp.weight = 1f
            tv.layoutParams = lpp
            et.layoutParams = lpp
            ll.addView(tv)
            ll.addView(et)

            Log.d("Edit Text Value", "" + et.text.toString())

            val rll = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            rll.addRule(RelativeLayout.BELOW, ll.id - 1)
            rll.setMargins(0, 20, 0, 0)
            relativeLayout.addView(ll, rll)
        }

        scrollView.addView(relativeLayout)
        //after the layout has been created display it in a alert dialog
        runOnUiThread {
            if (!isFinishing) {
                //this code will be executed when user changes values of edit text or spinner and clicks on ok button
                AlertDialog.Builder(this)
                        .setTitle("values")
                        .setView(scrollView)
                        .setCancelable(false)
                        .setPositiveButton("Ok") { _, _ ->
                            //get spinner value
                            val spinnerValue = crudDropdown.selectedItem.toString()

                            //it he spinner value is update this row get the values from
                            //edit text fields generate a update query and execute it
                            if (spinnerValue.equals("Update this row", ignoreCase = true)) {
                                var query = "UPDATE $tableName SET "
                                for (i in columnNames.indices) {
                                    val tvc = columnNames[i]
                                    val etc = columnValues[i]

                                    if (etc.text.toString() != "null") {
                                        query = query + tvc.text.toString() + " = "
                                        query = if (i == columnNames.size - 1)
                                            query + "'" + etc.text.toString() + "'"
                                        else
                                            query + "'" + etc.text.toString() + "' , "
                                    }
                                }
                                query = "$query where "
                                for (i in columnNames.indices) {
                                    val tvc = columnNames[i]
                                    if (valueStringLocal?.get(i) != "null") {
                                        query = query + tvc.text.toString() + " = "
                                        query = if (i == columnNames.size - 1)
                                            query + "'" + valueStringLocal?.get(i) + "' "
                                        else
                                            query + "'" + valueStringLocal?.get(i) + "' and "
                                    }
                                }
                                Log.d("Update query", query)
                                val cursor = getData(query)[1]
                                cursor?.moveToLast()
                                Log.d("Update Message", cursor?.getString(0))

                                if (cursor?.getString(0).equals("Success", ignoreCase = true)) {
                                    tvMessage?.setBackgroundColor(Color.parseColor("#2ecc71"))
                                    tvMessage?.text = "$tableName table Updated Successfully"
                                    refreshTable(0)
                                } else {
                                    tvMessage?.setBackgroundColor(Color.parseColor("#e74c3c"))
                                    tvMessage?.text = "Error:" + cursor?.getString(0)
                                }
                            }
                            //it he spinner value is delete this row get the values from
                            //edit text fields generate a delete query and execute it
                            if (spinnerValue.equals("Delete this row", ignoreCase = true)) {
                                var query = "DELETE FROM $tableName WHERE "
                                for (i in columnNames.indices) {
                                    val tvc = columnNames[i]
                                    if (valueStringLocal?.get(i) != "null") {
                                        query = query + tvc.text.toString() + " = "
                                        query = if (i == columnNames.size - 1)
                                            query + "'" + valueStringLocal?.get(i) + "' "
                                        else
                                            query + "'" + valueStringLocal?.get(i) + "' and "
                                    }
                                }
                                Log.d("Delete Query", query)
                                val cursor = getData(query)[1]
                                cursor?.moveToLast()
                                Log.d("Update Message", cursor?.getString(0))

                                if (cursor?.getString(0).equals("Success", ignoreCase = true)) {
                                    tvMessage?.setBackgroundColor(Color.parseColor("#2ecc71"))
                                    tvMessage?.text = "Row deleted from $tableName table"
                                    refreshTable(0)
                                } else {
                                    tvMessage?.setBackgroundColor(Color.parseColor("#e74c3c"))
                                    tvMessage?.text = "Error:" + cursor?.getString(0)
                                }
                            }
                        }
                        .setNegativeButton("close") { _, _ -> }
                        .create().show()
            }
        }
    }


    fun refreshTable(d: Int) {
        var cursor: Cursor? = null
        tableLayout?.removeAllViews()
        if (d == 0) {
            cursor = getData("select * from $tableName")[0]
            mainCursor = cursor
        }
        if (d == 1) cursor = mainCursor
        // if the cursor returned form tha database is not null we display the data in table layout
        if (cursor != null) {
            textView?.text = cursor.count.toString()
            val tableHeader = TableRow(this)

            tableHeader.setBackgroundColor(Color.BLACK)
            tableHeader.setPadding(0, 2, 0, 2)
            for (i in 0 until cursor.columnCount) {
                val cell = LinearLayout(this)
                cell.setBackgroundColor(Color.WHITE)
                cell.layoutParams = tableRowParams
                val tableHeaderColumns = TextView(this)
                tableHeaderColumns.setPadding(0, 0, 4, 3)
                tableHeaderColumns.text = "" + cursor.getColumnName(i)
                tableHeaderColumns.setTextColor(Color.parseColor("#000000"))
                cell.addView(tableHeaderColumns)
                tableHeader.addView(cell)
            }
            tableLayout?.addView(tableHeader)
            cursor.moveToFirst()

            //after displaying column names in the first row  we display data in the remaining columns
            //the below paginate table function will display the first 10 tuples of the tables
            //the remaining tuples can be viewed by clicking on the next button
            paginateTable()
        } else {
            val tableHeader = TableRow(this)
            tableHeader.setBackgroundColor(Color.BLACK)
            tableHeader.setPadding(0, 2, 0, 2)

            val cell = LinearLayout(this)
            cell.setBackgroundColor(Color.WHITE)
            cell.layoutParams = tableRowParams

            val tableHeaderColumns = TextView(this)
            tableHeaderColumns.setPadding(0, 0, 4, 3)
            tableHeaderColumns.text = "Table is empty"
            tableHeaderColumns.textSize = 30f
            tableHeaderColumns.setTextColor(Color.RED)

            cell.addView(tableHeaderColumns)
            tableHeader.addView(cell)
            tableLayout?.addView(tableHeader)
            textView?.text = "0"
        }
    }


    //the function which displays tuples from database in a table layout
    fun paginateTable() {
        if (tableLayout == null) return
        val cursor = mainCursor
        numberOfPages = cursor!!.count / ROWS_PER_PAGE + 1
        currentPage = 1
        cursor.moveToFirst()
        var currentRow = 0
        //display the first n tuples of the table selected by user
        do {
            val tableRow = TableRow(this)
            tableRow.setBackgroundColor(Color.BLACK)
            tableRow.setPadding(0, 2, 0, 2)

            for (i in 0 until cursor.columnCount) {
                val cell = LinearLayout(this)
                cell.setBackgroundColor(Color.WHITE)
                cell.layoutParams = tableRowParams
                val columnsView = TextView(this)
                var columnData = ""
                try {
                    columnData = cursor.getString(i)
                } catch (e: Exception) {
                    // Column data is not a string , do not display it
                }

                columnsView.text = columnData
                columnsView.setTextColor(Color.parseColor("#000000"))
                columnsView.setPadding(0, 0, 4, 3)
                cell.addView(columnsView)
                tableRow.addView(cell)
            }

            tableRow.visibility = View.VISIBLE
            currentRow += 1
            //we create listener for each table row when clicked a alert dialog will be displayed
            //from where user can update or delete the row
            tableRow.setOnClickListener {
                val valueString = ArrayList<String?>()
                for (i in 0 until cursor.columnCount) {
                    val column = tableRow.getChildAt(i) as LinearLayout
                    val tc = column.getChildAt(0) as TextView
                    val cv = tc.text.toString()
                    valueString.add(cv)
                }
                this.valueString = valueString
                //the below function will display the alert dialog
                updateDeletePopup()
            }
            tableLayout!!.addView(tableRow)
        } while (cursor.moveToNext() && currentRow < ROWS_PER_PAGE)

        // when user clicks on the previous button update the table with the previous n tuples from the database
        previous?.setOnClickListener {
            val toBeStartIndex = (currentPage - 2) * ROWS_PER_PAGE
            //if the table layout has the first n tuples then toast that this is the first page
            if (currentPage == 1) {
                Toast.makeText(this, "This is the first page", Toast.LENGTH_SHORT).show()
            } else {
                currentPage -= 1
                cursor.moveToPosition(toBeStartIndex)

                var decider = true
                for (i in 1 until tableLayout!!.childCount) {
                    val tableRow = tableLayout!!.getChildAt(i) as TableRow
                    if (decider) {
                        tableRow.visibility = View.VISIBLE
                        for (j in 0 until tableRow.childCount) {
                            val column = tableRow.getChildAt(j) as LinearLayout
                            val columnsView = column.getChildAt(0) as TextView
                            columnsView.text = "" + cursor.getString(j)
                        }
                        decider = !cursor.isLast
                        if (!cursor.isLast) cursor.moveToNext()
                    } else {
                        tableRow.visibility = View.GONE
                    }
                }
            }
        }

        // when user clicks on the next button update the table with the next n tuples from the database
        next?.setOnClickListener {
            //if there are no tuples to be shown toast that this the last page
            if (currentPage >= numberOfPages) {
                Toast.makeText(this, "This is the last page", Toast.LENGTH_SHORT).show()
            } else {
                currentPage += 1
                var decider = true
                for (i in 1 until tableLayout!!.childCount) {
                    val tableRow = tableLayout!!.getChildAt(i) as TableRow
                    if (decider) {
                        tableRow.visibility = View.VISIBLE
                        for (j in 0 until tableRow.childCount) {
                            val column = tableRow.getChildAt(j) as LinearLayout
                            val columnsView = column.getChildAt(0) as TextView
                            columnsView.text = "" + cursor.getString(j)
                        }
                        decider = !cursor.isLast
                        if (!cursor.isLast) cursor.moveToNext()
                    } else {
                        tableRow.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun getData(Query: String): ArrayList<Cursor?> {
        val columns = arrayOf("message")
        //an array list of cursor to save two cursors. one has results from the query
        //other stores error message if any errors are triggered
        val cursors = ArrayList<Cursor?>(2)
        val matrixCursor = MatrixCursor(columns)
        cursors.add(null)
        cursors.add(null)

        val cursor: Cursor?
        try {
            cursor = dbm?.writableDatabase?.rawQuery(Query, null)
            matrixCursor.addRow(arrayOf<Any>("Success"))
            cursors[1] = matrixCursor
            if (null != cursor && cursor.count > 0) {
                cursors[0] = cursor
                cursor.moveToFirst()
                return cursors
            }
            return cursors
        } catch (e: SQLException) {
            Logger.d("printing exception", e.message)
            matrixCursor.addRow(arrayOf<Any>("" + e.message))
            cursors[1] = matrixCursor
            return cursors
        } catch (e: Exception) {
            Logger.d("printing exception", e.message)
            matrixCursor.addRow(arrayOf<Any>("" + e.message))
            cursors[1] = matrixCursor
            return cursors
        }
    }


    companion object {
        private const val ROWS_PER_PAGE = 50

        fun newInstance(context: Context) {
            val intent = Intent(context, DBViewerActivity::class.java)
            context.startActivity(intent)
        }
    }
}