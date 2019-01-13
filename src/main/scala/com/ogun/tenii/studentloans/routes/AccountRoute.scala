package com.ogun.tenii.studentloans.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.studentloans.actors.AccountActor
import com.ogun.tenii.studentloans.model.api.{CreateStudentLoan, CreateStudentLoanResponse, StudentLoan}
import com.typesafe.scalalogging.LazyLogging
import javax.ws.rs.Path
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("account")
@Api(value = "/account", description = "Persist student loan account info", produces = "application/json")
class AccountRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val accountActor: ActorRef = system.actorOf(Props[AccountActor])

  def route: Route = pathPrefix("account") {
    createAccount
  }

  @ApiOperation(
    httpMethod = "POST",
    response = classOf[CreateStudentLoan],
    value = "Store student loan info",
    consumes = "application/json",
    notes =
      """
         Store student loan info
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "userId", dataType = "string", value = "The tenii Id for the user to find their account", paramType = "body", required = true),
    new ApiImplicitParam(name = "accountId", dataType = "java.lang.String", paramType = "body", value = "The id for the user with the SLC", required = true),
    new ApiImplicitParam(name = "balance", dataType = "double", paramType = "body", value = "The current balance for the account", required = true),
    new ApiImplicitParam(name = "rate", dataType = "string", paramType = "body", value = "The interest rate recorded on the account", required = true),
    new ApiImplicitParam(name = "password", dataType = "string", paramType = "body", value = "The password created for Tenii to request the user's account info", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Created", response = classOf[StudentLoan]),
    new ApiResponse(code = 400, message = "Bad request", response = classOf[CreateStudentLoanResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
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
