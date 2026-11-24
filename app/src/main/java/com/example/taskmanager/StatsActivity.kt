package com.example.taskmanager

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class StatsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TOTAL = "extra_total"
        const val EXTRA_IMPORTANT = "extra_important"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val toolbar = findViewById<Toolbar>(R.id.toolbarStats)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val total = intent.getIntExtra(EXTRA_TOTAL, 0)
        val important = intent.getIntExtra(EXTRA_IMPORTANT, 0)
        val normal = total - important

        findViewById<TextView>(R.id.textTotal).text = "Всего задач: $total"
        findViewById<TextView>(R.id.textImportant).text = "Важных: $important"
        findViewById<TextView>(R.id.textNormal).text = "Обычных: $normal"

        findViewById<Button>(R.id.btnClose).setOnClickListener { finish() }
    }
}