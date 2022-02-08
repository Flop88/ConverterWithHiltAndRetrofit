package ge.gergmoney.myconverterpro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ge.gergmoney.myconverterpro.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val isChecked = intent.getBooleanExtra("isChecked", false)
        checkbox.isChecked = isChecked

        save_settings_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isChecked", checkbox.isChecked)
            }
            startActivity(intent)
        }

    }
}