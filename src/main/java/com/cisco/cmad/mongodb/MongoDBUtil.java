package com.cisco.cmad.mongodb;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBUtil {
	private static ThreadLocal<Datastore> mongoTL = new ThreadLocal<Datastore>();
	private static Logger logger = Logger.getLogger(MongoDBUtil.class.getName());

	/**
	 * Method to retrieve a mongo database client from the thread local storage
	 * 
	 * @return
	 */
	public static Datastore getMongoDB() {
		logger.log(Level.INFO," Inside the getMongoDB API..  ");
		if (mongoTL.get() == null) {
			MongoClientURI connectionString = new MongoClientURI(
					"mongodb://173.36.54.200:27018");
			MongoClient mongoClient = new MongoClient(connectionString);
			Morphia morphia = new Morphia();
			morphia.mapPackage("com.mysocial.model");
			Datastore datastore = morphia.createDatastore(mongoClient, "cmadUserDB");
			datastore.ensureIndexes();
			mongoTL.set(datastore);
			return datastore;
		}
		return mongoTL.get();
	}
}
