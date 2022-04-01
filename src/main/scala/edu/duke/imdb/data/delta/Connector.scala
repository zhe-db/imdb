package edu.duke.imdb.data.delta

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import _root_.edu.duke.imdb.components._
import _root_.edu.duke.imdb.data.delta._
import edu.duke.imdb.utils.SparkComponent

trait DeltaConnector extends ConfigComponent with SparkComponent {

  // All additional possible jdbc connector properties described here -
  // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html

  val jdbcUrl =
    s"jdbc:postgresql://${this.config.getString(
      "database.postgre.properties.serverName"
    )}:${this.config.getString("database.postgre.properties.portNumber")}/${this.config
      .getString("database.postgre.properties.databaseName")}"

  val jdbcConfg = Map(
    "user" -> this.config.getString("database.postgre.properties.user"),
    "password" -> this.config.getString(
      "database.postgre.properties.password"
    )
  )

  // define a transform to convert all timestamp columns to strings
  val timeStampsToStrings: DataFrame => DataFrame = source => {
    val tsCols = source.schema.fields
      .filter(_.dataType == DataTypes.TimestampType)
      .map(_.name)
    tsCols.foldLeft(source)((df, colName) =>
      df.withColumn(
        colName,
        from_unixtime(unix_timestamp(col(colName)), "yyyy-MM-dd HH:mm:ss.S")
      )
    )
  }

  // Whatever functions are passed to below transform will be applied during import
  val transforms = new DataTransforms(
    Seq(df =>
      df.withColumn(
        "id",
        col("id").cast(types.StringType)
      ) // cast id column to string
    )
  )

}

object DeltaConnector extends DeltaConnector {
  def importTable(
      sourceTableName: String,
      destTableName: String,
      primaryColumnName: String
  ): Unit = {
    val conf = ImportConfig(
      source = sourceTableName,
      destination = destTableName,
      splitBy = primaryColumnName,
      chunks = 20
    )
    new JDBCImport(
      jdbcUrl = jdbcUrl,
      importConfig = conf,
      jdbcParams = jdbcConfg,
      dataTransform = transforms
    )
      .run()
  }
}
