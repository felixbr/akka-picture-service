package helpers

import ammonite.ops._
import com.typesafe.config.ConfigFactory

object Config {
  val defaultConfig = ConfigFactory.load("application.default.conf")
  val config = ConfigFactory.load().withFallback(defaultConfig)

  object directories {
    val processedImages = makePath(config.getString("app.directories.processedImages"))
    val uploadedImages = makePath(config.getString("app.directories.uploadedImages"))
  }

  object transformations {

    object resize {
      val widthLimit = config.getInt("app.transformations.resize.widthLimit")
      val heightLimit = config.getInt("app.transformations.resize.heightLimit")
    }

  }

  private def makePath(configPath: String): Path = {
    try {
      Path(configPath)
    } catch {
      case _: IllegalArgumentException => cwd/configPath
    }
  }
}
