package com.example.spaceshoot
import android.view.animation.AnimationUtils

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Date

@Suppress("DEPRECATION")
class GameOver : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var points: Int = 0
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        points = intent.getIntExtra("points", 0)
        val message = intent.getStringExtra("message") ?: "Game Over"
        findViewById<TextView>(R.id.textViewMessage).text = message
        findViewById<TextView>(R.id.textViewScore).text = "Score: $points"
        savePoints(points)
    }




    private fun savePoints(points: Int) {
        val scores = retrieveScores().toMutableList()
        var isNewHighScore = false
        if (scores.isEmpty() || points > scores.maxOf { it.score }) {
            isNewHighScore = true
        }
        scores.add(Score(points, Calendar.getInstance().time))

        val editor = sharedPreferences.edit()
        editor.putStringSet("scores", scores.map { "${it.score},${it.dateTime.time}" }.toSet())
        editor.apply()
        if (isNewHighScore) {
            val textViewMessage = findViewById<TextView>(R.id.textViewMessage)
            val originalTextColor = textViewMessage.currentTextColor
            textViewMessage.setTextColor(Color.RED)
            textViewMessage.text = "New High Score Record!"
            val animation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    textViewMessage.setTextColor(originalTextColor)
                }

            })

            textViewMessage.startAnimation(animation)

            Handler().postDelayed({
                textViewMessage.clearAnimation()
            }, 4000)
            mediaPlayer = MediaPlayer.create(this, R.raw.highscore)
            mediaPlayer?.start()

            Handler().postDelayed({
                textViewMessage.clearAnimation()
                mediaPlayer?.release()
                mediaPlayer = null
            }, 4000)
        }
    }
    private fun retrieveScores(): Set<Score> {
        val scoreSet = sharedPreferences.getStringSet("scores", setOf()) ?: setOf()
        return scoreSet.map { score ->
            val parts = score.split(",")
            Score(parts[0].toInt(), Date(parts[1].toLong()))
        }.toSet()
    }
    fun restart(view: View?) {
        val intent = Intent(this@GameOver, StartUp::class.java)
        startActivity(intent)
        finish()
    }
    fun exit(view: View?) {
        finish()
    }
}