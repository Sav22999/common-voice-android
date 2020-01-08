package org.commonvoice.saverio

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone

class FirstRunListen : AppCompatActivity() {

    var status: Int = 0
    private var PRIVATE_MODE = 0
    private val FIRST_RUN_LISTEN = "FIRST_RUN_LISTEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_run_listen_1)

        goNext()
        var btnNext: Button = this.findViewById(R.id.btnNextListen)
        btnNext.setOnClickListener {
            goNext()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    fun goNext() {
        var btnNext: Button = this.findViewById(R.id.btnNextListen)
        var txtNumberBottom: Button = this.findViewById(R.id.btnNumberBottomListen)
        var txtTextBottom: TextView = this.findViewById(R.id.txtTutorialMessageBottomListen)
        var txtNumberTop: Button = this.findViewById(R.id.btnNumberTopListen)
        var txtTextTop: TextView = this.findViewById(R.id.txtTutorialMessageTopListen)
        var txtOne: Button = this.findViewById(R.id.btnOneListen)
        var txtTwo: Button = this.findViewById(R.id.btnTwoListen)
        var txtThree: Button = this.findViewById(R.id.btnThreeListen)
        var txtFour: Button = this.findViewById(R.id.btnFourListen)
        var txtSeven: Button = this.findViewById(R.id.btnSevenListen)
        var txtEight: Button = this.findViewById(R.id.btnEightListen)
        var btnPlay: ImageView = this.findViewById(R.id.imgBtnPlayListen)
        var btnYes: ImageView = this.findViewById(R.id.imgBtnYesListen)
        var btnNo: ImageView = this.findViewById(R.id.imgBtnNoListen)
        if (this.status >= 0 && this.status < 8) {
            txtNumberBottom.isGone = true
            txtTextBottom.isGone = true
            txtNumberTop.isGone = true
            txtTextTop.isGone = true
            txtOne.isGone = true
            txtTwo.isGone = true
            txtThree.isGone = true
            txtFour.isGone = true
            txtSeven.isGone = true
            txtEight.isGone = true
            btnPlay.setImageResource(R.drawable.listen_cv)
            btnYes.isGone = true
            btnNo.isGone = true
        }

        if (this.status == 0) {
            this.status = 1
            btnNext.setText(getString(R.string.btn_tutorial1))
            txtNumberBottom.setText("1")
            txtTextBottom.setText(getString(R.string.txt1_tutorial_listen))
            txtNumberBottom.isGone = false
            txtTextBottom.isGone = false
            txtOne.isGone = false
        } else if (this.status == 1) {
            this.status = 2
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberBottom.setText("2")
            txtTextBottom.setText(getString(R.string.txt2_tutorial_speak_and_listen))
            txtNumberBottom.isGone = false
            txtTextBottom.isGone = false
            txtTwo.isGone = false
        } else if (this.status == 2) {
            this.status = 3
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("3")
            txtTextTop.setText(getString(R.string.txt3_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtThree.isGone = false
        } else if (this.status == 3) {
            this.status = 4
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("4")
            txtTextTop.setText(getString(R.string.txt4_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
        } else if (this.status == 4) {
            this.status = 5
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("5")
            txtTextTop.setText(getString(R.string.txt5_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("5")
            btnPlay.setImageResource(R.drawable.stop_cv)
        } else if (this.status == 5) {
            this.status = 6
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("6")
            txtTextTop.setText(getString(R.string.txt6_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtFour.isGone = false
            txtFour.setText("6")
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
        } else if (this.status == 6) {
            this.status = 7
            btnNext.setText(getString(R.string.btn_tutorial3))
            txtNumberTop.setText("7")
            txtTextTop.setText(getString(R.string.txt7_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtSeven.isGone = false
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
        } else if (this.status == 7) {
            this.status = 8
            btnNext.setText(getString(R.string.btn_tutorial5))
            txtNumberTop.setText("8")
            txtTextTop.setText(getString(R.string.txt8_tutorial_listen))
            txtNumberTop.isGone = false
            txtTextTop.isGone = false
            txtEight.isGone = false
            btnPlay.setImageResource(R.drawable.listen2_cv)
            btnYes.isGone = false
            btnNo.isGone = false
        } else if (this.status == 8) {
            val sharedPref: SharedPreferences = getSharedPreferences(FIRST_RUN_LISTEN, PRIVATE_MODE)
            var editor = sharedPref.edit()
            editor.putBoolean(FIRST_RUN_LISTEN, false)
            editor.apply()

            val intent = Intent(this, ListenActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }
}