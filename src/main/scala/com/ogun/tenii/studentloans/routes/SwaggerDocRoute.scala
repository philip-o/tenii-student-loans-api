package com.ogun.tenii.studentloans.routes

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}

import scala.reflect.runtime.{universe => ru}

class SwaggerDocRoute(implicit system: ActorSystem, mat: ActorMaterializer) extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = mat
  override val apiTypes = Seq(ru.typeOf[AccountRoute], ru.typeOf[BalanceRoute], ru.typeOf[PingRoute])
}
