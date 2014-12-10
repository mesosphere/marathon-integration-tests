package mesosphere.marathon.integration

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.net.URI

class GetApps extends Simulation {

  val conf = ConfigFactory.load.getConfig("marathon")
  val marathonUri = new URI(conf.getString("uri"))

  val endpoint = "/v2/apps"

  val httpConf = http
    .baseURL(marathonUri.toString)
    .acceptHeader("application/json")
    .userAgentHeader("gatling")

  val s0 =
    scenario("GetApp")
      .exec(
        http("get_v2_apps")
          .get(endpoint)
          .check(status is 200)
      )

  setUp(s0.inject(atOnceUsers(1)))
    .protocols(httpConf)

}
