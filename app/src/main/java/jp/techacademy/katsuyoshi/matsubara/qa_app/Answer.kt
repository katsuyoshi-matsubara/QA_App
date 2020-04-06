package jp.techacademy.katsuyoshi.matsubara.qa_app

//先に質問の回答のモデルクラスであるAnswer.ktも作成します。
// プロパティを以下のように作成し、それぞれGetterを用意します。
// 値はコンストラクタで設定するのみでSetterは作りません
//body	Firebaseから取得した回答本文
//name	Firebaseから取得した回答者の名前
//uid	Firebaseから取得した回答者のUID
//answerUid	Firebaseから取得した回答のUID

import java.io.Serializable

class Answer(val body: String, val name: String, val uid: String, val answerUid: String) : Serializable
