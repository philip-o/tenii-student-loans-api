package com.ogun.tenii.studentloans.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.ogun.tenii.studentloans.model.db.StudentLoan
import com.typesafe.scalalogging.LazyLogging

class StudentLoanConnection extends ObjectMongoConnection[StudentLoan] with LazyLogging {

  val collection = "studentLoan"

  override def transform(obj: StudentLoan): MongoDBObject = {
    MongoDBObject("_id" -> obj.id, "userId" -> obj.userId, "accountId" -> obj.accountId, "rate" -> obj.rate, "balance" -> obj.balance)
  }

  def findByUserId(userId: String): Option[StudentLoan] = {
    findByProperty("userId", userId, s"No account found with userId: $userId")
  }

  def findByAccountId(accountId: String): Option[StudentLoan] = {
    findByProperty("accountId", accountId, s"No account found with accountId: $accountId")
  }

  def findById(id: String): Option[StudentLoan] =
    findByObjectId(id, s"No account found with id: $id")

  override def revert(obj: MongoDBObject): StudentLoan = {
    StudentLoan(
      Some(getObjectId(obj, "_id")),
      getString(obj, "userId"),
      getString(obj, "accountId"),
      getDouble(obj, "balance"),
      getDouble(obj, "rate")
    )
  }

}
