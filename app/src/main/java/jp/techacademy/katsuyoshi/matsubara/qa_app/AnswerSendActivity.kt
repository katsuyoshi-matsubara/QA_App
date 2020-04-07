package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*

//OnClickListenerとFirebaseのCompletionListenerを実装
class AnswerSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {
    //プロパティとしてIntentで渡ってきたQuestionを保持する変数を定義
    private lateinit var mQuestion: Question

    //onCreateメソッドでは渡ってきたQuestionのインスタンスを保持することと、
    // UIの準備を行います
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        //渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        //UIの準備
        sendButton.setOnClickListener(this)
    }
    //Firebaseへの書き込み完了を受け取るonCompleteメソッドでは成功したら
    // finishメソッドを呼び出してActivityを閉じ、失敗した場合はSnackbarで
    // その旨を表示させます。
    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
    //投稿ボタンを押した時に呼ばれるonClickメソッドではキーボードを閉じて、
    // Firebaseに書き込みます。
    override fun onClick(v: View) {
        //キーボードが出てたら閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)

        val data =HashMap<String,String>()

        //UID
        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        // 表示名
        // Preferenceから名前を取る
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        data["name"] = name

        //回答を取得する
        val answer = answerEditText.text.toString()

        if (answer.isEmpty()) {
            //回答が入力されていない時はエラーを表示するだけ
            Snackbar.make(v, "回答を入力してください", Snackbar.LENGTH_LONG).show()
            return
        }
        data["body"] = answer

        progressBar.visibility = View.VISIBLE
        answerRef.push().setValue(data, this)
    }
}
