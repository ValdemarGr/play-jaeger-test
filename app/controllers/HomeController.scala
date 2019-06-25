package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import io.opentracing.contrib.concurrent.TracedExecutionContext
import io.opentracing.propagation.Format
import io.opentracing.util.GlobalTracer
import io.opentracing.{Scope, ScopeManager, Span, SpanContext, Tracer}
import play.api.routing.Router

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.util.AsyncExecutor

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc)  {

  val tracer: io.opentracing.Tracer = new io.jaegertracing.Configuration("play-test").getTracer

  GlobalTracer.registerIfAbsent(tracer)

  val executionContext = scala.concurrent.ExecutionContext.global

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  class AsyncExecutorContextAware(underlying: AsyncExecutor, ec: ExecutionContext) extends AsyncExecutor {
    override def executionContext: ExecutionContext = ec
    override def close():          Unit             = underlying.close()
  }

  implicit val ec: ExecutionContext = new TracedExecutionContext(executionContext, tracer)
  val aeca = new AsyncExecutorContextAware(slick.util.AsyncExecutor.default(), ec)

  val db: Database = Database.forURL(
    url = "jdbc:tracing:mysql://localhost/cerno2?useSSL=false",
    user = "root",
    password = "1234",
    driver = "io.opentracing.contrib.jdbc.TracingDriver",
    executor = aeca
  )

  def oom() = Action.async { implicit request: Request[AnyContent] =>
    import java.util
    val map = new util.HashMap[Long, String]
    var i = 1
    while ( {
      true
    }) {
      val sb = new StringBuilder
      var j = 0
      while ( {
        j < i
      }) {
        sb.append("x")

        {
          j += 1; j - 1
        }
      }
      map.put(i, sb.toString)
      if (i % 1000 == 0) {
        System.out.print(".")
      }

      {
        i += 1; i - 1
      }
    }
    Future.successful(Ok("Hello"))
  }

  def getFromDb() = Action.async { implicit request: Request[AnyContent] =>
    val requestDef = request.attrs(Router.Attrs.HandlerDef)
    val routeName = requestDef.path
    val routeMethod = request.method
    val span = tracer.buildSpan(s"$routeMethod $routeName")
      .withTag("uri", request.uri)
      .start()

    val query = sql"""SELECT namespace FROM customer""".as[String]
    db.run(query).map { s =>
      tracer.scopeManager().activate(span)
      span.setTag("query.result", s.mkString("\n"))
      Ok(s.mkString("\n"))
    }(ec).map{ x =>
      span.finish()
      x
    }(ec)
  }

  def printString(str: String) = Action.async { implicit request: Request[AnyContent] =>
    val requestDef = request.attrs(Router.Attrs.HandlerDef)
    val routeName = requestDef.path
    val routeMethod = request.method
    val span = tracer.buildSpan(s"$routeMethod $routeName")
      .withTag("uri", request.uri)
      .start()

    Future {
      Thread.sleep(5000)
      tracer.scopeManager().activate(span)
      span.setTag("result", str)
      Ok(str)
    }(ec).map{ x =>
      span.finish()
      x
    }(ec)
  }

  def index() = Action.async { implicit request: Request[AnyContent] =>
    val requestDef = request.attrs(Router.Attrs.HandlerDef)
    val routeName = requestDef.path
    val routeMethod = requestDef.method
    val span = tracer.buildSpan(s"$routeMethod $routeName")
      .withTag("uri", request.uri)
      .start()

    Future {
      Thread.sleep(5000)
      tracer.scopeManager().activate(span)
      span.setTag("result1", 42)
      Ok(views.html.index())
    }(ec).map{ x =>
      span.finish()
      x
    }(ec)
  }
}
