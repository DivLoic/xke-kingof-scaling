package fr.xebia.ldi.config

import com.typesafe.config.ConfigValueType
import pureconfig.ConfigReader
import pureconfig.error._

import scala.util.{Success, Try}

/**
  * Created by loicmdivad.
  */
case class ConfluentConfig(bootstrapServers: String,
                           sslEndpointIdentificationAlgorithm: String,
                           saslMechanism: String,
                           requestTimeoutMs: Int,
                           retryBackoffMs: Int,
                           saslJaasConfig: String,
                           securityProtocol: String)

object ConfluentConfig {

  def localErrorReason =
    WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING))

  def invalidFlagReason(value: String) =
    CannotConvert(value, "Option[ConfluentConfig]", "Flag 'cloud' can only be false or object.")

  implicit val confluentConfigReader: ConfigReader[Option[ConfluentConfig]] =

    ConfigReader.fromCursor[Option[ConfluentConfig]] { cursor =>

      cursor.asString match {
        case Right(flag) if Try(flag.toBoolean) equals Success(false) => Right(None)

        case Right(flag) => Left(ConfigReaderFailures(ConvertFailure(invalidFlagReason(flag), cursor)))

        case Left(ConfigReaderFailures(f: ConvertFailure, _)) if f.reason.equals(localErrorReason) =>
          cursor.asObjectCursor.flatMap(obj => pureconfig.loadConfig[ConfluentConfig](obj.value.toConfig)).map(Some(_))

        case failures@Left(f) => failures.map[Option[ConfluentConfig]](_ => None)
      }
  }
}