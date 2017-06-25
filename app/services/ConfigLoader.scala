package services

import java.io.FileReader
import javax.inject.Inject

import com.google.gson.{JsonObject, JsonParser}
import com.google.gson.stream.JsonReader

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import akka.actor._


trait ConfigLoader {
  def load(configFileName: String, fallBackFileName: Option[String]): Future[Try[JsonObject]]
}

class JsonConfigLoader @Inject() (akkaSystem: ActorSystem) extends ConfigLoader {

  implicit val myExecutionContext: ExecutionContext = akkaSystem.dispatchers.lookup("contexts.file-loadups")

  override def load(configFileName: String, fallBackFileName: Option[String] = None): Future[Try[JsonObject]] = Future {

    val configOptionUrl: Try[String] = Try(getClass.getResource(configFileName).getFile) match {
      case u: Success[_] => u
      case f: Failure[_] => fallBackFileName match {
        case Some(fallback) => Try(getClass.getResource(fallback).getFile)
        case _ => f
      }
    }

    configOptionUrl match {
      case Success(url) =>
        val reader = new JsonReader(new FileReader(url))
        Try(new JsonParser().parse(reader).getAsJsonObject)
      case Failure(e) => throw e
    }
  }
}