package fr.xebia.ldi.common

import java.io.{File, PrintWriter}
import java.util.Base64

import org.apache.commons.compress.utils.Charsets

/**
  * Created by loicmdivad.
  */
object Secrets extends App {

  val pubkey = System.getenv("API_KEY")
  val secretkey = System.getenv("SECRET_KEY")
  val servers = System.getenv("BOOTSTRAP_SERVERS")

  val serversUrl = s"SASL_SSL://$servers"
  val plainmodule = "org.apache.kafka.common.security.plain.PlainLoginModule"
  val jassconf = s"$plainmodule required username='$pubkey' password='$secretkey';"

  def base64(s: String) = Base64.getEncoder.encodeToString(s.getBytes(Charsets.UTF_8))

  val content =
    s"""
       |---
       |apiVersion: v1
       |kind: Secret
       |metadata:
       |  name: confluent-secrets
       |data:
       |  api-key: ${base64(pubkey)}
       |  secret-key: ${base64(secretkey)}
       |  bootstrap-servers: ${base64(servers)}
       |  bootstrap-servers-url: ${base64(serversUrl)}
       |  sasl-jaas-config: ${base64(jassconf)}
""".stripMargin

  val secret = new File("kubernetes/.secrets.yaml")
  val writer = new PrintWriter(secret)

  writer.write(content)
  writer.close()

}