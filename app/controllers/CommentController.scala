package controllers

import java.sql.SQLException

import javax.inject.Inject
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import anorm._


class CommentController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {



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
