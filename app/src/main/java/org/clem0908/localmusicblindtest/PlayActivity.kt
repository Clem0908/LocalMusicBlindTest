package org.clem0908.localmusicblindtest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class PlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val cbRandomStart = findViewById<CheckBox>(R.id.cb_random_start)
        val etDuration = findViewById<EditText>(R.id.et_duration)
        val btnPlay = findViewById<Button>(R.id.btn_play)

        btnPlay.setOnClickListener {
            val randomStart = cbRandomStart.isChecked
            val duration = etDuration.text.toString().toIntOrNull() ?: 30 // default 30 sec

            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("randomStart", randomStart)
                putExtra("duration", duration)
            }
            startActivity(intent)
        }
    }
}
