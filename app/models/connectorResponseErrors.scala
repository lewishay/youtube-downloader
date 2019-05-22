package models

sealed trait ConnectorResponseError {
  val message: String
}

case class BadRequest(code: Int) extends ConnectorResponseError {
  override val message: String = s"The server responded with status code: $code."
}

case object FailureToSend extends ConnectorResponseError {
  override val message: String = "The request could not be sent."
}

case object IncorrectFormat extends ConnectorResponseError {
  override val message = "The requested URL was not in the correct format."
}

case object InvalidVideoUrl extends ConnectorResponseError {
  override val message: String = "A valid video URL could not be found in the response."
}
