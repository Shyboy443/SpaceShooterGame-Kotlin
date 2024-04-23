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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // Retrieve the points and message from the intent
        points = intent.getIntExtra("points", 0)
        val message = intent.getStringExtra("message") ?: "Game Over"

        // Assuming you have TextViews for the message and score
        findViewById<TextView>(R.id.textViewMessage).text = message
        findViewById<TextView>(R.id.textViewScore).text = "Score: $points"



        // Save the points
        savePoints(points)
    }




    private fun savePoints(points: Int) {
        // Retrieve existing scores
        val scores = retrieveScores().toMutableList()

        // Check if the current points is the highest
        var isNewHighScore = false
        if (scores.isEmpty() || points > scores.maxOf { it.score }) {
            isNewHighScore = true
        }

        // Add the current score with date to the list
        scores.add(Score(points, Calendar.getInstance().time))

        // Save the updated scores
        val editor = sharedPreferences.edit()
        editor.putStringSet("scores", scores.map { "${it.score},${it.dateTime.time}" }.toSet())
        editor.apply()

        // Update UI if it's a new high score
        if (isNewHighScore) {
            val textViewMessage = findViewById<TextView>(R.id.textViewMessage)
            // Save the original text color
            val originalTextColor = textViewMessage.currentTextColor

            // Change the text color to red
            textViewMessage.setTextColor(Color.RED)
            textViewMessage.text = "New High Score Record!"

            // Load the animation
            val animation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)

            // Add animation listener to revert the text color after animation ends
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    // Revert the text color back to original after animation ends
                    textViewMessage.setTextColor(originalTextColor)
                }

            })

            // Start the animation
            textViewMessage.startAnimation(animation)

            // Stop animation after 3 seconds
            Handler().postDelayed({
                textViewMessage.clearAnimation()
            }, 3000)

            // Play sound for new high score
            mediaPlayer = MediaPlayer.create(this, R.raw.highscore)
            mediaPlayer?.start()

            // Stop animation and release MediaPlayer after 3 seconds
            Handler().postDelayed({
                textViewMessage.clearAnimation()
                mediaPlayer?.release()
                mediaPlayer = null
            }, 3000)
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