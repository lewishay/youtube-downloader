package connectors

import java.io.File

import base.BaseSpec
import com.softwaremill.sttp._
import com.softwaremill.sttp.testing.SttpBackendStub
import models.{BadRequest, FailureToSend, IncorrectFormat}

class YoutubeConnectorSpec extends BaseSpec {

  val testPath = "target/test/file.txt"
  val validUrl = "http://www.youtube.com/watch?v=XXXXXXXXXXX"

  def createConnector(backend: SttpBackend[Id, Nothing]): YoutubeConnector = new YoutubeConnector {
    override implicit val STTPBackend: SttpBackend[Id, Nothing] = backend
    override val filePath: String = testPath
  }

  ".fetchVideoBody" when {

    "the request URL is in the correct format" when {

      "the request is sent successfully" when {

        "the response code is 200" should {

          "return the response body" in {
            val backend = SttpBackendStub.synchronous.whenAnyRequest
              .thenRespondWrapped(Response(Right("OK"), 200, "", Nil, Nil))
            val connector = createConnector(backend)

            connector.fetchVideoBody(validUrl) shouldBe Right("OK")
          }
        }

        "the response code is not 200" should {

          "return a BadRequest error containing the response code" in {
            val backend = SttpBackendStub.synchronous.whenAnyRequest
              .thenRespondWrapped(Response(Right("FAIL"), 412, "", Nil, Nil))
            val connector = createConnector(backend)
            connector.fetchVideoBody(validUrl) shouldBe Left(BadRequest(412))
          }
        }
      }

      "the request cannot be sent" should {

        "return a FailureToSend error" in {
          val backend = SttpBackendStub.synchronous.whenAnyRequest.thenRespond(throw new Exception())
          val connector = createConnector(backend)
          connector.fetchVideoBody(validUrl) shouldBe Left(FailureToSend)
        }
      }
    }

    "the request URL is not in the correct format" should {

      "return an IncorrectFormat error" in {
        val backend = SttpBackendStub.synchronous
        val connector = createConnector(backend)

        connector.fetchVideoBody(validUrl.replace("youtube", "cooltube")) shouldBe Left(IncorrectFormat)
      }
    }
  }

  ".downloadVideo" when {

    "the request is sent successfully" when {

      "the response code is 200" should {

        val backend = SttpBackendStub.synchronous.whenAnyRequest
          .thenRespond(Response(Right("OK"), 200, "", Nil, Nil))
        val connector = createConnector(backend)
        val file = new File(testPath)

        "download the file" in {
          file.delete()
          connector.downloadVideo(validUrl)
          file.exists()
        }

        "return the path of the downloaded file" in {
          connector.downloadVideo(validUrl) shouldBe Right(testPath)
        }
      }

      "the response code is not 200" should {

        "return a BadRequest error with the response code" in {
          val backend = SttpBackendStub.synchronous.whenAnyRequest
            .thenRespond(Response(Right("FAIL"), 503, "", Nil, Nil))
          val connector = createConnector(backend)
          connector.downloadVideo(validUrl) shouldBe Left(BadRequest(503))
        }
      }
    }

    "the request cannot be sent" should {

      "return a FailureToSend error" in {
        val backend = SttpBackendStub.synchronous.whenAnyRequest.thenRespond(throw new Exception())
        val connector = createConnector(backend)
        connector.downloadVideo(validUrl) shouldBe Left(FailureToSend)
      }
    }
  }
}
