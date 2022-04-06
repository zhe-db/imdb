package edu.duke.imdb.data.delta.tables

import _root_.edu.duke.imdb.components._
import _root_.edu.duke.imdb.data.delta.DeltaConnector
import org.apache.spark.sql.DataFrame
import io.delta.implicits._

class DeltaTableBase(
    var tableName: String,
    var sourceTableName: String,
    var primaryColumnName: String,
    var tableSchema: String
) extends SparkComponent
    with ConfigComponent {
  var savePath: String = this.config.getString("delta.save_path")
  var tablePath: String = s"${this.savePath}/${tableName}"
  val createTableSql = s"""
     CREATE TABLE IF NOT EXISTS default.${this.tableName} (
       ${this.tableSchema}
     ) USING DELTA LOCATION '${savePath}/${tableName}' 
   """
  val deltaConnector = DeltaConnector

  def createTable() {
    spark.sql(createTableSql)
    deltaConnector.importTable(
      sourceTableName = sourceTableName,
      destTableName = tableName,
      primaryColumnName = primaryColumnName
    )
  }

  def readData(): DataFrame = {
    return spark.read.format("delta").load(s"${savePath}/${tableName}")
  }

  def addData(df: DataFrame): Unit = {
    df.write.format("delta").mode("append").save(this.tablePath)
  }

  def batchWrite(df: DataFrame): Unit = {
    df.write.format("delta").mode("append").save(this.tablePath)
  }

}
