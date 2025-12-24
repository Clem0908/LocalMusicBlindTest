package org.clem0908.localmusicblindtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.play).setOnClickListener()
        {
            val intent = Intent(this, PlayActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.configure).setOnClickListener()
        {
            val intent = Intent(this, Configure::class.java)
            startActivity(intent)
        }

    }
}