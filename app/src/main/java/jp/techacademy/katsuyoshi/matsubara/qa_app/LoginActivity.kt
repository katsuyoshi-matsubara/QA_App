package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*


//メソッド名	実装/修正する内容
//onCreate	Firebase関連の初期化。UIの準備。
//createAccount	処理中のダイアログを表示してFirebaseにアカウント作成を指示。
//login	処理中のダイアログを表示してFirebaseにログインを指示。
//saveName	Preferenceに表示名を保存

class LoginActivity : AppCompatActivity() {

    //プロパティです。Firebase関連でFirebaseAuthクラスと、処理の完了を
    // 受け取るリスナーであるOnCompleteListenerクラスをアカウント
    // 作成処理とログイン処理用の2つ、そしてデータベースへの読み書きに
    // 必要なDatabaseReferenceクラスを定義します。
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDatabaseReference: DatabaseReference

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var mIsCreateAccount = false

    //onCreateメソッドでは以下の処理を実装します。
    //データベースへのリファレンスを取得
    //FirebaseAuthクラスのインスタンスを取得
    //アカウント作成処理のリスナーを作成
    //ログイン処理のリスナーを作成
    //タイトルバーのタイトルを変更
    //アカウント作成ボタンとログインボタンのOnClickListenerを設定
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()

        // アカウント作成処理のリスナー
        // Firebaseのアカウント作成処理はOnCompleteListenerクラスで
        // 受け取ります。このクラスはonCompleteメソッドをオーバーライド
        // する必要があります。その中で引数で渡ってきたTaskクラスの
        // isSuccessfulメソッドで成功したかどうかを確認します。
        // アカウント作成が成功した際にはそのままログイン処理を行うため、
        // loginメソッドを呼び出します。アカウント作成に失敗した場合は、
        // Snackbarでエラーの旨を表示します。
        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                //成功した場合ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
            } else {
                //失敗した場合 エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()

                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        //Firebaseのログイン処理もOnCompleteListenerクラスで受け取ります。
        //ログインに成功したときはmIsCreateAccountを使ってアカウント作成
        // ボタンを押してからのログイン処理か、ログインボタンをタップの
        // 場合かで処理を分けます。
        //アカウント作成ボタンを押した場合は表示名をFirebaseと
        // Preferenceに保存します。Firebaseは、データをKeyとValueの
        // 組み合わせで保存します。DatabaseReferenceが指し示すKeyにValue
        // を保存するには setValue メソッドを使用します。
        // ログインボタンをタップしたときは、Firebaseから表示名を取得して
        // Preferenceに保存します。Firebaseからデータを一度だけ取得する
        // 場合はDatabaseReferenceクラスが実装しているQueryクラスの
        // addListenerForSingleValueEventメソッドを使います。ログインに
        // 失敗した場合は、Snackbarでエラーの旨を表示し、処理中に表示して
        // いたダイアログを非表示にします。最後に finish() メソッドで
        // LoginActivity を閉じます。
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合
                val user = mAuth.currentUser
                val userRef = mDatabaseReference.child(UsersPATH).child(user!!.uid)

                if (mIsCreateAccount) {
                    //アカウント作成の時は表示名をFirebaseに保存する
                    val name = nameText.text.toString()

                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)

                    //表示名をPrefarenceに保存する
                    saveName(name)
                } else {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String)
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
                }

                //プログレスバーを非表示にする
                progressBar.visibility = View.GONE

                //Activityを閉じる
                finish()

            } else {
                //失敗した場合 エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()

                //プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        //UIの準備
        title = "ログイン"

        //アカウント作成ボタンをタップした時には、 InputMethodManager の
        // hideSoftInputFromWindow メソッドを呼び出してキーボードを閉じ、
        //ログイン時に表示名を保存するようmIsCreateAccountにtrueを設定します。
        //そしてcreateAccountメソッドを呼び出しアカウント作成処理を開始させます。
        //ログインボタンのタップした時には同様にキーボードを閉じ、
        // loginメソッドを呼び出してログイン処理を開始させます。
        createButton.setOnClickListener() { v ->
            //キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            if (email.length != 0 && password.length >= 6 && name.length != 0) {
                //ログイン時に表示名を保存するようにフラグを立てる
                mIsCreateAccount = true

                createAccount(email, password)
            } else {
                //エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        loginButton.setOnClickListener { v ->
            //キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6) {
                //フラグを落としておく
                mIsCreateAccount = false

                login(email, password)
            } else {
                //エラーを表示する
                Snackbar.make(v,"正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    //アカウント作成を行うcreateAccountメソッドではプログレスバーを
    // 表示させ、FirebaseAuthクラスのcreateUserWithEmailAndPassword
    // メソッドでアカウント作成を行います。createUserWithEmailAndPassword
    // メソッドの引数にはメールアドレス、パスワードを与え、さらに
    // addOnCompleteListenerメソッドを呼び出してリスナーを設定します。
    private fun createAccount(email: String, password: String) {
        //プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        //アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(mCreateAccountListener)
    }

    //ログイン処理を行うloginメソッドではプログレスバーを表示させ、
    // FirebaseAuthクラスのsignInWithEmailAndPasswordメソッドで
    // ログイン処理を行います。signInWithEmailAndPasswordメソッドの
    // 引数にはメールアドレス、パスワードを与え、さらに
    // addOnCompleteListenerメソッドを呼び出してリスナーを設定します。
    private fun login(email: String, password: String) {
        //プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        //ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }
    //saveNameメソッドでは引数で受け取った表示名をPreferenceに保存します。
    // 忘れずにcommitメソッドを呼び出して保存処理を反映させます
    private fun saveName(name: String) {
        //Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }
}
