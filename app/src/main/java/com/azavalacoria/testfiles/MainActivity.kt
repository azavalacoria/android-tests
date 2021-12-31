package com.azavalacoria.testfiles

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.azavalacoria.testfiles.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.*
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class MainActivity : AppCompatActivity() {

    private lateinit var list: Array<String>
    private lateinit var permissionsToAsk: ArrayList<String>
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    var msg: String? = ""
    var lastMsg = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        list = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        currentPermissions(list)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("TAG", "Number of requested permissions: $permissions.size . Granted: ${grantResults.size}")
        when (requestCode) {
            1 -> {
                if (grantResults.size == permissionsToAsk.size) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Result")
                    builder.setMessage("All permissions granted")
                    builder.setPositiveButton("Ok") { dialog, id ->
                        Log.d("TAG", "$id")
                        currentPermissions(list)
                    }
                    builder.show()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun currentPermissions(permissions: Array<String>) {
        permissionsToAsk = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                permissionsToAsk.add(permission)
            }
        }
        if (permissionsToAsk.size > 0) {
            val array = arrayOfNulls<String>(permissionsToAsk.size)
            permissionsToAsk.toArray(array)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, permissionsToAsk[0])) {
                ActivityCompat.requestPermissions(this@MainActivity, array, 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, array, 1)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadOpenConnection("master")
            } else {
                Log.d("TAG", "No hay permiso")
            }
        }
    }

    @SuppressLint("Range", "ResourceType")
    private fun downloadFile(id: String) {
        val serverUrl = resources.getString(R.string.url_server)
        val url = "$serverUrl/$id"
        val directory = File(Environment.DIRECTORY_DOWNLOADS)
        val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }
        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }

                msg = statusMessage(url, directory, status)
                if (msg != lastMsg) {
                    this.runOnUiThread {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
            //val path = "${getStoragePath()}${Environment.DIRECTORY_DOWNLOADS}/master.zip"
            val streamPath =  getFileStreamPath("master.zip")
            if (streamPath.exists()) {
                streamPath.unzip()
            } else {
                Log.d("TAG", "not exista")
                val files = directory.listFiles()
                Log.d("TAG", "$files.size")
                for (f in files) {
                    Log.d("TAG", f.absolutePath)
                }
            }

        }.start()
    }

    private fun downloadOpenConnection(id: String) {
        Thread {
            try {
                val serverUrl = resources.getString(R.string.url_server)
                val mainUrl = "$serverUrl/$id"
                val url = URL(mainUrl)
                val connection = url.openConnection()
                val contentLength = connection.contentLength
                val stream = DataInputStream(url.openStream())
                val buffer = ByteArray(contentLength)
                stream.readFully(buffer)
                stream.close()
                val file = File.createTempFile(id, ".zip")
                val fos = FileOutputStream(file)
                val dos = DataOutputStream(fos)
                dos.write(buffer)
                dos.flush()
                dos.close()
                if (file.exists()) {
                    val path = "${getStoragePath()}${Environment.DIRECTORY_DOWNLOADS}"
                    val dirFile = File(path)
                    Log.d("TAG", "file size: ${file.length()}")
                    file.unzip(dirFile)
                }
            } catch (error: Exception) {
                error.printStackTrace()
                Log.d("TAG", error.localizedMessage)
            }
        }.start()
    }

    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg: String
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + url.substring(
                url.lastIndexOf("/") + 1
            )
            else -> "There's nothing to download"
        }
        return msg
    }

    private fun getStoragePath(): String {
        val splited = getExternalFilesDir(null)?.absolutePath?.split("Android")
        return if (splited?.size!! > 1) {
            splited[0]
        } else {
            ""
        }
    }
}

data class ZipIO (val entry: ZipEntry, val output: File)

fun File.unzip(unzipLocationRoot: File? = null) {

    val rootFolder = unzipLocationRoot ?: File( parentFile.absolutePath + File.separator + "unzip-" + nameWithoutExtension)
    if (!rootFolder.exists()) {
        rootFolder.mkdirs()
    }
    ZipFile(this).use { zip ->
        zip
            .entries()
            .asSequence()
            .map {
                val outputFile = File(rootFolder.absolutePath + File.separator + it.name)
                ZipIO(it, outputFile)
            }
            .map {
                it.output.parentFile?.run{
                    if (!exists()) mkdirs()
                }
                it
            }
            .filter { !it.entry.isDirectory }
            .forEach { (entry, output) ->
                zip.getInputStream(entry).use { input ->
                    output.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
    }

}