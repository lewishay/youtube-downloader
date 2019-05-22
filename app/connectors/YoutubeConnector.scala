package connectors

import java.io.File

import com.softwaremill.sttp._
import javax.inject.Singleton
import models.{BadRequest, ConnectorResponseError, FailureToSend, IncorrectFormat}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Singleton
class YoutubeConnector {

  implicit val STTPBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
  val filePath: String = System.getProperty("user.dir") + "\\target\\videos\\output.webm"
  val youtubeUri: String = "youtube.com/watch?v="

  def fetchVideoBody(requestUrl: String)(implicit ec: ExecutionContext): Either[ConnectorResponseError, String] =
    if (requestUrl.contains(youtubeUri)) {
      val request = sttp.get(uri"$requestUrl")
      Try(request.send()) match {
        case Success(response) =>
          response.code match {
            case 200 => Right(response.unsafeBody)
            case xxx => Left(BadRequest(xxx))
          }
        case Failure(_) =>
          Left(FailureToSend)
      }
    } else {
      Left(IncorrectFormat)
    }

  def downloadVideo(requestUrl: String)(implicit ec: ExecutionContext): Either[ConnectorResponseError, String] = {
    val request = sttp
      .get(uri"$requestUrl")
      .response(asFile(new File(filePath), overwrite = true))
    Try(request.send()) match {
      case Success(response) =>
        response.code match {
          case 200 => Right(filePath)
          case xxx => Left(BadRequest(xxx))
        }
      case Failure(_) => Left(FailureToSend)
    }
  }
}
