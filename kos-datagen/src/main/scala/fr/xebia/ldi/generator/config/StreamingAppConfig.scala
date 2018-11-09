package fr.xebia.ldi.generator.config

/**
  * Created by loicmdivad.
  */

object StreamingAppConfig {

  case class StreamingAppConfig(schemaRegistryUrl: String,
                                cloud: Option[ConfluentConfig] = None)

}
