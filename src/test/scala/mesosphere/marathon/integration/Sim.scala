package mesosphere.marathon.integration

import com.typesafe.config.{ Config, ConfigFactory }
import io.gatling.core.check.{ Check, CheckResult }
import io.gatling.core.Predef._
import io.gatling.core.validation
import io.gatling.core.validation.Validation
import io.gatling.http.check.{ HttpCheck, HttpCheckScope }
import io.gatling.http.Predef._
import io.gatling.http.response.StringResponseBodyUsageStrategy
import org.scalatest.Matchers
import play.api.libs.json._

import java.net.URI
import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }

trait Sim extends Simulation with Matchers {

  val conf: Config = ConfigFactory.load.getConfig("marathon")

  def marathonUri: URI = new URI(conf.getString("uri"))

  def httpConf = http
    .baseURL(marathonUri.toString)
    .acceptHeader("application/json")
    .userAgentHeader("gatling")

  def expectedJson(validate: JsValue => Unit): HttpCheck = {
    val wrapped = new Check[Response] {
      def check(
        response: Response,
        session: Session)(
          implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
        val json = Json.parse(response.body.string)
        Try(validate(json)) match {
          case Success(()) =>
            validation.Success(CheckResult(Some(json), None))

          case Failure(cause) =>
            cause.printStackTrace
            validation.Failure(cause.getMessage)
        }
      }
    }

    HttpCheck(
      wrapped,
      HttpCheckScope.Body,
      Some(StringResponseBodyUsageStrategy)
    )
  }

}

