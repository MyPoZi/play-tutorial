package controllers

import java.sql.SQLException
import java.util.Date

import javax.inject.Inject
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import anorm._

import scala.collection.mutable.ListMap


class CommentController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {

  case class commentsData(id: String, user_id: String, text: String, parent_post_id: String, posted_at: Date)

  val commentsParser = {
    SqlParser.str("id") ~
      SqlParser.str("user_id") ~
      SqlParser.str("text") ~
      SqlParser.str("parent_post_id") ~
      SqlParser.date("posted_at")
  } map {
    case id ~ user_id ~ text ~ parent_post_id ~ posted_at =>
      commentsData(id, user_id, text, parent_post_id,  posted_at)
  }

  def index(post_id:String) = Action { implicit request =>
    var user_id = ""
    var tmp_map = Map[String, String]()
    var result = Seq(Map[String, String]())
    var count:Int = 0
    db.withConnection { implicit conn =>
      val records = SQL("SELECT * FROM comments WHERE parent_post_id = {id}").on("id" -> post_id).as(commentsParser.*)
      for (record <- records) {
        tmp_map += ("id" -> record.id, "user_id" -> record.user_id, "text" -> record.text, "parent_post_id" -> record.parent_post_id,
          "comment_count" -> SQL("SELECT COUNT(*) FROM comments WHERE parent_post_id = {id}").on("id" -> record.id).as(SqlParser.int("COUNT(*)").singleOpt).getOrElse("0").toString, "posted_at" -> record.posted_at.toString)
        if (count == 0) {
          result = Seq(tmp_map)
        } else {
          result = result :+ tmp_map
        }
        count += 1
      }
      Ok(Json.toJson("comments" -> result))
    }
  }

  def create(post_id:String) = Action { request =>
    val form: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded
    val param: Map[String, Seq[String]] = form.getOrElse(Map())
    val user_id: String = param.get("user_id").get(0)
    val text: String = param.get("text").get(0)
    db.withConnection { implicit conn =>
      SQL("insert into comments (id, user_id, text, parent_post_id) values ({id}, {user_id}, {text}, {parent_post_id})")
        .on(
          "id" -> uuid,
          "user_id" -> user_id,
          "text" -> text,
          "parent_post_id" -> post_id
        ).executeInsert()
    }
    Ok(Json.toJson(Map("result" -> "OK")))
  }

  def uuid = java.util.UUID.randomUUID.toString

}
