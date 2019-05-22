package services

import java.net.URLDecoder

import connectors.YoutubeConnector
import javax.inject.Singleton

import models.{ConnectorResponseError, InvalidVideoUrl}

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

@Singleton
class YoutubeService(connector: YoutubeConnector) {

  val streamKey: String = "url_encoded_fmt_stream_map"
  val httpsRegex: Regex = """http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+""".r
  val qualityRegex: Regex = """(,|&)quality(=[^&]*)?""".r
  val typeRegex: Regex = """(,|&)type(=[^&]*)?""".r
  val itagRegex: Regex = """(,|&)itag(=[^&]*)?""".r
  val regexParameterList: List[Regex] = List(qualityRegex, typeRegex, itagRegex)

  def transformBodyToUrl(body: String): Option[String] = {
    val keyInfo = body.substring(body.lastIndexOf(streamKey))
      .split("\",\"").head
      .split("\":\"").last
    val decodedBody = URLDecoder.decode(keyInfo, "UTF-8").replace("\\u0026", "&")

    httpsRegex.findFirstIn(decodedBody) match {
      case Some(validUrl) =>
        val itag: Option[String] = itagRegex.findFirstIn(validUrl)
        val strippedUrl = recursiveStrip(regexParameterList, validUrl)
        val (firstPart, secondPart) = strippedUrl.splitAt(strippedUrl.indexOf('&'))
        Some(firstPart + itag.getOrElse("") + secondPart)
      case None => None
    }
  }

  def recursiveStrip(list: List[Regex], string: String): String = list match {
    case Nil => string
    case head :: tail => recursiveStrip(tail, head.replaceAllIn(string, ""))
  }

  def fetchVideoUrl(url: String)(implicit ec: ExecutionContext): Either[ConnectorResponseError, String] = {
    connector.fetchVideoBody(url) match {
      case Right(body) =>
        transformBodyToUrl(body) match {
          case Some(videoUrl) => Right(videoUrl)
          case None => Left(InvalidVideoUrl)
        }
      case Left(error) => Left(error)
    }
  }
}
