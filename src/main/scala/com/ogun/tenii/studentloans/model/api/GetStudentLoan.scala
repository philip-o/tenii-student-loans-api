package com.ogun.tenii.studentloans.model.api

case class GetStudentLoanRequest(userId: String)

case class CreateStudentLoan(userId: String, accountId: String, balance: Double, rate: Double, password: String)

case class StudentLoan(userId: String, accountId: String, balance: Double, rate: Double)

case class GetStudentLoanResponse(loan: StudentLoan)