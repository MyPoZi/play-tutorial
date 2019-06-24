package models

import java.util.Date
import anorm.{SqlParser, ~}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}

object Post {
  val postForm: Form[PostFormData] = Form(
    mapping(
      "user_id" -> text,
      "text" -> text.verifying("1文字以上入力してください", _.length >= 1).verifying("100文字以下で入力してください", _.length <= 100)
    )(PostFormData.apply)(PostFormData.unapply)
  )

  val postParser = {
    SqlParser.str("id") ~
      SqlParser.str("user_id") ~
      SqlParser.str("text") ~
      SqlParser.get[Option[String]]("parent_post_id") ~
      SqlParser.date("posted_at")
  } map {
    case id ~ user_id ~ text ~ parent_post_id ~ posted_at =>
      PostData(id, user_id, text, parent_post_id, posted_at)
  }

  case class PostData(id: String, user_id: String, text: String, parent_post_id: Option[String], posted_at: Date)
  case class PostFormData(user_id: String, text: String)
}