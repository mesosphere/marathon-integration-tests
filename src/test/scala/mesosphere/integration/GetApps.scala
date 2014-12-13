package mesosphere.integration

import mesosphere.integration.helpers._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.libs.json._

class GetApps extends MarathonSim {

  val endpoint = "/v2/apps"

  setUp(s0.inject(atOnceUsers(1))).protocols(httpConf)

  lazy val s0 =
    scenario("GetApps")
      .exitBlockOnFail {
        exec(
          http("get_v2_apps")
            .get(endpoint)
            .check(status.is(200))
            .check(
              jsonResponse { json =>
                val jsObj = json.as[JsObject]
                jsObj.keys should equal (Set("apps"))
                val apps = jsObj \ "apps"
                apps.isInstanceOf[JsArray] should equal (true)
              }
            )
        )
      }
}

