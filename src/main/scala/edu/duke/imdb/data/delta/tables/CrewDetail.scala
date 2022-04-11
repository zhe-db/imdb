package edu.duke.imdb.data.delta.tables

import org.apache.spark.sql._
import org.apache.spark._
import _root_.edu.duke.imdb.models.delta._
import _root_.edu.duke.imdb.models.entity._

class CrewDetailDeltaTable
    extends DeltaTableBase(
      tableName = "crewdetail",
      sourceTableName = "crewdetail",
      primaryColumnName = "id",
      tableSchema = s"""
         id STRING,
         know_for_department STRING,
         name STRING,
         gender INT,
         biography STRING,
         place_of_birth STRING,
         profile_path STRING,
         homepage STRING,
         imdb_id STRING
      """
    ) {
  import spark.implicits._
}

class CrewDetailDeltaTableComp extends MoviedDetailDeltaTable {
  lazy val df: DataFrame = this.readData()

  def add(crew: Crew): Unit = {}
}
