package app.carebnb.smartthing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity(){

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, SelectDeviceActivity::class.java)
        startActivity(intent)
        finish()
    }
}