package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.list_question_detail.*

//MainActivityからログイン画面を呼び出すように修正します。
// onCreateメソッドの中で記述されているFloatingActionButtonの
// OnClickListenerの中でLoginActivityに遷移するように実装します。
// QA_Appの仕様として、質問や回答を投稿する際にログインしていなければ
// ログイン画面を表示するようにしたいので、まずはログインしているか
// どうかを確認します。FirebaseクラスのgetAuthメソッドで認証情報である
// AuthDataクラスのインスタンスを取得することができます。この戻り値が
// nullである場合はログインしていないことになるのでその場合にログイン
// 画面に遷移するように実装します。                  //↓ここ抜けてエラー
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //5-5レイアウトファイル、メニューファイルの準備が出来たら最後に
    // MainActivityを修正します。修正する内容は以下の通りです。
    //ドロワーを表示するためのボタンを表示する
    //ドロワー内のメニューが選択された時にどのメニューが選択されたかを保持
    //ドロワー内のメニューが選択された時にタイトルを設定する
    //まずプロパティとしてmToolbarとmGenreを定義します。
    // onCreateメソッドでfindViewByIdを使って取得している箇所を
    // mToolbarを使うように修正します。そしてドロワーの設定を追加します。
    // なお、今回は、 NavigationView.OnNavigationItemSelectedListener
    // は MainActivity に実装しています。修正したら、実行してドロワーが
    // 表示され,メニューを選択するとタイトルが変更されることを確認しましょう。
    private lateinit var mToolbar: Toolbar
    private var mGenre = 0

    // --- ここから追加 8.5
    // まずプロパティとしてFirebaseへのアクセスに必要なDatabaseReference
    // クラスと、ListView、QuestionクラスのArrayList、
    // QuestionsListAdapter を定義
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null

    //次にQuestionsListAdapterにデータを設定します。Firebaseからデータを
    // 取得する必要があります。データに追加・変化があった時に受け取る
    // ChildEventListenerを作成します。onChildAddedメソッドが要素が
    // 追加されたとき、つまり質問が追加された時に呼ばれるメソッドです。
    // この中でQuestionクラスとAnswerを作成し、ArrayListに追加します
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        //onChildChangedメソッドは要素に変化があった時です。今回は質問に
        // 対して回答が投稿された時に呼ばれることとなります。このメソッドが
        // 呼ばれたら変化があった質問に対応するQuestionクラスのインスタンスが
        // 保持している回答のArrayListを一旦クリアし取得した回答を設定します。
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) { //Alt+Enterでimportできず

        }
    }
    // --- ここまで追加する ---8.5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)//5.5で(toolbar)から変更

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        /*  fab.setOnClickListener { _ ->

        */
        //---ここから修正---7-6
        // onCreateメソッドのFABのListenerの箇所でQuestionSendActivityを
        // 起動するようにします。また、 onResume メソッドを追加し、
        // mGenre == 0 の場合、すなわち初回起動時には、最初の選択肢である
        // 「1:趣味」を表示するようにしています。FABのListenerでも、
        // 念のため mGenre == 0 のチェックを行っています。
        fab.setOnClickListener { view ->
            //ジャンルを選択していない場合（mGenre == 0）は
            // エラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, "ジャンルを選択してください", Snackbar.LENGTH_LONG).show()
            } else {

            }
            //--- ここまで修正 ---7-6
            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            //ログインしていなければログイン画面に遷移させる
            // 第2引数は遷移させたいActivityのクラスを指定します。
            // クラス名::class.javaのように指定。
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                //---ここから修正---7-6
            } else {
                //ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
                //--- ここまで修正 ---7-6
            }
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //  .setAction("Action", null).show()
        }

        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle =
            ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //--- ここから 追加8.5
        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        //ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()
        //---ここまで追加8.5

        //9.4 QuestionDetailActivityの実装が完了したら質問一覧画面でリストを
        // タップしたらその質問の詳細画面に飛ぶように修正します。
        // ListViewのsetOnItemClickListenerメソッドでリスナーを登録し、
        // リスナーの中で質問に相当するQuestionのインスタンスを渡して
        // QuestionDetailActivityに遷移させます。
        mListView.setOnItemClickListener { parent, view, position, id ->
            //Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
            //ここまで9.4
        }
    }

    //7-6 onResume メソッドを追加し、 mGenre == 0 の場合、
    // すなわち初回起動時には、最初の選択肢である
    // 「1:趣味」を表示するようにしています
    override  fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        //
        if(mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //onOptionsItemSelectedメソッドを修正して
    // 右上のメニューから設定画面に進むようにします。
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //return when (item.itemId) {
        //  R.id.action_settings -> true
        //else -> super.onOptionsItemSelected(item)
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //5.5
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            mToolbar.title = "趣味"
            mGenre = 1
        } else if (id == R.id.nav_life) {
            mToolbar.title = "生活"
            mGenre = 2
        } else if (id == R.id.nav_health) {
            mToolbar.title = "健康"
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            mToolbar.title = "コンピューター"
            mGenre = 4
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        //--- ここから 追加8.5
        //質問のリストをクリアしてから再度Adapterにセットし、
        // AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
        //--- ここまで追加する ---8.5

        return true
    }
}
