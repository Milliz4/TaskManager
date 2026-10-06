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

    // @return true — задача с таким номером удалена, false — номера нет
    fun remove(index: Int): Boolean {
        if (index < 1 || index > tasks.size) return false
        tasks.removeAt(index - 1)   // пользователь вводит 1, 2, 3 — а список с нуля
        return true
    }

    fun clear() {
        tasks.clear()
    }

    fun all(): List<Task> = tasks.toList()
    fun count(): Int = tasks.size

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

    fun removeByName(name: String): Boolean {
        return tasks.removeAll { it.name.equals(name.trim(), ignoreCase = true) }
    }
}