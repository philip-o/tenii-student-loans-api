package com.ogun.tenii.studentloans.model.api

case class UpdateStudentLoanBalanceRequest(teniiId: String, balance: Double, rate: Double)

case class UpdateStudentLoanBalanceResponse(loan: StudentLoan)
