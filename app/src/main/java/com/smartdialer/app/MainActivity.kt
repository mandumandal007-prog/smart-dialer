package com.smartdialer.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.smartdialer.app.contacts.ContactsFragment
import com.smartdialer.app.dialpad.DialpadActivity
import com.smartdialer.app.favorites.FavoritesFragment
import com.smartdialer.app.recents.RecentsFragment
import com.smartdialer.app.spam.BlockedNumbersActivity

class MainActivity : AppCompatActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.addTab(tabs.newTab().setText("Contacts"))
        tabs.addTab(tabs.newTab().setText("Favorites"))
        tabs.addTab(tabs.newTab().setText("Recents"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                open(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        findViewById<FloatingActionButton>(R.id.fab_dial).setOnClickListener {
            startActivity(Intent(this, DialpadActivity::class.java))
        }

        findViewById<View>(R.id.btn_menu).setOnClickListener { anchor -> showMenu(anchor) }

        if (savedInstanceState == null) {
            tabs.getTabAt(0)?.select()
        }

        askPermissions()
        handleDialIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDialIntent(intent)
    }

    private fun handleDialIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme != "tel") return
        val number = data.schemeSpecificPart ?: return
        if (number.isEmpty()) return
        val dialIntent = Intent(this, DialpadActivity::class.java)
        dialIntent.putExtra(DialpadActivity.EXTRA_NUMBER, number)
        startActivity(dialIntent)
    }

    private fun open(position: Int) {
        val fragment: Fragment = when (position) {
            0 -> ContactsFragment()
            1 -> FavoritesFragment()
            else -> RecentsFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Blocked numbers")
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == 1) {
                startActivity(Intent(this, BlockedNumbersActivity::class.java))
                true
            } else {
                false
            }
        }
        popup.show()
    }

    private fun askPermissions() {
        val wanted = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
        val missing = wanted.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
