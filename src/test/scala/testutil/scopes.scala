package testutil

import java.util.UUID

import ammonite.ops._
import helpers.Config
import testutil.fixtures.TestImageFixture

object scopes extends TestImageFixture {

  def withUploadedImage(testCode: String => Unit): Unit = {
    val fileName = s"${UUID.randomUUID().toString}.jpg"

    write.over(Config.directories.uploadedImages/fileName, imageData)
    testCode(fileName)
    rm(Config.directories.uploadedImages/fileName)
  }
}
