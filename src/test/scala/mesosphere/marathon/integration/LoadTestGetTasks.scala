package mesosphere.marathon.integration

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class LoadTestGetTasks extends Sim {

  val endpoint = "/v2/tasks"

  setUp(s0.inject(atOnceUsers(1000)))
    .protocols(httpConf)

  lazy val s0 =
    scenario("LoadTestGetTasks")
      .exec(
        http("get_v2_tasks")
          .get(endpoint)
          .check(status is 200)
      )
}

