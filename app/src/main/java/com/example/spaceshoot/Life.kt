package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class Life(context: Context, var bx: Int, var by: Int) {
    private var bombBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.life)

    fun getLife(): Bitmap {
        return bombBitmap
    }
    fun updatePosition(speed: Int) {
        by += speed
    }
}
