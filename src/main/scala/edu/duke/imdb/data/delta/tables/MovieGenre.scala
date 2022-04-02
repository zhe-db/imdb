package edu.duke.imdb.data.delta.tables

package edu.duke.imdb.data.delta.tables

class MoviedGenreDeltaTable
    extends DeltaTableBase(
      tableName = "moviegenre",
      sourceTableName = "moviegenre",
      primaryColumnName = "movie_id",
      tableSchema = s"""
        movie_id INT,
        genre_id INT
      """
    ) {}
