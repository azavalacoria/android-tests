package com.azavalacoria.testfiles

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azavalacoria.testfiles.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var permissionsToAsk: ArrayList<String>
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
        val list = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                    builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
                        Log.d("TAG", "$id")
                    })
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
        }
    }
}