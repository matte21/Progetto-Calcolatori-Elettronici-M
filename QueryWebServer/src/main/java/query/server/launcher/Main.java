package query.server.launcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.studyroom.web.WebServer;

import query.server.handlers.BaseHandler;
import query.server.utils.SibConnectionFactory;
import query.server.utils.query.builder.PrefixedQueryUtilsFactory;

public class Main {

	private static final Logger LOG = LogManager.getLogger();
	
	private static final Map<String, String> PREFIXES_TO_FULL_URIs = initPrefixes();
	private static final Map<String, String> VARS_VOCABULARY = initVarsVocabulary();
	
	// FIXME These prefixes should be loaded from a config file. We were in a hurry so we hardcoded them 
	// in this method
	private static Map<String, String> initPrefixes() {
		Map<String, String> prefixesToFullURIs = new HashMap<String, String>();
		prefixesToFullURIs.put("sr", "http://www.semanticweb.org/matteo/ontologies/2016/11/OperazioneStudyRoom#");
		prefixesToFullURIs.put("locn", "http://www.w3.org/ns/locn#");
		prefixesToFullURIs.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixesToFullURIs.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		return prefixesToFullURIs;
	}

	// FIXME These var names should be loaded from a config file. We were in a hurry so we hardcoded them
	// in this method. Also, we should switch from a map to a Set
	private static Map<String, String> initVarsVocabulary() {
		Map<String, String> varsVocabulary = new HashMap<String, String>();
		varsVocabulary.put("roomID", "roomID");
		varsVocabulary.put("address", "address");
		varsVocabulary.put("roomState", "roomState");
		varsVocabulary.put("capacity", "capacity");
		varsVocabulary.put("availSeats", "availSeats");
		varsVocabulary.put("nextTransitionPredicate", "nextTransitionPredicate");
		varsVocabulary.put("nextTransitionInstant", "nextTransitionInstant");
		varsVocabulary.put("university", "university");
		varsVocabulary.put("latitude", "latitude");
		varsVocabulary.put("longitude", "longitude");
		return varsVocabulary;
	}
	
	public static void main(String[] args) throws IOException {
		LOG.debug("Initializing connection factory...");
		initSibConnectionFactory(args[0], Integer.parseInt(args[1]), args[2]);	
		LOG.debug("Connection factory successfully initialized.");

		
		LOG.debug("Initializing queries utils factory..."); 
		initQueryUtilsFactory(PREFIXES_TO_FULL_URIs, VARS_VOCABULARY);
		LOG.debug("Queries utils successfully initialized.");
		
		WebServer server = WebServer.getInstance();		

		LOG.debug("Adding routes to web server...");		
		server.addRoute("/", BaseHandler.class);
		LOG.debug("Routes successfully added to web server.");	

		LOG.info("Query web server is about to start...");
		// I don't know why but with the second argument set to true (AKA daemon mode on) it won't start
		server.start(0, false);
		LOG.info("Query web server is now listening on port 80.");
	}

	private static void initSibConnectionFactory(String sibIPorHost, int sibPort, String smartSpaceName) {
		SibConnectionFactory.init(sibIPorHost, sibPort, smartSpaceName);		
	}
	
	private static void initQueryUtilsFactory(Map<String, String> prefixesToFullURIs,
			Map<String, String> varsVocabulary) {
		
		PrefixedQueryUtilsFactory.init(prefixesToFullURIs, varsVocabulary);
	}
	
}








