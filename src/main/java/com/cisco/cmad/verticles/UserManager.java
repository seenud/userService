package com.cisco.cmad.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.cisco.cmad.dto.UserDTO;
import com.cisco.cmad.jwt.JwtTokenUtil;
import com.cisco.cmad.mongodb.MongoDBUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserManager extends AbstractVerticle {
	Logger logger = Logger.getLogger(UserManager.class.getName());
	ObjectMapper mapper = new ObjectMapper();
	Datastore dataStore = MongoDBUtil.getMongoDB();
	JwtTokenUtil jwtUtil = new JwtTokenUtil();

	public void addUser(RoutingContext routingContext) {
		logger.log(Level.INFO,
				"Inside addUser API!! " + routingContext.getBodyAsString());

		try {
			UserDTO userDTO = Json.decodeValue(
					routingContext.getBodyAsString(), UserDTO.class);
			userDTO.setId(UUID.randomUUID().toString());
			dataStore.save(userDTO);

			/*
			 * vertx.executeBlocking(future -> { dataStore.save(userDTO); }, res
			 * -> {
			 * logger.log(Level.INFO,"Added New User Details Successsfully. : "
			 * + res.result()); });
			 */

			routingContext
					.response()
					.setStatusCode(201)
					.putHeader("content-type",
							"application/json; charset=utf-8")
					.end(Json.encodePrettily(userDTO));

		} catch (Exception e) {
			logger.log(Level.INFO,
					"Exception while adding user " + e.getMessage());
		}

	}

	public void updateUser(RoutingContext routingContext) {

	}

	/*
	 * public boolean validateUser(RoutingContext routingContext) {
	 * logger.log(Level.INFO, "Inside validateUser API!! " +
	 * routingContext.getBodyAsString()); try { UserDTO frontDto =
	 * mapper.readValue(routingContext.getBodyAsString(), UserDTO.class);
	 * logger.log(Level.INFO, "The given userName is :" +
	 * frontDto.getUserName()); Query<UserDTO> q = dataStore.find(UserDTO.class,
	 * "userName", frontDto.getUserName()); if(q != null) { UserDTO dbDTO =
	 * q.get(); if ((dbDTO != null) && (dbDTO != null) && dbDTO.getUserName() !=
	 * null && dbDTO.getPassword() != null &&
	 * (dbDTO.getUserName().equals(frontDto.getUserName())) &&
	 * (dbDTO.getPassword().equals(frontDto.getPassword()))) {
	 * logger.log(Level.INFO, "User Successfully Authenticated !!! " +
	 * routingContext.getBodyAsString()); routingContext .response()
	 * .setStatusCode(201) .putHeader("content-type",
	 * "application/json; charset=utf-8").end(); Session session =
	 * routingContext.session(); session.put("userName", dbDTO); return true; }
	 * }else { logger.log(Level.INFO, "User Not Authenticated !!! " +
	 * routingContext.getBodyAsString()); return false;
	 * 
	 * } } catch (Exception e) { logger.log(Level.SEVERE,
	 * " Exception while validating the user!!! " + e.getMessage()); } return
	 * false; }
	 */

	public void validateUser(RoutingContext routingContext) {
		logger.log(Level.INFO,
				"Inside validateUser API!! " + routingContext.getBodyAsString());
		try {
			UserDTO dto = mapper.readValue(routingContext.getBodyAsString(),
					UserDTO.class);
			logger.log(Level.INFO,
					"The given userName is :" + dto.getUserName());
			Query<UserDTO> q = dataStore.find(UserDTO.class, "userName",
					dto.getUserName());
			if ((q != null) && (q.get() != null)
					&& q.get().getUserName() != null
					&& q.get().getPassword() != null
					&& (q.get().getUserName().equals(dto.getUserName()))
					&& (q.get().getPassword().equals(dto.getPassword()))) {
				logger.log(Level.INFO, "User Successfully Authenticated !!! "
						+ routingContext.getBodyAsString());
				
//				Session session = routingContext.session();
//				session.put("userData", q.get());
				
				dto.setJwtToken(jwtUtil.generateToken(q.get().getUserName()));
				
				System.out.println("Token Generated Successfully!!!!");

				routingContext
				.response()
				.setStatusCode(201)
				.putHeader("content-type",
						"application/json; charset=utf-8").end(Json.encodePrettily(dto));
				//return q.get();
			} else {
				logger.log(Level.INFO, "User Not Authenticated !!! "
						+ routingContext.getBodyAsString());
				//return false;

			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, " Exception while validating the user!!! "
					+ e);
		}
		//return false;
	}
	
	public void validateToken(RoutingContext routingContext){
		logger.log(Level.INFO,
				"Inside validateToken API!! " + routingContext.getBodyAsString());
		String isTokenValid = null;
		String authHeader = routingContext.request().getHeader("Authorization");
		String token = null;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out
			.println("Authorization Header is invalid");
		} else {
			token = authHeader.substring(7);
			isTokenValid = jwtUtil.validateToken(token);
			if (isTokenValid != null) {
				routingContext
				.response()
				.setStatusCode(201)
				.putHeader("content-type",
						"application/json; charset=utf-8")
				.end(Json.encodePrettily(isTokenValid));
			} else {
				routingContext
				.response()
				.setStatusCode(403)
				.putHeader("content-type",
						"application/json; charset=utf-8")
				.end(Json.encodePrettily(null));
			}
		}
		
	}

}
