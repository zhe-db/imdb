package edu.duke.imdb.data.delta.tables

import _root_.edu.duke.imdb.components.SparkComponent
import _root_.edu.duke.imdb.components.ConfigComponent

class MovieLensDeltaTable(tableName: String)
    extends SparkComponent
    with ConfigComponent {}
