package edu.duke.imdb.data.delta.tables.ml

import java.time.Instant
import java.sql.Timestamp

import org.apache.spark.sql.functions.broadcast

import _root_.edu.duke.imdb.data.StorageType
import _root_.edu.duke.imdb.components.SparkComponent
import _root_.edu.duke.imdb.data.StorageType
import _root_.edu.duke.imdb.utils.Encryption
import _root_.edu.duke.imdb.models.entity._
import edu.duke.imdb.components.DatabaseComponent
import edu.duke.imdb.models.components.UserRepository
import edu.duke.imdb.components.ConfigComponent
import edu.duke.imdb.models.components.UserRateMovieRepository
import scala.concurrent.ExecutionContext
import akka.actor.Status
import scala.util.{Failure, Success}

object GenerateTrainingData
    extends SparkComponent
    with DatabaseComponent
    with ConfigComponent {

  import spark.implicits._
  private val userRepo = new UserRepository(this.db)
  val userRatingRepo = new UserRateMovieRepository(this.db)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val databaseName = "ml-1m"
  val movieLensLoader =
    new MovieLensSpark(
      databaseName = databaseName,
      storageType = StorageType.fs_csv
    )
  val ratings_df = movieLensLoader.ratings_df.get
  val movies_df = movieLensLoader.movies_df.get
  val users_df = movieLensLoader.users_df.get
  val links_df = movieLensLoader.links_df.get
  val datasetPath =
    this.config.getString(s"movielens.${databaseName}.input_dir")
  val usersMappingsFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${databaseName}.usersMapping")}"

  val mappedRatingsFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${databaseName}.ratingsMapped")}"

  def createUser(): Unit = {
    val UUIDMap = users_df
      .map { row =>
        (row.getInt(0), java.util.UUID.randomUUID.toString())
      }
      .collect
      .toMap

    val userRDD = users_df
      .map { row =>
        (
          row.getInt(0),
          UUIDMap.get(row.getInt(0)),
          generateEmail(row.getInt(0)),
          generatePassword(),
          Timestamp.from(Instant.now()),
          Timestamp.from(Instant.now())
        )
      }

    userRDD
      .foreach { row =>
        userRepo
          .add(
            User(
              java.util.UUID.fromString(row._2.get),
              s"movielens-${row._1.toString}",
              row._3,
              row._4,
              row._5,
              Some(row._6)
            )
          )
          .onComplete {
            case Failure(f) =>
              println(f)
            case Success(id) =>
          }
        Thread.sleep(100)
      }

    val userMappingRDD = userRDD.map(row => (row._1, row._2))

    userMappingRDD
      .toDF("user", "userId")
      .repartition(1)
      .write
      .option("header", true)
      .csv(usersMappingsFile)
  }

  def generateEmail(userId: Int) = {
    s"${userId}@movielens.com"
  }

  def generatePassword(): String = {
    Encryption.encrypt("123456")
  }

  def generateUserRating(): Unit = {
    val usersMappings_df = movieLensLoader.usersMappings_df.get
    val movieIdMaps =
      links_df.map(x => (x.getString(0), x.getString(2))).collect.toMap
    val joinedDF = ratings_df
      .join(broadcast(usersMappings_df), Seq("user"), "inner")
      .join(broadcast(links_df), Seq("movieId"), "inner")
      .map { row =>
        (
          row.getString(3),
          row.getString(5),
          row.getFloat(2),
          java.util.UUID.randomUUID.toString()
        )
      }

    joinedDF
      .toDF("userId", "movieId", "ratings", "ratingId")
      .repartition(1)
      .write
      .option("header", true)
      .csv(mappedRatingsFile)

    joinedDF.foreach { row =>
      val movieId = row._2
      userRatingRepo
        .add(
          UserRating(
            java.util.UUID.fromString(row._4),
            java.util.UUID.fromString(row._1),
            Some(row._2.toInt),
            Some(row._3.toDouble)
          )
        )
        .onComplete { case Failure(f) =>
          println(row)
          println(f)
        }
      Thread.sleep(100)
    }
  }

  def main(args: Array[String]): Unit = {

    import spark.implicits._
    // generateUserRating()
    createUser()
  }
}
