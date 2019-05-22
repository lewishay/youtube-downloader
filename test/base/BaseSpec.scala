package base

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext

trait BaseSpec extends WordSpec with Matchers with MockFactory {
  implicit val ec: ExecutionContext = mock[ExecutionContext]
}
