package com.cisco.cmad.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BootStrapVerticle extends AbstractVerticle {
	Logger logger = Logger.getLogger(BootStrapVerticle.class.getName());
	ObjectMapper mapper = new ObjectMapper();
	UserManager userManager = new UserManager();

	@Override
	public void start(Future<Void> startFuture){
		logger.log(Level.INFO,"Inside BootStrapVerticle start API !!");
		
		// Create a router object.
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		// Start HTTP server and listen to port 8080.
		router.route("/")
				.handler(
						routingContext -> {
							HttpServerResponse response = routingContext
									.response();
							response.putHeader("content-type", "text/html")
									.end("<h1>CMAD Bootstrap</h1>");
						});
		router.route().handler(CookieHandler.create());
		router.route().handler(
                SessionHandler.create(LocalSessionStore.create(vertx)).setNagHttps(false)); 
	
		router.post("/api/user/addUser").handler(userManager::addUser);
		router.post("/api/user/updateUser/:userId").handler(userManager::updateUser);
		router.post("/api/user/validateUser").handler(userManager::validateUser);
		router.get("/api/user/validateToken").handler(userManager::validateToken);
		router.route().handler(StaticHandler.create()::handle);

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(
		// Retrieve the port from the configuration,
		// default to 8080.
				config().getInteger("http.port", 8989), result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});
        logger.log(Level.INFO,"Started HTTP Server Successfully on port 8989 !!");
	}
}
