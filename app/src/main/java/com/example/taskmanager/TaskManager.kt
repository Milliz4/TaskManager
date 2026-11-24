package com.example.taskmanager

class Task(val name: String, val important: Boolean)
class TaskManager {
    private val tasks = mutableListOf<Task>()

    fun add(name: String, important: Boolean): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        tasks.add(Task(clean.take(200), important))
        return true
    }

    fun remove(index: Int): Boolean {
        if (index < 1 || index > tasks.size) return false
        tasks.removeAt(index - 1)
        return true
    }

    fun clear() {
        tasks.clear()
    }

    fun all(): List<Task> = tasks.toList()
    fun count(): Int = tasks.size
    fun importantCount(): Int = tasks.count { it.important }

    fun render(): String {
        if (tasks.isEmpty()) return "Список пуст"
        return buildString {
            tasks.forEachIndexed { i, t ->
                val mark = if (t.important) "[Важно] " else ""
                append("${i + 1}. $mark${t.name}\n")
            }
            append("\nВсего задач: ${tasks.size}")
        }
    }
    /** Задача по её позиции в списке. null, если позиции нет. */
    fun get(index: Int): Task? = tasks.getOrNull(index)

    /** Изменение задачи по позиции. false — если позиции нет или имя пустое. */
    fun update(index: Int, name: String, important: Boolean): Boolean {
        if (index !in tasks.indices) return false
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        tasks[index] = Task(trimmed, important)
        return true
    }

    /** Удаление по позиции. */
    fun removeAt(index: Int): Boolean {
        if (index !in tasks.indices) return false
        tasks.removeAt(index)
        return true
    }
}