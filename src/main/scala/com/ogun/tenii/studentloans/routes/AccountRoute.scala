package com.ogun.tenii.studentloans.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.studentloans.actors.AccountActor
import com.ogun.tenii.studentloans.model.api.{CreateStudentLoan, CreateStudentLoanResponse}
import com.typesafe.scalalogging.LazyLogging
import javax.ws.rs.Path
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("account")
class AccountRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val accountActor: ActorRef = system.actorOf(Props[AccountActor])

  def route: Route = pathPrefix("account") {
    createAccount
  }

  def createAccount: Route =
    post {
      entity(as[CreateStudentLoan]) {
      request =>
          logger.info(s"POST /account - $request")
          onCompleteWithBreaker(breaker)(accountActor ? request) {
            case Success(msg: CreateStudentLoanResponse) if msg.cause.isEmpty => complete(StatusCodes.OK -> msg.loan)
            case Success(msg: CreateStudentLoanResponse) => complete(StatusCodes.BadRequest -> msg)
            case Failure(t) => failWith(t)
          }
      }
    }
}
