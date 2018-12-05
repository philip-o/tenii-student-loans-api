package com.ogun.tenii.studentloans.implicits

import com.ogun.tenii.studentloans.model.api.{ CreateStudentLoan => APIStudentLoan, StudentLoan => OutboundLoan}
import com.ogun.tenii.studentloans.model.db.StudentLoan

trait StudentLoanImplicit {

  def toAPIStudentLoan(loan: StudentLoan) : OutboundLoan = {
    OutboundLoan(
      loan.userId,
      loan.accountId,
      loan.balance,
      loan.rate
    )
  }

  implicit def toStudentLoan(loan: APIStudentLoan) : OutboundLoan = {
    OutboundLoan(
      loan.userId,
      loan.accountId,
      loan.balance,
      loan.rate
    )
  }

  implicit def toDBStudentLoan(loan: APIStudentLoan) : StudentLoan = {
    StudentLoan(
      userId = loan.userId,
      accountId = loan.accountId,
      balance = loan.balance,
      rate = loan.rate,
      password = loan.password
    )
  }
}
