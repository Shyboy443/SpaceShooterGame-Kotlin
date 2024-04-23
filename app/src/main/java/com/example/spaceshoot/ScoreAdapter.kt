package com.example.spaceshoot


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ScoreAdapter(private var scores: List<Score>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_score, parent, false)
        return ScoreViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.bind(score)
    }

    override fun getItemCount(): Int = scores.size

    inner class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewScore: TextView = itemView.findViewById(R.id.textViewScore)
        private val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)

        @SuppressLint("SetTextI18n")
        fun bind(score: Score) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            textViewScore.text = "Score :   ${score.score}"
            textViewDateTime.text = dateFormat.format(score.dateTime)
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateScores(newScores: List<Score>) {
        scores = newScores
        notifyDataSetChanged()
    }

}
