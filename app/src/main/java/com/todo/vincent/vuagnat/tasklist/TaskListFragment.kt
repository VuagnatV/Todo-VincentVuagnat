package com.todo.vincent.vuagnat.tasklist

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.todo.vincent.vuagnat.R
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.todo.vincent.vuagnat.databinding.FragmentTaskListBinding
import com.todo.vincent.vuagnat.form.FormActivity
import com.todo.vincent.vuagnat.network.Api
import com.todo.vincent.vuagnat.network.TasksListViewModel
import com.todo.vincent.vuagnat.user.UserInfoActivity
import kotlinx.coroutines.launch
import java.util.*

class TaskListFragment : Fragment() {

    val formLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = result.data?.getSerializableExtra("task") as? Task ?: return@registerForActivityResult
        //taskList = taskList + task
        //adapter.submitList(taskList)
        viewModel.create(task)
    }
    val formLauncherEdit = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = result.data?.getSerializableExtra("task") as? Task ?: return@registerForActivityResult
        //taskList = taskList + task
        //adapter.submitList(taskList)
        viewModel.update(task)
    }

    val userInfoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        //val task = result.data?.getSerializableExtra("task") as? Task ?: return@registerForActivityResult
        //taskList = taskList + task
        //adapter.submitList(taskList)
    }

    private val viewModel: TasksListViewModel by viewModels()

    /*private var taskList = listOf(
        Task(id = "id_1", title = "Task 1", description = "description 1"),
        Task(id = "id_2", title = "Task 2"),
        Task(id = "id_3", title = "Task 3")
    )*/
    private val adapter = TaskListAdapter()

    private lateinit var binding: FragmentTaskListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskListBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        /*binding.floatingActionButton.setOnClickListener {
            val newTask = Task(id = UUID.randomUUID().toString(), title = "Task ${taskList.size + 1}")
            taskList = taskList + newTask
            adapter.submitList(taskList)
        }*/
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(context, FormActivity::class.java)
            formLauncher.launch(intent)
        }
        adapter.onClickDelete = { task ->
            //taskList = taskList - task
            //adapter.submitList(taskList)
            viewModel.delete(task)
        }

        binding.avatar.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java)
            userInfoLauncher.launch(intent)
        }

        adapter.onClickUpdate = { task ->
            val intent = Intent(context, FormActivity::class.java)
            intent.putExtra("task", task)
            formLauncherEdit.launch(intent)
        }

        lifecycleScope.launch {
            viewModel.tasksStateFlow.collect { newList ->
                adapter.submitList(newList)
            }}

    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {

            val userInfo = Api.userWebService.getInfo().body()!!
            val avatar = requireView().findViewById<ImageView>(R.id.avatar)
            avatar.load("https://goo.gl/gEgYUd") {
                transformations(CircleCropTransformation())
            }
            binding.textView.text = "${userInfo.firstName} ${userInfo.lastName}"
            viewModel.refresh()
        }

    }

}