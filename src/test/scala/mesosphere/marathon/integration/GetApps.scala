package mesosphere.marathon.integration

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.libs.json._

class GetApps extends Sim {

  val endpoint = "/v2/apps"

  setUp(s0.inject(atOnceUsers(1))).protocols(httpConf)

  lazy val s0 =
    scenario("GetApps")
      .exitBlockOnFail {
        exec(
          http("get_v2_apps")
            .get(endpoint)
            .check(status.is(200))
            .check(expectedJson { json =>
              json.isInstanceOf[JsObject] should equal (true)
              val jsObj = json.as[JsObject]
              jsObj.keys should equal (Set("apps"))
              val apps = jsObj \ "apps"
              apps.isInstanceOf[JsArray] should equal (true)
            })
        )
      }
}

