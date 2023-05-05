package controllers

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import play.api.Configuration

import java.io.{File, FileInputStream}
import java.net.URL
import java.nio.charset.Charset

trait LocalFiles {

  def configuration: Configuration

  private val pinnedFolder = configuration.get[String]("pinned.folder")

  def filePathForContent(url: URL) = {
    val filename = DigestUtils.sha256Hex(url.toExternalForm) // TODO collisions
    Seq(pinnedFolder, filename).mkString("/")
  }

  def contentTypeOfPinned(url: URL): Option[String] = {
    val mineTypeFile = new File(filepathForMimeType(url))
    if (mineTypeFile.isFile) {
      Some(IOUtils.toString(new FileInputStream(mineTypeFile), Charset.defaultCharset()))
    } else {
      None
    }
  }

  def filepathForMimeType(url: URL) = {
    filePathForContent(url) + ".mime"
  }

}
