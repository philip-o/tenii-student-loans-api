package com.ogun.tenii.studentloans.implicits

import com.ogun.tenii.studentloans.model.api.{ StudentLoan => APIStudentLoan }
import com.ogun.tenii.studentloans.model.db.StudentLoan

trait StudentLoanImplicit {

  def toAPIStudentLoan(loan: StudentLoan) : APIStudentLoan = {
    APIStudentLoan(
      loan.accountId,
      loan.balance,
      loan.percentage
    )
  }
}
