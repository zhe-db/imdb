package edu.duke.imdb.data.delta.tables

import edu.duke.imdb.components._
import edu.duke.imdb.data.delta.DeltaConnector

class DeltaTableBase(
    var tableName: String,
    var sourceTableName: String,
    var primaryColumnName: String,
    var tableSchema: String
) extends SparkComponent
    with ConfigComponent {
  var savePath: String = this.config.getString("delta.save_path")
  var tablePath: String = s"${this.savePath}/moviedetail"
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

}
