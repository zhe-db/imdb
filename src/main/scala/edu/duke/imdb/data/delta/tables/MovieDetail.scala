package edu.duke.imdb.data.delta.tables

class MoviedDetailDeltaTable
    extends DeltaTableBase(
      tableName = "moviedetail",
      sourceTableName = "moviedetail",
      primaryColumnName = "id",
      tableSchema = s"""
     id STRING,
     adult BOOLEAN,
     backdrop_path STRING,
     budget INT,
     imdb_id STRING,
     title STRING,
     overview STRING,
     popularity DOUBLE,
     poster_path STRING,
     runtime INT,
     revenue INT,
     vote_average DOUBLE,
     homepage STRING,
     vote_count INT,
     tagline STRING
    """
    ) {}
