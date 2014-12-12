package mesosphere.marathon.integration

import com.typesafe.config.{ Config, ConfigFactory }
import io.gatling.core.check.{ Check, CheckResult }
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.core.validation
import io.gatling.core.validation.Validation
import io.gatling.http.check.{ HttpCheck, HttpCheckScope }
import io.gatling.http.Predef._
import io.gatling.http.request.builder.{
  HttpRequestBuilder,
  HttpRequestWithParamsBuilder
}
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
    .disableWarmUp
    .baseURL(marathonUri.toString)
    .acceptHeader("application/json")
    .userAgentHeader("gatling")

  def appGroup(id: String): String = {
    if (id.count(_ == '/') < 2) "/"
    else id.slice(0, id.lastIndexOf('/'))
  }

  def jsonResponse(validate: JsValue => Unit): HttpCheck = {
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

  def createGroup(groupName: String): ChainBuilder = {
    val key: String = s"get_group_status_$groupName"
    exitBlockOnFail {
      exec( // Check for existence.
        http("get_v2_groups_{id}")
          .get(Sim.groupsEndpoint + groupName)
          .check(status.saveAs(key))
      )
        .doIf(session => session(key).as[String] != "200") { // Create if DNE.
          exec( // Create.
            http("put_v2_groups")
              .put(Sim.groupsEndpoint + groupName)
              .header("Content-Type", "application/json")
              .body(StringBody(s"""{}""")).asJSON
              .check(status.is(200)) // Created
          )
        }
        .exec( // Verify group creation succeeded.
          http("get_v2_groups_{id}")
            .get(Sim.groupsEndpoint + groupName)
            .header("Content-Type", "application/json")
            .check(status.is(200)) // OK
            .check(
              jsonResponse { json =>
                val jsObj = json.as[JsObject]
                jsObj \ "id" should equal (JsString(groupName))
              }
            )
        )
    }
  }

  def deleteGroup(groupName: String): HttpRequestBuilder =
    http("delete_v2_groups_{id}")
      .delete(Sim.groupsEndpoint + groupName)
      .queryParam("force", "true")

  def createApp(app: JsValue): HttpRequestWithParamsBuilder = {
    app.isInstanceOf[JsObject] should equal (true)
    val JsString(appId) = app \ "id"
    val groupName = appGroup(appId)

    http("post_v2_apps")
      .post(Sim.appsEndpoint)
      .header("Content-Type", "application/json")
      .queryParam("force", "true")
      .body(StringBody(Json.stringify(app))).asJSON
      .check(status.is(201)) // Created
      .check(
        jsonResponse { json =>
          json.isInstanceOf[JsObject] should equal (true)
          val jsObj = json.as[JsObject]
          jsObj \ "id" should equal (JsString(appId))
        }
      )
  }

}

object Sim {

  val appsEndpoint = "/v2/apps"

  val groupsEndpoint = "/v2/groups"

  val tasksEndpoint = "/v2/tasks"

  def appTasksEndpoint(appId: String) =
    s"/v2/apps${appId.dropWhile(_ == '/')}"

}
