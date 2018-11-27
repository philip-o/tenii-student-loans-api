package com.ogun.tenii.studentloans.model.db

import org.bson.types.ObjectId

case class StudentLoan(id: Option[ObjectId] = None, userId:String, accountId: String, balance: Double, percentage: Double)
