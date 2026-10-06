package com.example.taskmanager

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val manager = TaskManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.editTask)
        val checkImportant = findViewById<CheckBox>(R.id.checkImportant)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val output = findViewById<TextView>(R.id.textOutput)

        btnAdd.setOnClickListener {
            val name = input.text.toString()
            val important = checkImportant.isChecked

            if (manager.add(name, important)) {
                output.text = manager.render()
                input.setText("")
                checkImportant.isChecked = false
            } else {
                Toast.makeText(this, "Задача не может быть пустой",
                    Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener {
            manager.clear()
            output.text = manager.render()
        }

        val btnRemove = findViewById<Button>(R.id.btnRemove)

        btnAdd.setOnClickListener {
            val text = input.text.toString()
            if (manager.add(text, checkImportant.isChecked)) {
                output.text = manager.render()
                input.setText("")
                checkImportant.isChecked = false
            } else {
                Toast.makeText(this, "Задача не может быть пустой", Toast.LENGTH_SHORT).show()
            }
        }

        btnRemove.setOnClickListener {
            val raw = input.text.toString().trim()

            // пользователь ввёл номер — удаляем одну задачу
            val num = raw.toIntOrNull()
            if (num != null) {
                if (manager.remove(num)) {
                    output.text = manager.render()
                    input.setText("")
                } else {
                    Toast.makeText(this, "Нет задачи с номером $num", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            // введён текст — ищем задачу с таким названием
            if (raw.isEmpty()) {
                Toast.makeText(this, "Введите номер удаляемой задачи", Toast.LENGTH_SHORT).show()
            } else if (manager.removeByName(raw)) {
                output.text = manager.render()
                input.setText("")
            } else {
                Toast.makeText(this, "Задача «$raw» не найдена", Toast.LENGTH_SHORT).show()
            }
        }
    }
}