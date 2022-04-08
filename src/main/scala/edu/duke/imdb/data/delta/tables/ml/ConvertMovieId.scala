package edu.duke.imdb.data.delta.tables.ml

import java.time.Instant
import java.sql.Timestamp

import _root_.edu.duke.imdb.data.StorageType
import _root_.edu.duke.imdb.components.SparkComponent
import _root_.edu.duke.imdb.data.StorageType
import _root_.edu.duke.imdb.utils.Encryption
import _root_.edu.duke.imdb.models.entity._
import edu.duke.imdb.components.DatabaseComponent
import edu.duke.imdb.models.components.UserRepository
import edu.duke.imdb.components.ConfigComponent

object GenerateTrainingData
    extends SparkComponent
    with DatabaseComponent
    with ConfigComponent {

  import spark.implicits._
  private val userRepo = new UserRepository(this.db)

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

  def createUser(): Unit = {
    val userRDD = users_df
      .map { row =>
        (
          row.getInt(0),
          java.util.UUID.randomUUID.toString(),
          generateEmail(row.getInt(0)),
          generatePassword(),
          Timestamp.from(Instant.now()),
          Timestamp.from(Instant.now())
        )
      }

    userRDD
      .foreach { row =>
        val res = userRepo.add(
          User(
            java.util.UUID.fromString(row._2),
            s"movielens-${row._1.toString}",
            row._3,
            row._4,
            row._5,
            Some(row._6)
          )
        )
      }

    val userMappingRDD = userRDD.map(row => (row._1, row._2))
    userMappingRDD
      .toDF("user", "userId")
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

  def main(args: Array[String]): Unit = {
    // write.option("header", true).csv("/home/hadoop/spark_output/ratings.csv")
  }
}
