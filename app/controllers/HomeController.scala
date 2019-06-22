package controllers

import java.sql.SQLException

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.db.Database

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(db: Database, cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def test() = Action { implicit request =>
    var msg = "database"
    try {
      db.withConnection { conn =>
        val stmt = conn.createStatement
        val rs = stmt.executeQuery("SELECT * from test_users")
        while (rs.next) {
          msg += rs.getString("name")
        }
      }
    } catch {
      case e:SQLException =>
        msg = "no"
    }
    Ok(msg).as("application/json")
  }

//  def index() = Action { implicit request =>
//    db.withConnection { implicit conn =>
//      val result:List[String] = SQL("Select * from test_users").as(SqlParser.str("name").*)
//      Ok("hoge").as("application/json")
//    }
//  }
}
