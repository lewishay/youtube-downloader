package run

import org.scalatest.{Matchers, WordSpec}

class MainSpec extends WordSpec with Matchers {

  "The main method" when {

    "provided with a valid Youtube URL" should {

      "download the video successfully" in {

      }

      "prompt the user to either input another URL or exit" in {

      }
    }

    "provided with an invalid Youtube URL" should {

      "prompt the user to try again or exit" in {

      }
    }

    "provided with the character 'x'" should {

      "terminate the application" in {

      }
    }
  }
}
