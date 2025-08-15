// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseBookingController BookingController = new controllers.ReverseBookingController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseBookingController BookingController = new controllers.javascript.ReverseBookingController(RoutesPrefix.byNamePrefix());
  }

}
