package mesosphere.integration

import mesosphere.integration.helpers.MarathonSim

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.libs.json._

class GetTasks extends MarathonSim {

  val endpoint = "/v2/tasks"

  setUp(s0.inject(atOnceUsers(1)))
    .protocols(httpConf)

  lazy val s0 =
    scenario("GetTasks")
      .exitBlockOnFail {
        exec(
          http("get_v2_tasks")
            .get(endpoint)
            .check(status.is(200))
            .check(
              jsonResponse { json =>
                val jsObj = json.as[JsObject]
                jsObj.keys should equal (Set("tasks"))
                val tasks = jsObj \ "tasks"
                tasks.isInstanceOf[JsArray] should equal (true)
              }
            )
        )
      }
}

