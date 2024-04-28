package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class BombDropCat(private val context: Context) {
    var ex: Int = 0  // X position
    var ey: Int = 0  // Y position

    private val spaceshipBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.enemycat)
    }

    fun getBombDropCatHeight(): Int {
        return spaceshipBitmap.height
    }

    fun getBombDropCatWidth(): Int {
        return spaceshipBitmap.width
    }

    fun getBombDropCatBitmap(): Bitmap {
        return spaceshipBitmap
    }

}
