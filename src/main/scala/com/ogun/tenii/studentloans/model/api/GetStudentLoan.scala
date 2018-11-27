package com.ogun.tenii.studentloans.model.api

case class GetStudentLoanRequest(userId: String)

case class StudentLoan(userId: String, accountId: String, balance: Double, percentage: Double)

case class GetStudentLoanResponse(loan: Option[StudentLoan], cause: Option[String] = None)