package run

import connectors.YoutubeConnector
import models.ConnectorResponseError
import services.YoutubeService

import scala.concurrent.ExecutionContext
import scala.io.StdIn.readLine

object Main {

  val connector: YoutubeConnector = new YoutubeConnector
  val service: YoutubeService = new YoutubeService(connector)
  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    var inputUrl = readLine("Please input the URL of a Youtube video, or input 'x' to exit:")
    while(inputUrl != "x") {
      service.fetchVideoUrl(inputUrl) match {
        case Right(videoUrl) =>
          println("Attempting to download video...")
          println(videoUrl)
          connector.downloadVideo(videoUrl) match {
            case Right(path) =>
              println(s"The video has been downloaded successfully.\nResult: $path")
            case Left(error: ConnectorResponseError) => println(s"[ERROR] - ${error.message}")
          }
          inputUrl = readLine("Input a URL to download another video, or input 'x' to exit:")
        case Left(error: ConnectorResponseError) =>
          println(s"[ERROR] - ${error.message}")
          inputUrl = readLine("Please input the URL of a Youtube video, or input 'x' to exit:")
      }
    }
  }
}
