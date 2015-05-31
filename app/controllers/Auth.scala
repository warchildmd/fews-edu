package controllers

import javax.inject.Inject

import models.{Token, User}
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.Json._
import play.api.libs.json.{JsObject, Json, JsPath, Writes}
import play.api.mvc._
import play.api.libs.functional.syntax._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by Mihail on 5/30/2015.
 */
class Auth @Inject()(protected val usersRepo: models.Users,
                     protected val tokensRepo: models.Tokens) extends Controller {

  implicit val tokenFormat = Json.format[models.Token]
  implicit val userFormat = Json.format[models.User]

  /*def userExtractor(user: User): Option[(Option[Int], String, String, String, DateTime)] =
    Some(user.id, user.username, user.password, user.status, user.createdAt)

  implicit val userWrites: Writes[User] = (
    (JsPath \ "id").write[Option[Int]] and
      (JsPath \ "username").write[String] and
      (JsPath \ "password").write[String] and
      (JsPath \ "status").write[String] and
      (JsPath \ "created_at").write[DateTime]
    )(unlift(userExtractor))*/

  def login = Action { implicit request =>
    request.body.asJson.map { json =>
      (json \ "username").asOpt[String].map { username =>
        (json \ "password").asOpt[String].map { password =>
          Await.result(usersRepo.findByUsername(username), Duration.Inf).map { user =>
            if (BCrypt.checkpw(password, user.password)) {
              val strToken: String = java.util.UUID.randomUUID.toString
              try {
                val token = Token(None, strToken, user.id.get, DateTime.now())
                val id = Await.result(tokensRepo.insert(token), Duration.Inf)
                val jsonToken = Json.toJson(token.copy(id = Some(id))).as[JsObject] + ("user" -> toJson(user.copy(password="")))
                Ok(jsonToken).as("application/json")
              } catch {
                case e: Exception => BadRequest(e.getMessage)
              }
            } else {
              BadRequest("Wrong credentials")
            }
          }.getOrElse {
            BadRequest("Username not found")
          }
        }.getOrElse {
          BadRequest("Missing parameter [password]")
        }
      }.getOrElse {
        BadRequest("Missing parameter [username]")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def logout = Action { implicit request =>
    val token = Await.result(tokensRepo.findByContent(request.headers.get("Auth-Token").getOrElse("")), Duration.Inf)
    token.map { st =>
      tokensRepo.delete(st.id.get)
    }
    Ok("")
  }

  def register = Action { implicit request =>
    request.body.asJson.map { json =>
      (json \ "username").asOpt[String].map { username =>
        (json \ "password").asOpt[String].map { password =>
          (json \ "password_confirmation").asOpt[String].map { password_confirmation =>
            if (password != password_confirmation) {
              BadRequest("Passwords do not match!")
            } else if (password.length < 4) {
              BadRequest("Password should contain at least 4 characters long!")
            } else {
              try {
                val user = User(None, username, BCrypt.hashpw(password, BCrypt.gensalt()), "FRESH", DateTime.now())
                val id = usersRepo.insert(user)
                // val jsonUser = Json.toJson(user.copy(id = Some(id)))
                // Ok(jsonUser).as("application/json")
                val strToken = java.util.UUID.randomUUID.toString
                try {
                  val token = Token(None, strToken, Await.result(id, Duration.Inf), DateTime.now())
                  val tokenId = Await.result(tokensRepo.insert(token), Duration.Inf)
                  val jsonToken = Json.toJson(token.copy(id = Some(tokenId))).as[JsObject] + ("user" -> toJson(user.copy(password="")))
                  Ok(jsonToken).as("application/json")
                } catch {
                  case e: Exception => BadRequest(e.getMessage)
                }
              } catch {
                case e: Exception => BadRequest("Failed to create the user! Maybe the email is already registered?")
              }
            }
          }.getOrElse {
            BadRequest("Missing parameter [password_confirmation]")
          }
        }.getOrElse {
          BadRequest("Missing parameter [password]")
        }
      }.getOrElse {
        BadRequest("Missing parameter [email]")
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }
}
