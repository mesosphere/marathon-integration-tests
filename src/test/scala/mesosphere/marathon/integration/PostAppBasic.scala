package mesosphere.marathon.integration
package mesosphere.marathon.integration

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.libs.json._

import scala.concurrent.duration._

class PostAppBasic extends Sim {

  val endpoint = "/v2/apps"
  val testGroup = "/marathon/integration/test"
  val prefix = testGroup.slice(0, testGroup.indexOf('/', 1))
  val testApp = s"$testGroup/a"

  setUp(s0.inject(atOnceUsers(1))).protocols(httpConf)

  lazy val s0 =
    scenario("PostAppBasic")
      .exitBlockOnFail {
        exec(createGroup(testGroup))
          .pause(1.second)
          .exec(
            http("post_v2_apps")
              .post(endpoint)
              .header("Content-Type", "application/json")
              .queryParam("force", "true")
              .body(StringBody(s"""
                {
                  "id": "$testApp",
                  "cmd": "sleep 30",
                  "instances": 0
                }
              """)).asJSON
              .check(status.is(201)) // Created
              .check(
                jsonResponse { json =>
                  json.isInstanceOf[JsObject] should equal (true)
                  val jsObj = json.as[JsObject]
                  val appId = jsObj \ "id"
                  appId.isInstanceOf[JsString] should equal (true)
                  appId.as[JsString] should equal (JsString(testApp))
                }
              )
          )
          .pause(1.second)
          .exec(deleteGroup(prefix))
      }
}

