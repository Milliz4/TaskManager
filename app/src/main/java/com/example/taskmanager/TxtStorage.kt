package com.example.taskmanager

import android.content.Context

/**
 * S3: TXT-файл в общем хранилище — Download/tasks.txt.
 * Формат человекочитаемый: одна задача = одна строка,
 * важные помечаются префиксом "Важно".
 */
object TxtStorage {

    private const val NAME = "tasks.txt"
    private const val MIME = "text/plain"
    private const val MARK = "[Важно] "

    /** WRITE: запись всего списка (полная перезапись файла) */
    fun save(ctx: Context, tasks: List<Task>): Boolean =
        SharedFiles.write(ctx, NAME, MIME) { out ->
            val w = out.bufferedWriter()
            tasks.forEach { t ->
                val line = t.name.replace("\n", " ").replace("\r", " ")
                w.write(if (t.important) MARK + line else line)
                w.newLine()
            }
            w.flush()
        }

    /** READ: чтение всего списка */
    fun read(ctx: Context): MutableList<Task> {
        val list = mutableListOf<Task>()
        SharedFiles.read(ctx, NAME) { input ->
            input.bufferedReader().forEachLine { raw ->
                val line = raw.trim()
                if (line.isEmpty()) return@forEachLine

                val important = line.startsWith(MARK)
                val name = if (important) line.removePrefix(MARK).trim() else line
                if (name.isNotEmpty()) list.add(Task(name, important))
            }
        }
        return list
    }

    /** CREATE одной записи: читаем файл, дописываем задачу в конец */
    fun add(ctx: Context, task: Task): Boolean {
        val list = read(ctx)
        list.add(task)
        return save(ctx, list)
    }

    /** UPDATE одной записи по позиции */
    fun update(ctx: Context, index: Int, task: Task): Boolean {
        val list = read(ctx)
        if (index !in list.indices) return false
        list[index] = task
        return save(ctx, list)
    }

    /** DELETE одной записи по позиции */
    fun delete(ctx: Context, index: Int): Boolean {
        val list = read(ctx)
        if (index !in list.indices) return false
        list.removeAt(index)
        return save(ctx, list)
    }

    /** DELETE: удалить файл целиком */
    fun deleteFile(ctx: Context): Boolean = SharedFiles.delete(ctx, NAME)
}