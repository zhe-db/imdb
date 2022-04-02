package edu.duke.imdb.data

object StorageType extends Enumeration {
  type Storage = Value

  val fs_csv = Value("FS_CSV")
  val hdfs_csv = Value("HDFS_CSV")
  val fs_delta = Value("FS_DELTA")
  val hdfs_delta = Value("HDFS_DELTA")
}
