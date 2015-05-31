package helpers

import javax.inject.Inject

import models.{User, Tokens, Users}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Auth @Inject() (val userRepo: Users,
                    val tokensRepo: Tokens) {

  def getUser(token: String): Option[User] = {
    Await.result(tokensRepo.findByContent(token), Duration.Inf) match {
      case Some(s) => Await.result(userRepo.findById(s.userId), Duration.Inf)
      case None => None
    }
  }

}
