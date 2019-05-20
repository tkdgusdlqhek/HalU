package com.example.halilarm

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import android.view.WindowManager
import android.os.Build
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    companion object {
        var mHandler: Handler? = null
        @JvmStatic
        var AC: Context? = null
        var auth: FirebaseAuth? = null
        var userModel: UserInfo? = null
        var database: FirebaseDatabase? = null
        var myRef: DatabaseReference? = null
        var nicks: String? = null
        var sh_Pref: SharedPreferences? = null
        var toEdit: SharedPreferences.Editor? = null
        var counter = 0
    }

    var backKeyPressedTime: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_login)
        updateStatusBarColor("#E43F3F")
        sh_Pref = getSharedPreferences("Login credentials", Context.MODE_PRIVATE)
        toEdit = sh_Pref?.edit()
        supportActionBar?.hide()
        AC = applicationContext
        ester?.setOnClickListener {
            if (counter < 5) ;
            else {
                makeCustomToast("이걸 왜 ${counter}번이나 눌렀니", 0, -700)
            }
            counter++
        }
        auth = FirebaseAuth.getInstance()
        mHandler = Handler()
        input_email.setText("")
        input_password.setText("")

        saveID.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                false -> {
                    loging.isChecked = false
                }
            }
        }

        loging.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    saveID.isChecked = true
                }
            }
        }
        database = FirebaseDatabase.getInstance()



        btn_login.setOnClickListener {

            var id: String? = input_email.text.toString().trim()
            var pass: String? = input_password.text.toString().trim()
            if (!(id.isNullOrBlank()) && !(pass.isNullOrBlank())) {
                btn_login.isEnabled = false
                input_email.isEnabled = false
                input_password.isEnabled = false
                link_signup.isEnabled = false
                saveID.isEnabled = false
                loging.isEnabled = false
                LoginCheck(id, pass)
                if (saveID.isChecked) {

                    toEdit?.putBoolean("SAVEFLAG", true)
                    toEdit?.putString("Email", id)
                } else {
                    toEdit?.putBoolean("SAVEFLAG", false)
                }
                if (loging.isChecked) {
                    toEdit?.putBoolean("AUTOLOGIN", true)
                    toEdit?.putString("PASSWORD", pass)
                } else {
                    toEdit?.putBoolean("AUTOLOGIN", false)
                }
                toEdit?.commit()
            } else {
                btn_login.isEnabled = true
                input_email.isEnabled = true
                input_password.isEnabled = true
                link_signup.isEnabled = true
                saveID.isEnabled = true
                loging.isEnabled = true
                makeCustomToast("정보를 확인해주세요.", 0, 500)
            }
        }

        link_signup.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)

            startActivity(intent)
        }
        applySharedPreference()
        /////////////////For Developers
        // DevelopMoreEasy()  // 출시때 빼야함@@@@@@@@@@@@@@@@@@@@
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000L) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(applicationContext, "One more you press back, EXIT", Toast.LENGTH_SHORT).show()
            return
        } else {
            setResult(RESULT_OK)
            finish()
        }
    }

    fun LoginCheck(id: String, pw: String) {
        auth?.signInWithEmailAndPassword(id, pw)
            ?.addOnCompleteListener(this,
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        //var user = auth?.currentUser?.displayName
                        var uid = auth?.currentUser!!.uid
                        myRef = database?.getReference("users")?.child(uid)?.child("nickname")
                        myRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                nicks = p0.value.toString()
                                var intents = Intent(this@LoginActivity, MainActivity::class.java)
                                intents.putExtra("nickname", nicks)
                                startActivityForResult(intents, 101)

                                //Toast.makeText(this@LoginActivity,"nickname = $nicks",Toast.LENGTH_SHORT).show()
                            }

                            override fun onCancelled(p0: DatabaseError) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }
                        })

                    } else {
                        btn_login.isEnabled = true
                        input_email.isEnabled = true
                        input_password.isEnabled = true
                        link_signup.isEnabled = true
                        saveID.isEnabled = true
                        loging.isEnabled = true
                        //Toast.makeText(this@LoginActivity, "LogIn Error", Toast.LENGTH_SHORT).show()
                        makeCustomToast("로그인 실패", 0, 500)
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            btn_login.isEnabled = true
            input_email.isEnabled = true
            input_password.isEnabled = true
            link_signup.isEnabled = true
            saveID.isEnabled = true
            loging.isEnabled = true

            if (!saveID.isChecked) {
                input_email.setText("")
            }
            if (!loging.isChecked) {
                input_password.setText("")
            }
        } else if (resultCode == 1004) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun makeCustomToast(msg: String, xSet: Int, ySet: Int) {
        var view: View =
            getLayoutInflater().inflate(R.layout.toastborder, findViewById<ViewGroup>(R.id.toast_layout_root))
        var text: TextView = view.findViewById(R.id.text)
        text.setTextColor(Color.WHITE)
        text.text = msg
        var toast = Toast(applicationContext)
        toast.setGravity(Gravity.CENTER, xSet, ySet)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = view
        toast.show()
    }

    private fun applySharedPreference() {

        Log.d("tq", "ddd")
        if (sh_Pref != null && sh_Pref!!.contains("Email") &&
            sh_Pref!!.getBoolean("SAVEFLAG", false)
        ) {
            var ML = sh_Pref?.getString("Email", "NULL")
            input_email.setText(ML)
            saveID.isChecked = true
            Log.d("SAVEFLAG", ML)
        }
        if (sh_Pref != null && sh_Pref!!.contains("Email") &&
            sh_Pref!!.getBoolean("AUTOLOGIN", false)
        ) {
            btn_login.isEnabled = false
            input_email.isEnabled = false
            input_password.isEnabled = false
            link_signup.isEnabled = false
            saveID.isEnabled = false
            loging.isEnabled = false
            var ML = sh_Pref?.getString("Email", "")
            input_email.setText(ML)
            var PW = sh_Pref?.getString("PASSWORD", "")
            input_password.setText(PW)
            LoginCheck(ML!!, PW!!)
            loging.isChecked = true
            Log.d("AUTOLOGIN", PW)
        }
    }

    private fun updateStatusBarColor(color: String) {// Color must be in hexadecimal fromat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(color)
        }
    }
}
