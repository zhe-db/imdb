package edu.duke.imdb.data.delta.tables

class MovieCrewDeltaTable
    extends DeltaTableBase(
      tableName = "moviecrew",
      sourceTableName = "moviecrew",
      primaryColumnName = "movie_id",
      tableSchema = s"""
         movie_id INT,
         crew_id INT,
         types STRING,
         cast_id INTEGER,
         character STRING,
         job STRING,
         department STRING,
         credit_id STRING,
         ordering INTEGER
    """
    ) {}
