package controllers

import java.util.Date

import javax.inject.{Inject, Singleton}
import play.api.db.Database
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import anorm._
import play.api.data._
import play.api.data.Forms._

@Singleton
class CommentController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {

  case class commentsData(id: String, user_id: String, text: String, parent_post_id: String, posted_at: Date)

  case class CommentFormData(user_id: String, text: String)

  val commentsParser = {
    SqlParser.str("id") ~
      SqlParser.str("user_id") ~
      SqlParser.str("text") ~
      SqlParser.str("parent_post_id") ~
      SqlParser.date("posted_at")
  } map {
    case id ~ user_id ~ text ~ parent_post_id ~ posted_at =>
      commentsData(id, user_id, text, parent_post_id, posted_at)
  }

  val commentForm = Form(
    mapping(
      "user_id" -> text,
      "text" -> text.verifying("1文字以上入力してください", _.length >= 1).verifying("100文字以下で入力してください", _.length <= 100)
    )(CommentFormData.apply)(CommentFormData.unapply)
  )

  def index(post_id: String) = Action { implicit request =>
    var tempMap = Map[String, JsValue]()
    var result = Seq(Map[String, JsValue]())
    var count: Int = 0
    db.withConnection { implicit conn =>
      val records = SQL("SELECT * FROM posts WHERE parent_post_id = {id} ORDER BY posted_at DESC").on("id" -> post_id).as(commentsParser.*)
      for (record <- records) {
        tempMap += ("id" -> Json.toJson(record.id), "user_id" -> Json.toJson(record.user_id), "text" -> Json.toJson(record.text), "parent_post_id" -> Json.toJson(record.parent_post_id),
          "comment_count" -> Json.toJson(SQL("SELECT COUNT(*) FROM posts WHERE parent_post_id = {id}").on("id" -> record.id).as(SqlParser.int("COUNT(*)").singleOpt).getOrElse("0").toString), "posted_at" -> Json.toJson(record.posted_at.toString))
        if (count == 0) {
          result = Seq(tempMap)
        } else {
          result = result :+ tempMap
        }
        count += 1
      }
      Ok(Json.toJson("comments" -> result))
    }
  }

  def create(post_id: String) = Action { implicit request =>
    val textBody: Option[JsValue] = request.body.asJson
    var isBadRequestFlag: String = "ok"
    // postデータ抽出
    val anyData = Map("user_id" -> textBody.get("user_id").as[String], "text" -> textBody.get("text").as[String])
    commentForm.bind(anyData).fold(
      // エラーの場合、400エラーでエラー内容表示
      hasError => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> hasError.error("text").get.message.toString))),
      userData => {
        db.withConnection { implicit conn =>
          val isExistUser = SQL("SELECT * FROM test_users WHERE id = {id}").on("id" -> userData.user_id).as(SqlParser.str("id").singleOpt)
          val isExistPostId = SQL("SELECT * FROM posts WHERE id = {id}").on("id" -> post_id).as(SqlParser.str("id").singleOpt)
          if (isExistUser.getOrElse(null) != userData.user_id) { // ユーザーが存在するか調べて、存在しないならflagを立てる
            isBadRequestFlag = "dose_not_exist_user"
          } else if (isExistPostId.getOrElse(null) != post_id) { // post_idが存在するか調べて、存在しないならflagを立てる
            isBadRequestFlag = "dose_not_exist_post_id"
          } else {
            // コメント作成SQL
            SQL("INSERT INTO posts (id, user_id, text, parent_post_id) VALUES ({id}, {user_id}, {text}, {parent_post_id})")
              .on(
                "id" -> uuid,
                "user_id" -> userData.user_id,
                "text" -> userData.text,
                "parent_post_id" -> post_id
              ).executeInsert()
          }
        }
        isBadRequestFlag match {
          case "ok" => Ok(Json.toJson(Map("result" -> "OK")))
          case "dose_not_exist_user" => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> "user_idが存在しません")))
          case "dose_not_exist_post_id" => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> "post_idが存在しません")))
          case _ => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> "error")))
        }
      }
    )
  }

  def uuid = java.util.UUID.randomUUID.toString

}
