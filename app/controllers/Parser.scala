package controllers

import anorm.{SqlParser, ~}

object Parser{
  case class postsData(str: String, str1: String, str2: String)

  val postsParser = {
    SqlParser.str("id") ~
      SqlParser.str("user_id") ~
      SqlParser.str("text")
  } map {
    case id ~ user_id ~ text =>
      postsData(id, user_id, text)
  }

}
