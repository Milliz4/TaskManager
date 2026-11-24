package com.example.taskmanager

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class EditTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_NUMBER   = "extra_task_number"
        const val EXTRA_TASK_LIST     = "extra_task_list"
        const val EXTRA_EDIT_POSITION = "extra_edit_position"
        const val NO_POSITION = -1
    }

    /** -1 — создание новой задачи, иначе позиция изменяемой. */
    private var editPosition = NO_POSITION

    private lateinit var input: EditText
    private lateinit var important: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // Тулбар: если в разметке он есть — подключаем и добавляем стрелку «назад»
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener { finish() }
        }

        val title    = findViewById<TextView>(R.id.editTitle)
        val existing = findViewById<TextView>(R.id.existingText)
        input        = findViewById(R.id.inputName)
        important    = findViewById(R.id.checkImportant)
        val save     = findViewById<Button>(R.id.btnSave)

        editPosition = intent.getIntExtra(EXTRA_EDIT_POSITION, NO_POSITION)

        if (editPosition >= 0) {
            // ===== режим ИЗМЕНЕНИЯ (U1) =====
            val task = TaskRepository.manager.get(editPosition)
            if (task == null) {
                // задача исчезла — возвращаемся, чтобы не работать с пустотой
                finish()
                return
            }

            title.text = getString(R.string.title_edit_task)
            input.setText(task.name)
            input.setSelection(task.name.length)   // курсор в конец
            important.isChecked = task.important
            save.setText(R.string.btn_save_changes)
            existing.visibility = View.GONE         // список здесь не нужен

        } else {
            // ===== режим СОЗДАНИЯ (C1) — как в лабе №4 =====
            val number = intent.getIntExtra(
                EXTRA_TASK_NUMBER,
                TaskRepository.manager.count() + 1
            )
            val list = intent.getStringArrayExtra(EXTRA_TASK_LIST) ?: emptyArray()

            title.text = getString(R.string.title_add_task_format, number)
            existing.text = if (list.isEmpty()) {
                getString(R.string.list_empty)
            } else {
                list.joinToString("\n") { "• $it" }
            }
            existing.visibility = View.VISIBLE
        }

        save.setOnClickListener {
            val name = input.text.toString()
            val ok = if (editPosition >= 0) {
                TaskRepository.manager.update(editPosition, name, important.isChecked)
            } else {
                TaskRepository.manager.add(name, important.isChecked)
            }

            if (ok) {
                finish()
            } else {
                Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show()
            }
        }
    }
}