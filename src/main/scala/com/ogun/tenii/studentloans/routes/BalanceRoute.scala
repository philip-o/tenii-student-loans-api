package com.ogun.tenii.studentloans.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.ogun.tenii.studentloans.actors.BalanceActor
import com.ogun.tenii.studentloans.model.api._
import com.typesafe.scalalogging.LazyLogging
import javax.ws.rs.Path
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.swagger.annotations._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Path("balance")
@Api(value = "/balance", description = "Process student loan account info", produces = "application/json")
class BalanceRoute(implicit system: ActorSystem, breaker: CircuitBreaker) extends RequestDirectives with LazyLogging {

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)
  protected val balanceActor: ActorRef = system.actorOf(Props[BalanceActor])

  def route: Route = pathPrefix("balance") {
    getLoanBalance ~ updateBalance
  }

  @Path("{userId}")
  @ApiOperation(httpMethod = "GET", response = classOf[GetStudentLoanResponse], value = "balance")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "userId", dataType = "string", paramType = "path", value = "Tenii Id for the user", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[StudentLoan]),
    new ApiResponse(code = 400, message = "Bad request", response = classOf[ErrorResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def getLoanBalance: Route =
    get {
      path(userIdDirective).as(GetStudentLoanRequest) {
        request =>
          logger.info(s"GET /balance - $request")
          onCompleteWithBreaker(breaker)(balanceActor ? request) {
            case Success(msg: GetStudentLoanResponse) => complete(StatusCodes.OK -> msg.loan)
            case Success(msg: ErrorResponse) => complete(StatusCodes.BadRequest -> msg)
            case Failure(t) => failWith(t)
          }
      }
    }

  @ApiOperation(
    httpMethod = "POST",
    response = classOf[CreateStudentLoan],
    value = "Update student loan info",
    consumes = "application/json",
    notes =
      """
         Update student loan info
      """
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "teniiId", dataType = "string", value = "The tenii Id for the user to find their account", paramType = "body", required = true),
    new ApiImplicitParam(name = "balance", dataType = "double", paramType = "body", value = "The current balance for the account", required = true),
    new ApiImplicitParam(name = "rate", dataType = "string", paramType = "body", value = "The interest rate recorded on the account", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok", response = classOf[StudentLoan]),
    new ApiResponse(code = 400, message = "Bad request", response = classOf[ErrorResponse]),
    new ApiResponse(code = 500, message = "Internal Server Error", response = classOf[Throwable])
  ))
  def updateBalance: Route =
    post {
        entity(as[UpdateStudentLoanBalanceRequest]) {
          request =>
            logger.info(s"POST /balance - $request")
            onCompleteWithBreaker(breaker)(balanceActor ? request) {
              case Success(msg: UpdateStudentLoanBalanceResponse) => complete(StatusCodes.OK -> msg.loan)
              case Success(msg: ErrorResponse) => complete(StatusCodes.BadRequest -> msg)
              case Failure(t) => failWith(t)
            }
        }
    }
}
