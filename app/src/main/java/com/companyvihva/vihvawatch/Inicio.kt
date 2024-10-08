package com.companyvihva.vihvawatch.Inicio

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Inicio : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_1)

        val textView = findViewById<TextView>(android.R.id.text1)
        textView.text = "Parabéns!"
    }
}
