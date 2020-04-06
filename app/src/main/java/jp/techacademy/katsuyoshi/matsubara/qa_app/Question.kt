package jp.techacademy.katsuyoshi.matsubara.qa_app


//プロパティはvalで定義し、コンストラクタで値を設定します。
// Serializableクラスを実装している理由はIntentでデータを渡せるように
// するためです。このQuestionクラスの中で保持しているクラスもSerializable
// クラスを実装している必要があるため、後述のAnswerクラスもSerializable
// クラスを実装させます。
//プロパティ名	内容
//title	Firebaseから取得したタイトル
//body	Firebaseから取得した質問本文
//name	Firebaseから取得した質問者の名前
//uid	Firebaseから取得した質問者のUID
//questionUid	Firebaseから取得した質問のUID
//genre	質問のジャンル
//imageBytes	Firebaseから取得した画像をbyte型の配列にしたもの
//answers	Firebaseから取得した質問のモデルクラスであるAnswerのArrayList

import java.io.Serializable

class Question(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}