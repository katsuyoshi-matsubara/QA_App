package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_main.*

//MainActivityからログイン画面を呼び出すように修正します。
// onCreateメソッドの中で記述されているFloatingActionButtonの
// OnClickListenerの中でLoginActivityに遷移するように実装します。
// QA_Appの仕様として、質問や回答を投稿する際にログインしていなければ
// ログイン画面を表示するようにしたいので、まずはログインしているか
// どうかを確認します。FirebaseクラスのgetAuthメソッドで認証情報である
// AuthDataクラスのインスタンスを取得することができます。この戻り値が
// nullである場合はログインしていないことになるのでその場合にログイン
// 画面に遷移するように実装します。
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { _ ->
            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            //ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }

            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //  .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
