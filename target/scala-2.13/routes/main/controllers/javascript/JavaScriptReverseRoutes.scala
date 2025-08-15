// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:3
package controllers.javascript {

  // @LINE:3
  class ReverseBookingController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:3
    def graphql: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.BookingController.graphql",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "graphql"})
        }
      """
    )
  
  }


}
