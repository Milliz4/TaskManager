package com.example.taskmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: TaskAdapter

    /** Android 8–9: запрос разрешения на запись в общую память (на 10+ не нужен) */
    private val storagePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                toast("Без доступа к памяти файлы сохранить не получится")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        recycler = findViewById(R.id.taskList)
        emptyView = findViewById(R.id.emptyView)

        // ===== ЛАБА 6: READ из TXT при старте (S3) + счётчик запусков (S1) =====
        TaskRepository.init(this)
        Prefs.onOpen(this)
        requestStoragePermissionIfNeeded()

        adapter = TaskAdapter { position -> openEditTask(position) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnAdd).setOnClickListener { openEditTask() }

        // ===== ЛАБА 6: кнопки хранилищ =====
        findViewById<Button>(R.id.btnTxt).setOnClickListener { exportTxt() }
        findViewById<Button>(R.id.btnBin).setOnClickListener { backupBin() }
        findViewById<Button>(R.id.btnRestore).setOnClickListener { restoreBin() }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<NavigationView>(R.id.navigationView)
            .setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_tasks -> refresh()
                    R.id.nav_add -> openEditTask()
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

    // ================= ЛАБА 6 =================

    /** На Android 8–9 без этого разрешения запись в Download не пройдёт */
    private fun requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    /** Есть ли доступ к общей памяти (на 10+ всегда да, там MediaStore) */
    private fun hasStorageAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

    /** S3: записать текущий список в Download/tasks.txt */
    private fun exportTxt() {
        if (!hasStorageAccess()) {
            requestStoragePermissionIfNeeded()
            toast("Сначала разрешите доступ к памяти")
            return
        }
        val ok = TxtStorage.save(this, TaskRepository.manager.all())
        toast(if (ok) "Записано в Download/tasks.txt" else "Не удалось записать файл")
    }

    /** S11: бинарный бэкап + отметка времени в SharedPreferences (S1) */
    private fun backupBin() {
        if (!hasStorageAccess()) {
            requestStoragePermissionIfNeeded()
            toast("Сначала разрешите доступ к памяти")
            return
        }
        val ok = BinStorage.save(this, TaskRepository.manager.all())
        if (ok) {
            Prefs.setLastBackup(
                this,
                SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())
            )
            toast("Бэкап сохранён: Download/tasks_backup.bin")
        } else {
            toast("Не удалось записать бэкап")
        }
        refresh()
    }

    /** S11: восстановить список из бинарного бэкапа */
    private fun restoreBin() {
        if (!hasStorageAccess()) {
            requestStoragePermissionIfNeeded()
            toast("Сначала разрешите доступ к памяти")
            return
        }
        val restored = BinStorage.read(this)
        if (restored.isEmpty()) {
            toast("Бэкап не найден или пуст")
            return
        }
        TaskRepository.replaceAll(restored)
        refresh()
        toast("Восстановлено задач: ${restored.size}")
    }

    // ================= СПИСОК =================

    /** Перечитать список, перерисовать экран, обновить данные из SharedPreferences */
    private fun refresh() {
        val list = TaskRepository.manager.all()
        adapter.submit(list)

        val empty = list.isEmpty()
        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        recycler.visibility = if (empty) View.GONE else View.VISIBLE

        findViewById<TextView>(R.id.tvInfo).text =
            "Запусков: ${Prefs.opens(this)}   |   " +
                    "Бэкап: ${Prefs.lastBackup(this)}   |   " +
                    "Задач: ${list.size}   |   " +
                    "Удалено: ${Prefs.deleted(this)}"
    }

    /** Удаление свайпом влево или вправо */
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

                if (position == RecyclerView.NO_POSITION) {
                    refresh()
                    return
                }

                if (TaskRepository.removeAt(position)) {
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

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}