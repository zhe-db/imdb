package edu.duke.imdb.http.services

import edu.duke.imdb.models.entity._

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

  implicit object SqlDateFormat extends JsonFormat[java.sql.Date] {
    def write(obj: java.sql.Date) = JsString(obj.toString())
    def read(json: JsValue) = {
      json match {
        case JsString(date) => java.sql.Date.valueOf(date)
        case _ => throw new DeserializationException("Date expected")
      }
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
  implicit val genreCountJsonFormat = jsonFormat2(GenreCount)
  implicit val genreCountsJsonFormat = jsonFormat1(GenreCounts)
  implicit val apiUserRatingJsonFormat = jsonFormat3(APIUserRating)
  implicit val userRatingJsonFormat = jsonFormat4(UserRating)
  implicit val apiUserFavouriteMovieJsonFormat = jsonFormat2(
    APIUserFavouriteMovie
  )
  implicit val userReviewJsonFormat = jsonFormat4(UserReview)
  implicit val apiUserReviewJsonFormat = jsonFormat3(APIUserReview)
  implicit val movieDetailRowJsonFormat = jsonFormat16(MovieDetailRow)
  implicit val movieDetailRowsJsonFormat = jsonFormat1(MovieDetailRows)
  implicit val paginatedMovieDetailRowsJsonFormat = jsonFormat3(
    PaginatedResult[MovieDetailRow]
  )
  implicit val movieCrewJsonFormat = jsonFormat9(MovieCrew)
  implicit val movieCrewsJsonFormat = jsonFormat1(MovieCrews)
  implicit val completeMovieJsonFormat = jsonFormat3(CompleteMovie)
  implicit val userInfoJsonFormat = jsonFormat2(UserInfo)
}
