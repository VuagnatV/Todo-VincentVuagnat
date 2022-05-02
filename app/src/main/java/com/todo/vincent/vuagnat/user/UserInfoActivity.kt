package com.todo.vincent.vuagnat.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.modernstorage.permissions.RequestAccess
import com.google.modernstorage.permissions.StoragePermissions
import com.google.modernstorage.storage.AndroidFileSystem
import com.todo.vincent.vuagnat.R
import com.todo.vincent.vuagnat.databinding.ActivityUserInfoBinding
import com.todo.vincent.vuagnat.databinding.FragmentTaskListBinding
import com.todo.vincent.vuagnat.form.FormActivity
import com.todo.vincent.vuagnat.network.Api
import com.todo.vincent.vuagnat.network.Api.userWebService
import com.todo.vincent.vuagnat.network.TasksListViewModel
import com.todo.vincent.vuagnat.network.UserInfo
import com.todo.vincent.vuagnat.network.UserInfoViewModel
import com.todo.vincent.vuagnat.tasklist.Task
import com.todo.vincent.vuagnat.tasklist.TaskListAdapter
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*


class UserInfoActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        photoUri = fileSystem.createMediaStoreUri(
            filename = "picture-${UUID.randomUUID()}.jpg",
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            directory = "image/*",
        )!!

        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePictureButton.setOnClickListener {
            launchCameraWithPermissions()
        }

        binding.uploadImageButton.setOnClickListener {
            openGallery()
        }

        binding.validationUser.setOnClickListener {

            viewModel.update(viewModel.userStateFlow.value.copy(
                email = binding.email.text.toString(),
                firstName = binding.firstName.text.toString(),
                lastName = binding.lastName.text.toString()
            ))
            showMessageUpdate("Profil modifié")
        }

        //binding.email.text = UserInfo.email

        lifecycleScope.launch {
            viewModel.userStateFlow.collect { userInfo ->
                binding.email.setText(userInfo.email)
                binding.firstName.setText(userInfo.firstName)
                binding.lastName.setText(userInfo.lastName)

            }
        }
        viewModel.refresh()
    }

    private val viewModel: UserInfoViewModel by viewModels()

    private fun Bitmap.toRequestBody(): MultipartBody.Part {
        val tmpFile = File.createTempFile("avatar", "jpeg")
        tmpFile.outputStream().use {
            this.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                it
            ) // this est le bitmap dans ce contexte
        }
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = tmpFile.readBytes().toRequestBody()
        )
    }

    private fun Uri.toRequestBody(): MultipartBody.Part {
        val fileInputStream = contentResolver.openInputStream(this)!!
        val fileBody = fileInputStream.readBytes().toRequestBody()
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "temp.jpeg",
            body = fileBody
        )
    }

    private lateinit var binding: ActivityUserInfoBinding


    private val getPhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap == null) {
                showMessage("error bitmap")
                return@registerForActivityResult
            } else binding.imageView.load(bitmap) // afficher

            lifecycleScope.launch {
                val response = userWebService.updateAvatar(bitmap.toRequestBody())
                val userInfo = response.body()
                Log.e("jkfshdkhf", response.raw().toString())
                binding.imageView.load(userInfo?.avatar) {
                    error(R.drawable.ic_launcher_background) // affiche une image par défaut en cas d'erreur:
                }
            }
        }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Open Settings") {
                val intent = Intent(
                    ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivity(intent)
            }
            .show()
    }

    /*@RequiresApi(Build.VERSION_CODES.M)
    private fun launchCameraWithPermission() {
        val camPermission = Manifest.permission.CAMERA
        val permissionStatus = checkSelfPermission(camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)
        when {
            isAlreadyAccepted -> getPhoto.launch()// lancer l'action souhaitée
                isExplanationNeeded -> showMessage("adadadadada")// afficher une explication
            else -> requestCamera.launch(camPermission)// lancer la demande de permission
        }
    }*/

    /*private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accepted ->
            if (accepted) getPhoto.launch()// lancer l'action souhaitée
            else showMessage("sdfsfsdfsf")
        }*/

    val requestReadAccess = registerForActivityResult(RequestAccess()) { hasAccess ->
        if (hasAccess) {
            galleryLauncher.launch("image/*")// launch gallery
        } else {
            showMessage("pipop")// message
        }
    }

    fun openGallery() {
        requestReadAccess.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.AllApps
            )
        )
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // au retour de la galerie on fera quasiment pareil qu'au retour de la caméra mais avec une URI àla place du bitmap
        binding.imageView.load(uri)
        lifecycleScope.launch {
            val response = userWebService.updateAvatar(uri!!.toRequestBody())
            val userInfo = response.body()
            Log.e("jkfshdkhf", response.raw().toString())
            binding.imageView.load(userInfo?.avatar) {
                error(R.drawable.ic_launcher_background) // affiche une image par défaut en cas d'erreur:
            }
        }
    }

    private val fileSystem by lazy { AndroidFileSystem(this) } // pour interagir avec le stockage

    private lateinit var photoUri: Uri // on stockera l'uri dans cette variable

    private val openCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        // afficher et uploader l'image enregistrée dans `photoUri`
        binding.imageView.load(photoUri) // afficher

        lifecycleScope.launch {
            val response = userWebService.updateAvatar(photoUri.toRequestBody())
            val userInfo = response.body()
            Log.e("jkfshdkhf", response.raw().toString())
            binding.imageView.load(userInfo?.avatar) {
                error(R.drawable.ic_launcher_background) // affiche une image par défaut en cas d'erreur:
            }
        }
    }

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accepted ->
            // créer et stocker l'uri:

            openCamera.launch(photoUri)
        }

    @RequiresApi(Build.VERSION_CODES.M)
    val requestWriteAccess = registerForActivityResult(RequestAccess()) { accepted ->
        // utiliser le code précédent de `launchCameraWithPermissions`
        val camPermission = Manifest.permission.CAMERA
        val permissionStatus = checkSelfPermission(camPermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(camPermission)
        when {
            isAlreadyAccepted -> openCamera.launch(photoUri)// lancer l'action souhaitée
            isExplanationNeeded -> showMessage("adadadadada")// afficher une explication
            else -> requestCamera.launch(camPermission)// lancer la demande de permission
        }
    }

    private fun showMessageUpdate(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    fun launchCameraWithPermissions() {
        requestWriteAccess.launch(
            RequestAccess.Args(
                action = StoragePermissions.Action.READ_AND_WRITE,
                types = listOf(StoragePermissions.FileType.Image),
                createdBy = StoragePermissions.CreatedBy.Self
            )
        )
    }

    val userEditLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val user = result.data?.getSerializableExtra("user") as? UserInfo ?: return@registerForActivityResult
        //taskList = taskList + task
        //adapter.submitList(taskList)
        viewModel.update(user)
    }


}