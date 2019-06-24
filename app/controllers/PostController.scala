package controllers

import javax.inject._
import play.api.mvc._
import play.api.db.Database
import play.api.libs.json.{JsValue, Json}
import anorm._

import models.Post._
import Util._

@Singleton
class PostController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {

  def index() = Action { implicit request =>
    var tempMap = Map[String, JsValue]()
    var result = Seq(Map[String, JsValue]())
    var count: Int = 0
    db.withConnection { implicit conn =>
      // postsから全ての投稿を抽出
      val records = SQL("SELECT * FROM posts ORDER BY posted_at DESC").as(postParser.*)
      for (record <- records) {
        tempMap += ("id" -> Json.toJson(record.id), "user_id" -> Json.toJson(record.user_id), "text" -> Json.toJson(record.text), "parent_post_id" -> Json.toJson(record.parent_post_id.getOrElse(null)),
          "comment_count" -> Json.toJson(SQL("SELECT COUNT(*) FROM posts WHERE parent_post_id = {id}").on("id" -> record.id).as(SqlParser.int("COUNT(*)").singleOpt).getOrElse(0)), "posted_at" -> Json.toJson(record.posted_at.toString))
        if (count == 0) {
          result = Seq(tempMap)
        } else {
          result = result :+ tempMap
        }
        count += 1
      }
      Ok(Json.obj("posts" -> result))
    }
  }

  def create() = Action { request =>
    val textBody: Option[JsValue] = request.body.asJson
    var isBadRequestFlag: String = "ok"
    // postデータ抽出
    val anyData = Map("user_id" -> textBody.get("user_id").as[String], "text" -> textBody.get("text").as[String])
    postForm.bind(anyData).fold(
      // エラーの場合、400レスポンスでエラー内容表示
      hasError => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> hasError.error("text").get.message.toString))),
      postData => {
        db.withConnection { implicit conn =>
          val isExistUser = SQL("SELECT * FROM test_users WHERE id = {id}").on("id" -> postData.user_id).as(SqlParser.str("id").singleOpt)
          if (isExistUser.getOrElse(null) != postData.user_id) {
            isBadRequestFlag = "does_not_exist_user"
          } else {
            SQL("INSERT INTO posts (id, user_id, text) VALUES ({id}, {user_id}, {text})")
              .on(
                "id" -> uuid,
                "user_id" -> postData.user_id,
                "text" -> postData.text
              ).executeInsert()
          }
        }
        isBadRequestFlag match {
          case "ok" => Ok(Json.toJson(Map("result" -> "OK")))
          case "does_not_exist_user" => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> "user_idが存在しません")))
          case _ => BadRequest(Json.toJson(Map("result" -> "NG", "message" -> "error")))
        }
      }
    )
  }
}
