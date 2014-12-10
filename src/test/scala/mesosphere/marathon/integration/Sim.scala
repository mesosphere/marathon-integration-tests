package mesosphere.marathon.integration

import com.typesafe.config.{ Config, ConfigFactory }
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.net.URI

trait Sim extends Simulation {

  val conf: Config = ConfigFactory.load.getConfig("marathon")

  println(s"""
    |conf: $conf
    """.stripMargin)

  def marathonUri: URI = new URI(conf.getString("uri"))

  def httpConf = http
    .baseURL(marathonUri.toString)
    .acceptHeader("application/json")
    .userAgentHeader("gatling")

}

