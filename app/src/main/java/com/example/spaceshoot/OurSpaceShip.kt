package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.Random



class OurSpaceship(context: Context) {
    var ourSpaceship: Bitmap? = null
    var ox: Int = 0
    var oy: Int = 0
    private val random: Random = Random()

    init {
        ourSpaceship = BitmapFactory.decodeResource(context.resources, R.drawable.rocket1);
        ox = random.nextInt(SpaceShooter.screenWidth)
        oy = SpaceShooter.screenHeight - ourSpaceship!!.height
    }

    fun getOurSpaceshipBitmap(): Bitmap? {
        return ourSpaceship
    }

    fun getOurSpaceshipWidth(): Int {
        return ourSpaceship!!.width
    }
    fun getOurSpaceshipHeight(): Int {
        return ourSpaceship!!.height
    }
}
