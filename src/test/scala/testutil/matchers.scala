package testutil

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.{HttpEncoding, HttpEncodings, `Content-Encoding`}
import org.scalatest._
import org.scalatest.matchers.Matcher

object matchers extends Matchers {

  def haveContentEncoding(encoding: HttpEncoding): Matcher[HttpResponse] = {
    be(encoding) compose { (_: HttpResponse).header[`Content-Encoding`].map(_.encodings.head).getOrElse(HttpEncodings.identity) }
  }
}
