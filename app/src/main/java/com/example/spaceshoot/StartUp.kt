package com.example.spaceshoot
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
class StartUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.startup)
    }

    fun startGame(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun openMatchHistory(view: View) {
        val intent = Intent(this, MatchHistory::class.java)
        startActivity(intent)
    }
}
