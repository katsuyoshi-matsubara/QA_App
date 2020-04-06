package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*


//6.2表示名変更ボタンのOnClickListenerではログインしているかどうかを確認し、
// もしログインしていなければSnackBarでその旨を表示して、その後は何も
// しません。ログインしていればFirebaseに表示名を保存し、Preferenceにも
// 保存します。これらの処理はログイン画面での処理を同じ内容です。
//ログアウトボタンのOnClickListenerではログアウト処理を行います。
// ログアウトはFirebaseAuthクラスのsignOutメソッドを呼び出します。
// signOutメソッドを呼び出した後はPreferenceに空文字(““)を保存し、
// Snackbarでログアウト完了の旨を表示します。
class SettingActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        nameText.setText(name)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        //UIの初期設定
        title = "設定"

        changeButton.setOnClickListener{v ->
            //キーボードが出ていたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                //ログインしていない場合は何もしない
                Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
            } else {
                //変更した表示名をFirebaseに保存する
                val name = nameText.text.toString()
                val userRef = mDatabaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name
                userRef.setValue(data)

                //
                val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp.edit()
                editor.putString(NameKEY, name)
                editor.commit()

                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener { v ->
            FirebaseAuth.getInstance().signOut()
            nameText.setText("")
            Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show()
        }
    }
}