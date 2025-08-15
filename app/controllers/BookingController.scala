package controllers

import graphql.SchemaDefinition
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{BaseController, ControllerComponents}
import sangria.execution.{ErrorWithResolver, ExceptionHandler, Executor, HandledException, MaxQueryDepthReachedError, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.{Failure, Success}
import sangria.marshalling.playJson._
import services.BookingService
import scala.concurrent.ExecutionContext.Implicits.global

class BookingController @Inject()(val controllerComponents: ControllerComponents, val bookingService: BookingService) extends BaseController {

  def graphql = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]

    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    executeQuery(query, variables, operation)
  }

  private def parseVariables(variables: String) =
    if (variables.trim == "" || variables.trim == "null") Json.obj() else Json.parse(variables).as[JsObject]

  private def executeQuery(query: String, variables: Option[JsObject], operation: Option[String]) =
    QueryParser.parse(query) match {

      case Success(queryAst) =>
        Executor.execute(SchemaDefinition.BookingSchema, queryAst, bookingService,
          operationName = operation,
          variables = variables getOrElse Json.obj())
          .map(Ok(_))
          .recover {
            case error: QueryAnalysisError => BadRequest(error.resolveError)
            case error: ErrorWithResolver => InternalServerError(error.resolveError)
          }

      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj(
          "syntaxError" -> error.getMessage,
          "locations" -> Json.arr(Json.obj(
            "line" -> error.originalError.position.line,
            "column" -> error.originalError.position.column)))))

      case Failure(error) =>
        throw error
    }

  lazy val exceptionHandler = ExceptionHandler {
    case (_, error @ MaxQueryDepthReachedError(_)) => HandledException(error.getMessage)
  }
}
