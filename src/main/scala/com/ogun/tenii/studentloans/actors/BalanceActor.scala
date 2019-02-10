package com.ogun.tenii.studentloans.actors

import akka.actor.Actor
import com.ogun.tenii.studentloans.db.StudentLoanConnection
import com.ogun.tenii.studentloans.implicits.StudentLoanImplicit
import com.ogun.tenii.studentloans.model.api._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class BalanceActor extends Actor with LazyLogging with StudentLoanImplicit {

  val connection = new StudentLoanConnection

  override def receive: Receive = {
    case request : GetStudentLoanRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.userId)
      } onComplete {
        case Success(loan) => loan match {
          case Some(acc) => senderRef ! GetStudentLoanResponse(toAPIStudentLoan(acc))
          case None => senderRef ! ErrorResponse("NO_USER", Some(s"No student loan exists with userId: ${request.userId}"))
            logger.error(s"No student loan exists with userId: ${request.userId}")
        }
        case Failure(t) => senderRef ! ErrorResponse("SEARCH_FAILURE", Some(s"Search failed due to ${t.getMessage}"))
      }

    case request: UpdateStudentLoanBalanceRequest =>
      val senderRef = sender()
      Future {
        connection.findByUserId(request.teniiId)
      } onComplete {
        case Success(loan) => loan match {
          case Some(acc) => val account = acc.copy(balance = request.balance, rate = request.rate)
            Future {
            connection.save(account)
            } onComplete {
              case Success(_) => senderRef ! UpdateStudentLoanBalanceResponse(toAPIStudentLoan(account))
              case Failure(t) => logger.error(s"Failed to update balance for request: $request", t)
                senderRef ! ErrorResponse("UPDATE_ERROR", Some(s"Failed to update balance for request: ${t.getMessage}"))
          }
          case None => senderRef ! ErrorResponse("UNKNOWN_USER", Some(s"No student loan exists with userId: ${request.teniiId}"))
        }
        case Failure(t) => senderRef ! ErrorResponse("SEARCH_ERROR", Some(s"Failed to find student loan with id: ${request.teniiId} due to ${t.getMessage}"))
      }

    case other => logger.error(s"Unknown message received: $other")
  }
}
