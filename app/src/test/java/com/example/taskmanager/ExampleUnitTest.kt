package com.example.taskmanager

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskManagerTest {

    private lateinit var manager: TaskManager

    @Before
    fun setUp() {
        manager = TaskManager()   // свежий объект перед каждым тестом
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
}