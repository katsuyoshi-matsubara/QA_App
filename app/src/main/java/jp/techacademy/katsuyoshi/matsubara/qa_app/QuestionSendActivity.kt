package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_send.*
import java.io.ByteArrayOutputStream

//メソッド名	内容
//onCreate	渡ってきたジャンルの番号を保持。UIの準備。
//onActivityResult	Intent連携で取得した画像をリサイズしてImageViewに設定。
//onClick	ImageViewとButtonがタップされた時の処理。
//          ImageViewをタップした時は必要であれば許可を求めるダイアログを表示。
//onRequestPermissionsResult	許可を求めるダイアログからの結果を受け取る。
//showChooser	Intent連携の選択ダイアログを表示する。
//onComplete	Firebaseへの保存完了時に呼ばれる。
class QuestionSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {
    companion object {

        //パーミッションのダイアログとIntent連携からActivityに戻ってきた時に識別するための定数、ジャンルを保持するプロパティ、カメラで撮影した画像を保存するURIを保持するプロパティを定義します。
        //PERMISSIONS_REQUEST_CODEとCHOOSER_REQUEST_CODEはそれぞれ100として
        // 定数を定義していますが、他の数値でも問題ありません。それぞれ
        // このActivity内で複数のパーミッションの許可のダイアログを出すことが
        // ある場合、複数のActivityから戻ってくることがある場合に識別する
        // ため の値ですが、質問投稿画面ではそれぞれ1種類だけで本来識別する
        // 必要もないので何かしらの値が入っていれば問題ありません。ただ、
        // 1種類だけの場合でも今後のために定数として定義してその値を使用
        // することが好ましいためこのようにしています。
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var mGenre: Int = 0
    private var mPictureUri: Uri? = null

    //onCreateメソッドではIntentで渡ってきたジャンル番号を取り出して
    //mGenreで保持します。そしてタイトルの設定と、リスナーの設定を行います。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_send)

        // 渡ってきたジャンルの番号を保持する
        val extras = intent.extras
        mGenre = extras.getInt("genre")

        // UIの準備
        title = "質問作成"

        sendButton.setOnClickListener(this)
        imageView.setOnClickListener(this)
    }
//onActivityResultメソッドではIntent連携から戻ってきた時に画像を取得し、
// ImageViewに設定します。dataがnullかdata.getData()の場合はカメラで
// 撮影したときなので画像の取得にmPictureUriを使います。data.getData()
// で取得出来た場合はそのURIを使用します。ContentResolverクラス、
// InputStreamクラス、BitmapFactoryクラスを使ってURIからBitmapを作成します。
// 取得したBitmapからリサイズして新たなBitmapを作成しImageViewに設定します。
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            imageView.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    //onClickメソッドでは添付画像を選択・表示するImageViewを
    // タップした時と、投稿ボタンをタップした時の処理を行います。
    // 許可されていればIntent連携でギャラリーとカメラを選択する
    // ダイアログを表示させるshowChooserメソッドを呼び出し、
    // 許可されていなければrequestPermissionsメソッドで
    // 許可ダイアログを表示させます。
    override fun onClick(v: View) {
        if (v === imageView) { //===と==の違い？調べたけどイマイチわからない・・・
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)

                    return
                }
            } else {
                showChooser()
            }

//投稿ボタンがタップされた時はキーボードを閉じ、タイトルと本文が
//入力されていることを確認した上で、投稿するデータを用意してFirebaseに保存
        } else if (v === sendButton) {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child(ContentsPATH).child(mGenre.toString())

            val data = HashMap<String, String>()

            // UID TODO
            data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

            // タイトルと本文を取得する
            val title = titleText.text.toString()
            val body = bodyText.text.toString()

            if (title.isEmpty()) {
                // タイトルが入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show()
                return
            }

            if (body.isEmpty()) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "質問を入力して下さい", Snackbar.LENGTH_LONG).show()
                return
            }

            // Preferenceから名前を取る
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")

            data["title"] = title
            data["body"] = body
            data["name"] = name

            // 添付画像を取得する
            // as? を使いましたが、これは 安全なキャスト演算子 というもので、
            // キャストに失敗した場合に null を返します。画像が設定されていない
            // 場合にキャストしようとするとアプリが落ちるため、as? を使って、
            // 画像が無いときは null を返すようにしています。
            val drawable = imageView.drawable as? BitmapDrawable

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            //画像はBASE64エンコードというデータを文字列に変換する仕組みを使って
            // 文字列にします。Firebaseは文字列や数字しか保存できませんが
            // こうすることで画像をFirebaseに保存することが可能となります。
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["image"] = bitmapString
            }
//保存する際はDatabaseReferenceクラスのsetValueを使いますが、今回は
// 第2引数も指定しています。第2引数にはCompletionListenerクラスを
// 指定します（今回はActivityがCompletionListenerクラスを実装している）
// 。画像を保存する可能性があり、保存するのに時間がかかることが
// 予想されるのでCompletionListenerクラスで完了を受け取るようにします。
            genreRef.push().setValue(data, this)
            progressBar.visibility = View.VISIBLE
        }
    }
//onRequestPermissionsResultメソッドは
// 許可ダイアログでユーザが選択した 結果を受け取ります。
// if (grantResults[0] == PackageManager.PERMISSION_GRANTED)と
// することで許可したかどうかを判断することが出来ます。許可された場合は
// showChooserメソッドを呼び出します。
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser()
                }
                return
            }
        }
    }
//showChooserメソッドではギャラリーから選択するIntentとカメラで撮影する
//Intentを作成して、さらにそれらを選択するIntentを作成してダイアログを
// 表示させます。
    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
    // IntentクラスのcreateChooserメソッドの第1引数に1つ目のIntentを
    // 指定し、第2引数にダイアログに表示するタイトルを指定します。
    // そしてそのIntentに対し、chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
    // new Intent[]{cameraIntent})と2つ目のIntentを指定することで
    // これら2つのIntentを選択するダイアログを表示されることができます。
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")
    // そしてそのIntentに対し、chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
    // new Intent[]{cameraIntent})と2つ目のIntentを指定することで
    // これら2つのIntentを選択するダイアログを表示されることができます。
    // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }
//Firebaseへの保存が完了したらfinishメソッドを呼び出してActivityを
// 閉じます。もし失敗した場合はSnackbarでエラーの旨を表示します。
    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}
