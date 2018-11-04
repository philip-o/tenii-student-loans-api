package com.ogun.tenii.studentloans.routes

import akka.http.scaladsl.server.{ Directives, PathMatcher1 }

trait RequestDirectives extends Directives {

  val userIdDirective: PathMatcher1[String] = Segment

}
