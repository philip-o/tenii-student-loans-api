package com.ogun.tenii.studentloans.actors

import akka.actor.Actor
import com.ogun.tenii.studentloans.db.StudentLoanConnection
import com.ogun.tenii.studentloans.implicits.StudentLoanImplicit
import com.ogun.tenii.studentloans.model.api.{GetStudentLoanRequest, GetStudentLoanResponse, UpdateStudentLoanBalanceRequest, UpdateStudentLoanBalanceResponse}
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
          case Some(acc) => senderRef ! GetStudentLoanResponse(Some(toAPIStudentLoan(acc)))
          case None => senderRef ! GetStudentLoanResponse(None, Some(s"No student loan exists with userId: ${request.userId}"))
        }
        case Failure(t) => senderRef ! GetStudentLoanResponse(None, Some(s"Failed to find student loan with id: ${request.userId} due to $t"))
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
              case Success(_) => senderRef ! UpdateStudentLoanBalanceResponse(Some(toAPIStudentLoan(account)))
              case Failure(t) => logger.error(s"Failed to update balance for request: $request", t)
                senderRef ! UpdateStudentLoanBalanceResponse(None, Some(s"Failed to update balance for request: $request"))
          }
          case None => senderRef ! UpdateStudentLoanBalanceResponse(None, Some(s"No student loan exists with userId: ${request.teniiId}"))
        }
        case Failure(t) => senderRef ! UpdateStudentLoanBalanceResponse(None, Some(s"Failed to find student loan with id: ${request.teniiId} due to $t"))
      }

    case other => logger.error(s"Unknown message received: $other")
  }
}
