package com.ogun.tenii.studentloans.actors

import akka.actor.Actor
import com.ogun.tenii.studentloans.db.StudentLoanConnection
import com.ogun.tenii.studentloans.implicits.StudentLoanImplicit
import com.ogun.tenii.studentloans.model.api.{CreateStudentLoanResponse, StudentLoan}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class AccountActor extends Actor with LazyLogging with StudentLoanImplicit {

  val connection = new StudentLoanConnection

  override def receive: Receive = {

    case req: StudentLoan =>
      val senderRef = sender()
      val userIdFuture = Future { connection.findByUserId(req.userId) }
      val accountIdFuture = Future { connection.findByAccountId(req.accountId) }
      val result = for {
        userId <- userIdFuture
        accountId <- accountIdFuture
      }  yield (userId, accountId)

      result onComplete {
        case Success((None, None)) => Future {
          connection.save(req)
        } onComplete {
          case Success(_) => senderRef ! CreateStudentLoanResponse(Some(req), None)
          case Failure(t) => logger.error(s"Error thrown when saving student loan: $req", t)
            senderRef ! CreateStudentLoanResponse(None, Some(s"Error when saving student loan: $t"))
        }
        case Success(_) => logger.error(s"Student loan already exists: $req.  Please check")
          senderRef ! CreateStudentLoanResponse(None, Some(s"Student loan already exists: $req.  Please check"))
        case Failure(t) => logger.error(s"Error thrown when looking up student loan: $req", t)
          senderRef ! CreateStudentLoanResponse(None, Some(s"Error when looking up student loan: $t"))
      }
    case other => logger.error(s"Unknown message received: $other")
  }
}
