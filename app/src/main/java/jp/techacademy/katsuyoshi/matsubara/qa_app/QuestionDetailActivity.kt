package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*


//Adapterが完成したらQuestionDetailActivityを実装します。
// onCreateメソッドでは渡ってきたQuestionクラスのインスタンスを保持し、タイトルを設定します。
// そして、ListViewの準備をします。
//FABをタップしたらログインしていなければログイン画面に遷移させ、ログインして
// いれば後ほど作成する回答作成画面に遷移させる準備をしておきます。そして
// 重要なのがFirebaseへのリスナーの登録です。回答作成画面から戻ってきた時に
// その回答を表示させるために登録しておきます。
class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    //一時的にお気に入りの追加状態を保持する変数を定義0：未追加　1：追加済み
    private  var mFavorite: Boolean = false

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""//?:

            for (answer in mQuestion.answers) {
                //同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }
    //TODO
    private val mFavoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
           // val map = dataSnapshot.value as Map<String, String>

           // val favoriteUID = dataSnapshot.key ?: ""//?:

            //favoriteButton.text =
           // val body = map[""] ?: ""
            //favoriteButton.text = map[String()] ?: ""
            Log.d("favorite", "a")

           /* for (answer in mQuestion.answers) {
                //同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()*/
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        //渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        //ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        //課題activity起動時にお気に入りボタンが押されているかの判断をする
        //TODO
        val user = FirebaseAuth.getInstance().currentUser
        val databaseReference = FirebaseDatabase.getInstance().getReference("ContentsPATH")
        val mFavoriteRef = databaseReference.child(FavoritePath).child(UsersPATH).child(user!!.uid).child(
            ContentsPATH)
        mFavoriteRef.addChildEventListener(mFavoriteListener)

        mFavoriteRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                Toast.makeText(baseContext, value,
                    Toast.LENGTH_LONG).show()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "読み込み失敗",
                    Toast.LENGTH_LONG).show()
            }
        })
/*
        if (mFavorite == false) {
            favoriteButton.setBackgroundResource(R.color.colorButtonFavoriteOn)
            favoriteButton.setTextColor(getColor(R.color.colorButtonText))
        } else {
            favoriteButton.setBackgroundResource(R.color.colorButtonFavoriteOff)
            favoriteButton.setTextColor(getColor(R.color.colorButtonTextFavoriteOff))
        }
*/
        fab.setOnClickListener {
            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                //ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                //Questionを渡して回答作成画面を起動する
                //最後にQuestionDetailActivityからAnswerSendActivityに遷移
                //するように修正します。先ほどTodoとコメントしていた箇所を修正
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        //課題ログイン時にお気に入りボタンを表示
        if (user == null) {
            favoriteButton.visibility = View.INVISIBLE
        } else {
            favoriteButton.visibility = View.VISIBLE
        }

        //課題お気に入りボタンを押したときの反応。色変える時の書き方注意
        //TODO
        favoriteButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val databaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = databaseReference.child(FavoritePath).child(UsersPATH).child(user!!.uid).child(
                ContentsPATH).child(mQuestion.questionUid)

            if (mFavorite == false) {
                favoriteButton.setBackgroundResource(R.color.colorButtonFavoriteOn)
                favoriteButton.setTextColor(getColor(R.color.colorButtonText))
                mFavorite = true
                favoriteRef.setValue(mQuestion.questionUid)
            } else {
                favoriteButton.setBackgroundResource(R.color.colorButtonFavoriteOff)
                favoriteButton.setTextColor(getColor(R.color.colorButtonTextFavoriteOff))
                mFavorite = false
                favoriteRef.removeValue()
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH)
            .child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

    }

    /*override fun onResume() {
        super.onResume()
        //課題activity再開時にお気に入りボタンが押されているかの判断をする
        if (favoriteFlag == false) {
            favoriteButton.setBackgroundResource(R.color.colorButtonFavoriteOn)
            favoriteButton.setTextColor(getColor(R.color.colorButtonText))
        } else {
            favoriteButton.setBackgroundResource(R.color.colorButtonFavoriteOff)
            favoriteButton.setTextColor(getColor(R.color.colorButtonTextFavoriteOff))
        }
    }*/
}
