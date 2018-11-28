package com.ogun.tenii.studentloans.implicits

import com.ogun.tenii.studentloans.model.api.{ StudentLoan => APIStudentLoan }
import com.ogun.tenii.studentloans.model.db.StudentLoan

trait StudentLoanImplicit {

  def toAPIStudentLoan(loan: StudentLoan) : APIStudentLoan = {
    APIStudentLoan(
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
      rate = loan.rate
    )
  }
}
