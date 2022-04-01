package edu.duke.imdb.data.delta.tables

class UserRatingDeltaTable
    extends DeltaTableBase(
      tableName = "userratings",
      sourceTableName = "userratings",
      primaryColumnName = "id",
      tableSchema = s"""
         id STRING,
         user_id STRING,
         movie_id INT,
         rating DOUBLE
    """
    ) {}
