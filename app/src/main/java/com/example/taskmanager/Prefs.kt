package com.example.taskmanager

import android.content.Context

/** S1: SharedPreferences — счётчики и служебные отметки приложения */
object Prefs {

    private const val FILE = "lab6_prefs"
    private const val K_OPENS = "opens"
    private const val K_BACKUP = "last_backup"
    private const val K_DELETED = "deleted_total"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** UPDATE: +1 к счётчику запусков приложения */
    fun onOpen(ctx: Context) {
        val s = sp(ctx)
        s.edit().putInt(K_OPENS, s.getInt(K_OPENS, 0) + 1).apply()
    }

    /** READ */
    fun opens(ctx: Context): Int = sp(ctx).getInt(K_OPENS, 0)

    /** UPDATE: время последнего бинарного бэкапа */
    fun setLastBackup(ctx: Context, time: String) {
        sp(ctx).edit().putString(K_BACKUP, time).apply()
    }

    /** READ */
    fun lastBackup(ctx: Context): String = sp(ctx).getString(K_BACKUP, "—") ?: "—"

    /** UPDATE: +1 к счётчику удалённых задач */
    fun incDeleted(ctx: Context) {
        val s = sp(ctx)
        s.edit().putInt(K_DELETED, s.getInt(K_DELETED, 0) + 1).apply()
    }

    /** READ */
    fun deleted(ctx: Context): Int = sp(ctx).getInt(K_DELETED, 0)

    /** DELETE: очистить все значения */
    fun clear(ctx: Context) {
        sp(ctx).edit().clear().apply()
    }
}