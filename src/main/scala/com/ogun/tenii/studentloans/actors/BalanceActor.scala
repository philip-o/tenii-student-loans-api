package com.ogun.tenii.studentloans.actors

import akka.actor.Actor
import com.ogun.tenii.studentloans.db.StudentLoanConnection
import com.ogun.tenii.studentloans.implicits.StudentLoanImplicit
import com.ogun.tenii.studentloans.model.api.{GetStudentLoanRequest, GetStudentLoanResponse}
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
        connection.findByAccountId(request.userId)
      } onComplete {
        case Success(loan) => loan match {
          case Some(acc) => senderRef ! GetStudentLoanResponse(Some(toAPIStudentLoan(acc)))
          case None => senderRef ! GetStudentLoanResponse(None, Some(s"No student loan exists with userId: ${request.userId}"))
        }
        case Failure(t) => senderRef ! GetStudentLoanResponse(None, Some(s"Failed to find student loan with id: ${request.userId} due to $t"))
      }

    case other => logger.error(s"Unknown message received: $other")
  }
}
