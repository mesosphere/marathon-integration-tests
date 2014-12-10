package mesosphere.marathon.integration

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class GetApps extends Sim {

  val endpoint = "/v2/apps"

  val s0 =
    scenario("GetApps")
      .exec(
        http("get_v2_apps")
          .get(endpoint)
          .check(status is 200)
      )

  setUp(s0.inject(atOnceUsers(1)))
    .protocols(httpConf)

}
