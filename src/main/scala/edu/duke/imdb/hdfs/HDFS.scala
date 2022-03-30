package edu.duke.imdb.hdfs

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import akka.stream.alpakka.hdfs._
import akka.stream.alpakka.hdfs.scaladsl.HdfsFlow
import org.apache.hadoop.io.compress._

import _root_.edu.duke.imdb.components.ConfigComponent

object HDFSComponent extends ConfigComponent {
  val conf = new Configuration()
  val hdfsDomain = this.config.getString("hdfs.domain")
  val hdfsPort = this.config.getString("hdfs.port")
  conf.set("fs.default.name", s"hdfs://${hdfsDomain}:${hdfsPort}")
  val fs: FileSystem = FileSystem.get(conf)
  val codec = new DefaultCodec()
  codec.setConf(fs.getConf)
  val settings = HdfsWritingSettings()
  val flow = HdfsFlow.compressed(
    fs,
    SyncStrategy.count(1),
    RotationStrategy.size(0.1, FileUnit.MB),
    codec,
    settings
  )
}
