package jp.techacademy.katsuyoshi.matsubara.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

//qustion?
class QuestionDetailListAdapter(context: Context, private val mQustion: Question) : BaseAdapter() {
    //定数を2つ用意します。どのレイアウトを使って表示させるかを判断する
    // ためのタイプを表す定数です。今回は質問と回答の2つのレイアウトを
    // 使うので2つの定数を用意しています。もしこれが3つであればもう1つ
    // 定数を用意することとなります。
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null

    init {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }

    override fun getCount(): Int {
        return 1 + mQustion.answers.size
    }
    //次に異なる点はgetItemViewTypeメソッドをオーバーライドしている点です。
    // これは今までオーバーライドしていなかったメソッドです。この
    // メソッドは引数で渡ってきたポジションがどのタイプかを返すように
    // します。1行目、つまりポジションが0の時に質問であるTYPE_QUESTIONを
    // 返し、それ以外は回答なのでTYPE_ANSWERを返すようにします。
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {  //書き方？
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQustion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }
    //getViewメソッドの中でgetItemViewTypeメソッドを呼び出して
    // どちらのタイプかを判断してレイアウトファイルを指定します。
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQustion.body
            val name = mQustion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQustion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQustion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name
        }
        return convertView
    }
}