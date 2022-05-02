package com.todo.vincent.vuagnat.form

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.todo.vincent.vuagnat.R
import com.todo.vincent.vuagnat.tasklist.Task
import java.util.*

class FormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        val task = intent.getSerializableExtra("task") as? Task
        val newTitle = findViewById<EditText>(R.id.form_editText_title)
        val newDescription = findViewById<EditText>(R.id.form_editText_description)
        newTitle.setText(task?.title)
        newDescription.setText(task?.description)
        val id = task?.id ?: UUID.randomUUID().toString()
        findViewById<FloatingActionButton>(R.id.fab_form).setOnClickListener {

            val newTask = Task(id = id,
                title = newTitle.text.toString(),
                description = newDescription.text.toString())
            intent.putExtra("task", newTask)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}