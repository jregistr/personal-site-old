package controllers

import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsNull
import play.api.mvc._
import services.Constants.{failMessage, succMessage}
import services.{CapchaVerifyService, MailerService}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Controller to handle mailing.
  *
  * @param mailerService - A dependency on the mailing service.
  */
class MailController @Inject()(mailerService: MailerService, capchaVerifyService: CapchaVerifyService) extends Controller {

  /**
    * Action to process message input and send email.
    *
    * @param capchaCode - The verification code generated by recapcha.
    * @param name       - The name of the sender.
    * @param email      - The email of the sender.
    * @param subject    - The subject of the message.
    * @param message    - The content of the message.
    * @return - Returns a json describing the success of the operation.
    */
  def index(capchaCode: String, name: String, email: String, subject: String, message: String): Action[AnyContent] = Action.async {
    capchaVerifyService.verify(capchaCode).map { verified =>
      if (verified) {
        val result: Future[Boolean] = mailerService.sendMail(name, email, subject, message)
        result.map(value => {
          if (value) Ok(succMessage(JsNull)) else BadRequest(failMessage("Did not successfully send message"))
        })
      } else {
        Future {
          BadRequest(failMessage("Failed to verify recapcha token"))
        }
      }
    }.flatMap(identity) recover {
      case _ => BadRequest(failMessage("Failed to verify recapcha token"))
    }
  }

}
