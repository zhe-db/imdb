package edu.duke.compsci516.http.services

import edu.duke.compsci516.models.entity._

import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import spray.json.JsNumber
import spray.json.JsString
import spray.json.JsValue
import spray.json.DeserializationException
import DefaultJsonProtocol._
import java.util.UUID
import java.sql.Timestamp

object JsonFormats {
  implicit object UuidFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ =>
          throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp) = JsNumber(obj.getTime)

    def read(json: JsValue) = json match {
      case JsNumber(time) => new Timestamp(time.toLong)
      case _              => throw new DeserializationException("Date expected")
    }
  }

  implicit val userJsonFormat = jsonFormat6(User)
  implicit val usersJsonFormat = jsonFormat1(Users)
  implicit val apiUserJsonFormat = jsonFormat3(APIUser)
  implicit val userActionPerformedJsonFormat = jsonFormat1(
    UserRegistry.ActionPerformed
  )
  implicit val genreActionPerformedJsonFormat = jsonFormat1(
    GenreRegistry.ActionPerformed
  )
  implicit val genreJsonFormat = jsonFormat2(Genre)
  implicit val genresJsonFormat = jsonFormat1(Genres)
}
