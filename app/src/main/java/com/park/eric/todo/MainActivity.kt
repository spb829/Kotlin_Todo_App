package com.park.eric.todo

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.park.eric.todo.model.Todo
import io.realm.*
import io.realm.kotlin.where


import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_main.view.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    var list: MutableList<ItemVO> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        realm = Realm.getDefaultInstance()

        selectDB()

        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivityForResult(intent, 10)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==10 && resultCode==Activity.RESULT_OK) {
            selectDB()
        }
    }

    class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val headerView = view.itemHeaderView
    }

    class DataViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val completedIconView = view.completedIconView
        val itemTitleView = view.itemTitleView
        val itemContentsView = view.itemContentsView
    }

    inner class MyAdapter(val list: MutableList<ItemVO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return list.get(position).type
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            if (p1 == ItemVO.TYPE_HEADER) {
                val layoutInflater = LayoutInflater.from(p0.context)
                return HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, p0, false))
            } else {
                val layoutInflater = LayoutInflater.from(p0.context)
                return DataViewHolder(layoutInflater.inflate(R.layout.item_main, p0, false))
            }
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val itemVO = list[p1]

            lateinit var viewHolder: RecyclerView.ViewHolder
            lateinit var dataItem: ItemVO

            if (itemVO.type == ItemVO.TYPE_HEADER) {
                viewHolder = p0 as HeaderViewHolder
                dataItem = itemVO as HeaderItem
                viewHolder.headerView.text = dataItem.date
            } else {
                viewHolder = p0 as DataViewHolder
                dataItem = itemVO as DataItem
                viewHolder.itemTitleView.text = dataItem.title
                viewHolder.itemContentsView.text = dataItem.contents
                if (dataItem.completed) {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                } else {
                    viewHolder.completedIconView.setImageResource(R.drawable.icon)
                }

                viewHolder.completedIconView.setOnClickListener {
                    realm.executeTransaction { realm ->
                        var target: Todo?
                        if (dataItem.completed) {
                            target = realm.where<Todo>().equalTo(Todo::id.name, dataItem.id).findFirst()
                            viewHolder.completedIconView.setImageResource(R.drawable.icon)
                        } else {
                            target = realm.where<Todo>().equalTo(Todo::id.name, dataItem.id).findFirst()
                            viewHolder.completedIconView.setImageResource(R.drawable.icon_completed)
                        }
                        target?.completed = target!!.completed
                    }
                }
            }


        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    inner class MyDecoration() : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent.getChildAdapterPosition(view)
            val itemVO = list[index]

            if (itemVO.type == ItemVO.TYPE_DATA) {
                view.setBackgroundColor(0xFFFFFF.toInt())
                ViewCompat.setElevation(view, 10.0f)
            }

            outRect.set(20, 10, 20, 10)
        }
    }

    fun selectDB(){
        list = mutableListOf()
        val results = realm.where<Todo>().sort(Todo::date.name,Sort.ASCENDING).findAll()

        var preDate: Date? = null

        for (item in results) {
            if(!item.date.equals(preDate)) {
                val headerItem = HeaderItem(item.date.toString())
                list.add(headerItem)
                preDate=item.date
            }

            val dataItem = DataItem(item.id, item.title, item.contents, item.completed)
            list.add(dataItem)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(list)
        recyclerView.addItemDecoration(MyDecoration())
    }
}

abstract class ItemVO {
    abstract val type: Int
    companion object {
        val TYPE_HEADER = 0
        val TYPE_DATA = 1
    }
}
class HeaderItem(var date: String): ItemVO() {
    override val type: Int
        get() = ItemVO.TYPE_HEADER
}

internal class DataItem(var id: Long, var title: String, var contents: String, var completed: Boolean = false): ItemVO() {
    override val type: Int
        get() = ItemVO.TYPE_DATA
}