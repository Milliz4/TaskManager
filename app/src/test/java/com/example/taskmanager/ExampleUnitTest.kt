package com.example.taskmanager

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskManagerTest {

    private lateinit var manager: TaskManager

    @Before
    fun setUp() {
        manager = TaskManager()
    }

    @Test
    fun add_validTask_returnsTrueAndIncreasesCount() {
        assertTrue(manager.add("Купить хлеб", false))
        assertEquals(1, manager.count())
    }

    @Test
    fun add_emptyString_returnsFalse() {
        assertFalse(manager.add("", false))
        assertEquals(0, manager.count())
    }

    @Test
    fun remove_validIndex_removesTask() {
        manager.add("Первая", false)
        manager.add("Вторая", false)
        assertTrue(manager.remove(1))
        assertEquals(1, manager.count())
        assertEquals("Вторая", manager.all()[0].name)
    }

    @Test
    fun remove_indexTooBig_returnsFalse() {
        manager.add("Первая", false)
        assertFalse(manager.remove(5))
        assertEquals(1, manager.count())
    }

    @Test
    fun render_emptyList_returnsPlaceholder() {
        assertEquals("Список пуст", manager.render())
    }

    @Test fun updateChangesTask() {
        val m = TaskManager()
        m.add("старое", false)
        assertTrue(m.update(0, "новое", true))
        assertEquals("новое", m.get(0)?.name)
        assertTrue(m.get(0)!!.important)
    }

    @Test fun updateWithWrongIndexReturnsFalse() {
        assertFalse(TaskManager().update(5, "текст", false))
    }

    @Test fun updateWithBlankNameReturnsFalse() {
        val m = TaskManager()
        m.add("задача", false)
        assertFalse(m.update(0, "   ", false))
    }

    @Test fun removeAtDeletesTask() {
        val m = TaskManager()
        m.add("первая", false)
        m.add("вторая", false)
        assertTrue(m.removeAt(0))
        assertEquals(1, m.count())
        assertEquals("вторая", m.get(0)?.name)
    }
}