package com.example.taskmanager

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * S11: бинарный файл в общем хранилище — Download/tasks_backup.bin.
 * Пишем через DataOutputStream: количество, затем пары (строка, флаг).
 */
object BinStorage {

    private const val NAME = "tasks_backup.bin"
    private const val MIME = "application/octet-stream"

    /** WRITE / UPDATE: запись всего списка (бинарник хранит снимок целиком) */
    fun save(ctx: Context, tasks: List<Task>): Boolean =
        SharedFiles.write(ctx, NAME, MIME) { out ->
            val d = DataOutputStream(BufferedOutputStream(out))
            d.writeInt(tasks.size)
            tasks.forEach {
                d.writeUTF(it.name)      // строка с префиксом длины
                d.writeBoolean(it.important)
            }
            d.flush()
        }

    /** READ: чтение всего списка */
    fun read(ctx: Context): MutableList<Task> {
        val list = mutableListOf<Task>()
        SharedFiles.read(ctx, NAME) { input ->
            val d = DataInputStream(BufferedInputStream(input))
            val count = try {
                d.readInt()
            } catch (e: Exception) {
                0
            }
            repeat(count) {
                list.add(Task(d.readUTF(), d.readBoolean()))
            }
        }
        return list
    }

    /** DELETE: удалить файл бэкапа */
    fun deleteFile(ctx: Context): Boolean = SharedFiles.delete(ctx, NAME)
}