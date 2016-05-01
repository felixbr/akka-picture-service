package helpers

import ammonite.ops._
import com.typesafe.config.ConfigFactory

object Config {
  val defaultConfig = ConfigFactory.load("application.default.conf")
  val config = ConfigFactory.load().withFallback(defaultConfig)

  val processedImagesDirectory = makePath(config.getString("app.directories.processedImages"))
  val uploadedImagesDirectory = makePath(config.getString("app.directories.uploadedImages"))

  private def makePath(configPath: String): Path = {
    try {
      Path(configPath)
    } catch {
      case _: IllegalArgumentException => cwd/configPath
    }
  }
}
