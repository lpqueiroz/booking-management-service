package testcontainers

import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future

class DummyLifecycle extends ApplicationLifecycle {
  override def addStopHook(hook: () => Future[_]): Unit = {
    // Do nothing in tests
  }

  override def stop(): Future[_] = ???
}
