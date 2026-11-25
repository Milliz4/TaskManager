package com.example.taskmanager

import android.content.Context

/**
 * Единая точка доступа к списку задач.
 * Все изменения (add / update / removeAt / replaceAll) сразу пишутся в TXT (S3).
 */
object TaskRepository {

    val manager = TaskManager()

    private var appContext: Context? = null

    /** Вызывается в MainActivity.onCreate: READ из TXT (S3) */
    fun init(context: Context) {
        appContext = context.applicationContext
        manager.clear()
        TxtStorage.read(context.applicationContext)
            .forEach { manager.add(it.name, it.important) }
    }

    /** CREATE: в память + в файл */
    fun add(name: String, important: Boolean): Boolean {
        if (!manager.add(name, important)) return false
        val ctx = appContext ?: return true
        TxtStorage.add(ctx, manager.all().last())
        return true
    }

    /** UPDATE: в память + в файл */
    fun update(index: Int, name: String, important: Boolean): Boolean {
        if (!manager.update(index, name, important)) return false
        val ctx = appContext ?: return true
        TxtStorage.update(ctx, index, manager.all()[index])
        return true
    }

    /** DELETE: из памяти + из файла + счётчик удалений в SharedPreferences (S1) */
    fun removeAt(index: Int): Boolean {
        if (!manager.removeAt(index)) return false
        val ctx = appContext ?: return true
        TxtStorage.delete(ctx, index)
        Prefs.incDeleted(ctx)
        return true
    }

    /** Заменить весь список (восстановление из бинарного бэкапа, S11) */
    fun replaceAll(tasks: List<Task>) {
        manager.clear()
        tasks.forEach { manager.add(it.name, it.important) }
        appContext?.let { TxtStorage.save(it, manager.all()) }
    }
}