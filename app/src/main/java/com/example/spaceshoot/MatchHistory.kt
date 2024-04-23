package com.example.spaceshoot


import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MatchHistory : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScoreAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve and display scores
        val scores = retrieveScores()
        adapter = ScoreAdapter(scores)
        recyclerView.adapter = adapter

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

    }

    private fun retrieveScores(): List<Score> {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val scoreSet = sharedPreferences.getStringSet("scores", setOf()) ?: setOf()
        return scoreSet.map { score ->
            val parts = score.split(",")
            Score(parts[0].toInt(), Date(parts[1].toLong()))
        }.sortedByDescending { it.dateTime }
    }
    fun clearHistory(view: View) {
        val editor = sharedPreferences.edit()
        editor.remove("scores")
        editor.apply()
        adapter.updateScores(emptyList())

    }

    fun goBack(view: View) {
        onBackPressed()
    }
}
