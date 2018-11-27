package com.ogun.tenii.studentloans.model.api

case class CreateStudentLoanResponse(loan: Option[StudentLoan], cause: Option[String] = None)