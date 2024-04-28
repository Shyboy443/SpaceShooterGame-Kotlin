package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class Shot(context: Context, var shx: Int, var shy: Int) {
    private val shot: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.fish)


    fun getShot(): Bitmap {
        return shot
    }

    fun getShotWidth(): Int {
        return shot.width
    }

    fun getShotHeight(): Int {
        return shot.height
    }
}
