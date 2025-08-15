package modules

import cats.effect.unsafe.IORuntime
import com.google.inject.{AbstractModule, Provides, Singleton}

class IORuntimeModule extends AbstractModule {

  @Provides
  @Singleton
  def provideIORuntime(): IORuntime = IORuntime.global
}
