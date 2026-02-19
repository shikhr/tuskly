package com.example.minimaltodo.data.repository

import com.example.minimaltodo.data.fake.FakeTaskDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    private lateinit var taskDao: FakeTaskDao
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        taskDao = FakeTaskDao()
        repository = TaskRepository(taskDao)
    }

    @Test
    fun addTask_appearsInActiveTasks() = runTest {
        repository.addTask("Buy groceries")

        val tasks = repository.observeActiveTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Buy groceries", tasks[0].title)
    }

    @Test
    fun completeTask_movesToCompleted() = runTest {
        repository.addTask("Buy groceries")
        val task = repository.observeActiveTasks().first().first()

        repository.completeTask(task)

        val active = repository.observeActiveTasks().first()
        val completed = repository.observeCompletedTasks().first()
        assertTrue(active.isEmpty())
        assertEquals(1, completed.size)
        assertTrue(completed[0].isCompleted)
        assertNotNull(completed[0].completedAt)
    }

    @Test
    fun uncompleteTask_movesBackToActive() = runTest {
        repository.addTask("Buy groceries")
        val task = repository.observeActiveTasks().first().first()
        repository.completeTask(task)

        val completedTask = repository.observeCompletedTasks().first().first()
        repository.uncompleteTask(completedTask)

        val active = repository.observeActiveTasks().first()
        val completed = repository.observeCompletedTasks().first()
        assertEquals(1, active.size)
        assertTrue(completed.isEmpty())
        assertNull(active[0].completedAt)
    }

    @Test
    fun deleteTask_removesFromList() = runTest {
        repository.addTask("Buy groceries")
        val task = repository.observeActiveTasks().first().first()

        repository.deleteTask(task)

        val tasks = repository.observeActiveTasks().first()
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun addMultipleTasks_allAppear() = runTest {
        repository.addTask("Task 1")
        repository.addTask("Task 2")
        repository.addTask("Task 3")

        val tasks = repository.observeActiveTasks().first()
        assertEquals(3, tasks.size)
    }
}
