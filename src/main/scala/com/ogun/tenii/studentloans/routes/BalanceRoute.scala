package com.ogun.tenii.studentloans.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.studentloans.actors.BalanceActor
import com.ogun.tenii.studentloans.model.api.{GetStudentLoanRequest, GetStudentLoanResponse}
import com.typesafe.scalalogging.LazyLogging
import javax.ws.rs.Path
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("balance")
class BalanceRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val balanceActor: ActorRef = system.actorOf(Props[BalanceActor])

  def route: Route = pathPrefix("balance") {
    getLoanBalance
  }

  def getLoanBalance: Route =
    get {
      path(userIdDirective).as(GetStudentLoanRequest) {
        request =>
          logger.info(s"GET /balance - $request")
          onCompleteWithBreaker(breaker)(balanceActor ? request) {
            case Success(msg: GetStudentLoanResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg.loan)
            case Success(msg: GetStudentLoanResponse) => complete(StatusCodes.BadRequest -> msg)
            case Failure(t) => failWith(t)
          }
      }
    }
}
