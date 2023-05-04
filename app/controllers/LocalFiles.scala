package controllers

import org.apache.commons.codec.digest.DigestUtils
import play.api.Configuration

import java.net.URL

trait LocalFiles {

  def configuration: Configuration

  private val pinnedFolder = configuration.get[String]("pinned.folder")

  def filePathForContent(url: URL) = {
    val filename = DigestUtils.sha256Hex(url.toExternalForm) // TODO collisions
    Seq(pinnedFolder, filename).mkString("/")
  }

  def filepathForMimeType(url: URL) = {
    filePathForContent(url) + ".mime"
  }

}
