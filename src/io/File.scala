package org.improving.io

import _root_.java.io.File

object Files
{
  // implicit def filename2file(s: String) = new File(s)
  private def onull[T](x: T): Option[T] = if (x == null) None else Some(x)
  
  def files(s: String): Iterable[File] = files(new File(s))
  def files(f: File): Iterable[File] = f.listFiles filter (_.isFile)
  def dirs(s: String): Iterable[File] = dirs(new File(s))
  def dirs(f: File): Iterable[File] = f.listFiles filter (_.isDirectory)
  
  def filesAndDirs(s: String): Iterable[File] = filesAndDirs(s, 0)
  def filesAndDirs(s: String, depth: Int): Iterable[File] = filesAndDirs(new File(s), depth)
  def filesAndDirs(f: File): Iterable[File] = filesAndDirs(f, 0)
  def filesAndDirs(f: File, depth: Int): Iterable[File] = {
    lazy val files = onull(f.listFiles) getOrElse Array[File]()

    if (depth <= 0) files
    else files ++ files.filter(x => x != null && x.isDirectory).flatMap(x => filesAndDirs(x, depth - 1))
  }
  
  def stdin(): Stream[String] = scala.io.Source.fromInputStream(System.in).getLines.toStream map (_.trim)
}