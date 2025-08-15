// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  v1_post_PostRouter_0: v1.post.PostRouter,
  // @LINE:3
  BookingController_0: controllers.BookingController,
  val prefix: String
) extends GeneratedRouter {

  @javax.inject.Inject()
  def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    v1_post_PostRouter_0: v1.post.PostRouter,
    // @LINE:3
    BookingController_0: controllers.BookingController
  ) = this(errorHandler, v1_post_PostRouter_0, BookingController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, v1_post_PostRouter_0, BookingController_0, prefix)
  }

  private val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    prefixed_v1_post_PostRouter_0_0.router.documentation,
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """graphql""", """controllers.BookingController.graphql"""),
    Nil
  ).foldLeft(Seq.empty[(String, String, String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String, String, String)]
    case l => s ++ l.asInstanceOf[List[(String, String, String)]]
  }}


  // @LINE:1
  private val prefixed_v1_post_PostRouter_0_0 = Include(v1_post_PostRouter_0.withPrefix(this.prefix + (if (this.prefix.endsWith("/")) "" else "/") + "v1/posts"))

  // @LINE:3
  private lazy val controllers_BookingController_graphql1_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("graphql")))
  )
  private lazy val controllers_BookingController_graphql1_invoker = createInvoker(
    BookingController_0.graphql,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.BookingController",
      "graphql",
      Nil,
      "POST",
      this.prefix + """graphql""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case prefixed_v1_post_PostRouter_0_0(handler) => handler
  
    // @LINE:3
    case controllers_BookingController_graphql1_route(params@_) =>
      call { 
        controllers_BookingController_graphql1_invoker.call(BookingController_0.graphql)
      }
  }
}
