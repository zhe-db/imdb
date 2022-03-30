package edu.duke.compsci516.delta

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import io.delta.connectors.spark.jdbc._

import edu.duke.compsci516.components._

object Connector extends ConfigComponent {
  implicit val spark: SparkSession = SparkSession
    .builder()
    .master("local[*]")
    .getOrCreate()

  // All additional possible jdbc connector properties described here -
  // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html

  val jdbcUrl = "jdbc:postgresql://localhost:5500/imdb"

  val config = ImportConfig(
    source = "table",
    destination = "target_database.table",
    splitBy = "id",
    chunks = 10
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
    Seq(
      df =>
        df.withColumn(
          "id",
          col("id").cast(types.StringType)
        ), // cast id column to string
      timeStampsToStrings // use transform defined above for timestamp conversion
    )
  )

  new JDBCImport(
    jdbcUrl = jdbcUrl,
    importConfig = config,
    dataTransform = transforms
  )
    .run()

}
