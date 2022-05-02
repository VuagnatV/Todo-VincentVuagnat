package com.todo.vincent.vuagnat.tasklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.todo.vincent.vuagnat.R
import com.todo.vincent.vuagnat.databinding.ItemTaskBinding



object TasksDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task) : Boolean {
        return oldItem.id == newItem.id// comparison: are they the same "entity" ? (usually same id)
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task) : Boolean {
        return oldItem == newItem// comparison: are they the same "content" ? (simplified for data class)
    }
}


//class TaskListAdapter : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

class TaskListAdapter : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TasksDiffCallback) {

    var onClickDelete: (Task) -> Unit = {}

    var onClickUpdate: (Task) -> Unit = {}

    //var currentList: List<Task> = emptyList()

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            //val textView = itemView.findViewById<TextView>(R.id.task_title)
            //val textView2 = itemView.findViewById<TextView>(R.id.task_description)
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description
            binding.taskDelete.setOnClickListener {
                onClickDelete(task)

            }
            binding.taskEdit.setOnClickListener {
                onClickUpdate(task)

            }
        }
    }

    override fun getItemCount(): Int {
        return this.currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}