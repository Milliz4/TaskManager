package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        recycler = findViewById(R.id.taskList)
        emptyView = findViewById(R.id.emptyView)

        // адаптер сообщает позицию, активность решает, что делать
        adapter = TaskAdapter { position -> openEditTask(position) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        //  создание отдельной кнопкой
        findViewById<Button>(R.id.btnAdd).setOnClickListener { openEditTask() }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<NavigationView>(R.id.navigationView)
            .setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_tasks -> refresh()
                    R.id.nav_add   -> openEditTask()
                    R.id.nav_stats -> openStats()
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        attachSwipeToDelete()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /** Перечитать список из хранилища и перерисовать экран. */
    private fun refresh() {
        val list = TaskRepository.manager.all()
        adapter.submit(list)

        val empty = list.isEmpty()
        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        recycler.visibility = if (empty) View.GONE else View.VISIBLE
    }

    /** удаление свайпом влево или вправо*/
    private fun attachSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                android.util.Log.d("SWIPE", "position=$position")

                if (position == RecyclerView.NO_POSITION)
                {
                    refresh()
                    return
                }

                if (TaskRepository.manager.removeAt(position)) {
                    refresh()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.task_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recycler)
    }

    /**
     * Без аргумента = создание новой задачи
     * С аргументом = изменение задачи по позиции
     */
    private fun openEditTask(position: Int = EditTaskActivity.NO_POSITION) {
        val intent = Intent(this, EditTaskActivity::class.java)
        intent.putExtra(
            EditTaskActivity.EXTRA_TASK_NUMBER,
            TaskRepository.manager.count() + 1
        )
        intent.putExtra(
            EditTaskActivity.EXTRA_TASK_LIST,
            TaskRepository.manager.all().map { it.name }.toTypedArray()
        )
        intent.putExtra(EditTaskActivity.EXTRA_EDIT_POSITION, position)
        startActivity(intent)
    }

    private fun openStats() {
        val intent = Intent(this, StatsActivity::class.java)
        intent.putExtra(StatsActivity.EXTRA_TOTAL, TaskRepository.manager.count())
        intent.putExtra(StatsActivity.EXTRA_IMPORTANT, TaskRepository.manager.importantCount())
        startActivity(intent)
    }
}