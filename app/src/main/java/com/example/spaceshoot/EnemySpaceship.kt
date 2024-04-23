import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.spaceshoot.R
import java.util.Random

class EnemySpaceship(context: Context) {

    var enemySpaceship: Bitmap? = null
    var ex = 0
    var ey = 0
    var enemyVelocity = 0
    private val random: Random = Random()

    init {
        enemySpaceship = BitmapFactory.decodeResource(context.resources, R.drawable.rocket2)
        ex = 200 + random.nextInt(400)
        enemyVelocity = 14 + random.nextInt(10)
    }

    fun getEnemySpaceshipBitmap(): Bitmap? {
        return enemySpaceship
    }

    fun getEnemySpaceshipWidth(): Int {
        return enemySpaceship?.width ?: 0
    }

    fun getEnemySpaceshipHeight(): Int {
        return enemySpaceship?.height ?: 0
    }
}
