package mesosphere.marathon.integration

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class GetTasks extends Sim {

  val endpoint = "/v2/tasks"

  setUp(s0.inject(atOnceUsers(1)))
    .protocols(httpConf)

  lazy val s0 =
    scenario("GetTasks")
      .exec(
        http("get_v2_tasks")
          .get(endpoint)
          .check(status is 200)
      )
}

