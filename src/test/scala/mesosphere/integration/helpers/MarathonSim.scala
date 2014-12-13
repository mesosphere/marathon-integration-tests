package mesosphere.integration.helpers

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.{
  HttpRequestBuilder,
  HttpRequestWithParamsBuilder
}
import play.api.libs.json._

import java.net.URI
import scala.concurrent.duration._

trait MarathonSim extends Sim {

  import MarathonSim._

  def marathonUri: URI = new URI(conf.getString("uri"))

  def appGroup(id: String): String = {
    if (id.count(_ == '/') < 2) "/"
    else id.slice(0, id.lastIndexOf('/'))
  }

  def createGroup(groupName: String): ChainBuilder = {
    val key: String = s"get_group_status_$groupName"
    exitBlockOnFail {
      exec( // Check for existence.
        http("get_v2_groups_{id}")
          .get(groupsEndpoint + groupName)
          .check(status.saveAs(key))
      )
        .doIf(session => session(key).as[String] != "200") { // Create if DNE.
          exec( // Create.
            http("put_v2_groups")
              .put(groupsEndpoint + groupName)
              .header("Content-Type", "application/json")
              .body(StringBody(s"""{}""")).asJSON
              .check(status.is(200)) // Created
          )
        }
        .exec( // Verify group creation succeeded.
          http("get_v2_groups_{id}")
            .get(groupsEndpoint + groupName)
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
      .delete(groupsEndpoint + groupName)
      .queryParam("force", "true")

  def createApp(app: JsValue): HttpRequestWithParamsBuilder = {
    app.isInstanceOf[JsObject] should equal (true)
    val JsString(appId) = app \ "id"
    val groupName = appGroup(appId)

    http("post_v2_apps")
      .post(appsEndpoint)
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

object MarathonSim {

  val appsEndpoint = "/v2/apps"

  val groupsEndpoint = "/v2/groups"

  val tasksEndpoint = "/v2/tasks"

  def appTasksEndpoint(appId: String) =
    s"/v2/apps${appId.dropWhile(_ == '/')}"

}

