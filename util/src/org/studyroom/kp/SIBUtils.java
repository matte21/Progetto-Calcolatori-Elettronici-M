package org.studyroom.kp;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.jdom2.*;
import org.jdom2.input.*;
import sofia_kp.*;

public class SIBUtils {
	private static final Map<String,String> ns=new HashMap<>();
	
	//START application specific data:
	public static final String SMART_SPACE_NAME="StudyRoom";
	static {
		initNamespaces(SIBUtils.class.getResourceAsStream("/ontology.owl"));
	}
	//END application specific data
	
	public static String getNS(String pref){
		return ns.getOrDefault(pref,"");
	}
	
	/**Load namespaces to use when make triples 
	 * @param xml - a RDF/XML document from which load namespaces */
	public static void initNamespaces(File xml){
		try {
			initNamespaces(new FileInputStream(xml));
		} catch (FileNotFoundException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	/**Load namespaces to use when make triples 
	 * @param xml - a RDF/XML document from which load namespaces */
	public static void initNamespaces(String xml){
		initNamespaces(new ByteArrayInputStream(xml.getBytes()));
	}

	/**Load namespaces to use when make triples 
	 * @param in - a RDF/XML document from which load namespaces */
	public static void initNamespaces(InputStream in){
		try {
			Element r=new SAXBuilder().build(in).getRootElement();
			for (Namespace n : r.getNamespacesInScope())
				ns.putIfAbsent(n.getPrefix(),n.getURI());
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**Transform triple elements in the format required by RDF query.
	 * Don't use default namespaces for URI elements.
	 * @param triples - Array of elements; every group of 5 elements is formed by
	 *  				subject, predicate, object, subject type and object type
	 * @return a Vector of triples, each of them stored as a Vector */
	public static Vector<Vector<String>> tripleList(String...triples){
		if (triples.length%5!=0)
			throw new IllegalArgumentException("missing arguments to make a triple");
		Vector<Vector<String>> tv=new Vector<>();
		Vector<String> t=new Vector<>(5);
		for (int i=0;i<triples.length;i++){
			if (i%5<3 && triples[i].indexOf(':')>0){
				String[] uri=triples[i].split(":",2);
				String fullNS=ns.get(uri[0]);
				if (fullNS!=null)
					triples[i]=fullNS+uri[1];
				else
					throw new IllegalArgumentException(uri[0]+": namespace sconosciuto");
			}
			t.add(triples[i]);
			if (i%5==4){
				tv.add(t);
				t=new Vector<>(5);
			}
		}
		return tv;
	}
	
	/**Transform a RDF/XML document in the format required by RDF query.
	 * @param xml - a RDF/XML document
	 * @return a Vector of triples, each of them stored as a Vector */
	public static Vector<Vector<String>> tripleListFromXML(String xml){
		try {
			Element r=new SAXBuilder().build(new ByteArrayInputStream(xml.getBytes())).getRootElement();
			for (Namespace n : r.getNamespacesInScope())
				ns.putIfAbsent(n.getPrefix(),n.getURI());
			Vector<Vector<String>> tv=new Vector<>();
			String[] t={"","","","URI","URI"};
			System.out.println(r.getQualifiedName());
			System.out.println(r.getValue());
			for (Element s : r.getChildren()){
				t[0]=s.getAttributeValue("about",s.getNamespace("rdf"));
				if (!(s.getNamespacePrefix().equals("rdf")&& s.getName().equals("Description"))){
					t[1]=ns.get("rdf")+"type";
					t[2]=s.getNamespaceURI()+s.getName();
					t[4]="URI";
					tv.add(new Vector<>(Arrays.asList(t)));
				}
				for (Element p : s.getChildren()){
					t[1]=p.getNamespaceURI()+p.getName();
					Attribute a=p.getAttribute("resource",s.getNamespace("rdf"));
					if (a!=null){
						t[2]=a.getValue();
						t[4]="URI";
					} else {
						t[2]=p.getValue();
						t[4]="literal";
					}
					tv.add(new Vector<>(Arrays.asList(t)));
				}
			}
			return tv;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**Shortcut method to get the result list from a SPARQL SELECT query.
	 * To access each value of a result, use the {@link SSAP_sparql_response.getCellValue()} function
	 * or the {@code getID()}, {@code getInt()} and {@code getString()} functions.
	 * @param sib - the SIB to which send the query
	 * @param query - the SPARQL query
	 * @return the results list */
	public static Vector<Vector<String[]>> query(KPICore sib, String query){
		return sib.querySPARQL(query).sparqlquery_results.getResults();
	}
	
	/**Shortcut method to get the result from a SPARQL ASK query.
	 * @param sib - the SIB to which send the query
	 * @param query - the SPARQL query
	 * @return the query answere */
	public static boolean askQuery(KPICore sib, String query){
		//return sib.querySPARQL(query).sparqlquery_results.getBooleans().get(0).equalsIgnoreCase("true");
		SIBResponse r=sib.querySPARQL(query);
		return r.sparqlquery_results.getBooleans().get(0).equalsIgnoreCase("true");
	}
	
	/**Prints the SIB response to the standard output in case of success
	 * or to the standard error otherwise
	 * @param r - the SIB response to print */
	public static void printSIBResponse(SIBResponse r){
		if (!r.isConfirmed()){
			System.err.println(r.TransactionType+" fallito:");
			printXml(r,System.err);
		} else
			printXml(r);
	}
	
	/**Print an XML-like object to the standard output (without indentation).
	 * @param xml - an XML-like object */
	public static void printXml(Object xml){
		System.out.println(xml.toString().replaceAll("><",">\r\n<"));
		System.out.println();
	}
	
	/**Print an XML-like object to the specified PrintStream (without indentation).
	 * @param xml - an XML-like object
	 * @param out - the PrintStream in which print */
	public static void printXml(Object xml, PrintStream out){
		out.println(xml.toString().replaceAll("><",">\r\n<"));
		out.println();
	}
	
	/**Prepare the PREFIX clauses of a SPARQL query
	 * @param namespacePref - list of prefixes used in the query
	 * @return The PREFIX clauses of the query */
	public static String sparqlPrefix(String...namespacePref){
		return Arrays.stream(namespacePref).map(p->"PREFIX "+p+":<"+getNS(p)+">").collect(Collectors.joining("\n","","\n"));
	}
	
	/**undo char transforms maked by the KP before sending message */
	public static String decodeXMLChars(String s){
		return s.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&apos;","'").replaceAll("&quot;","\"").replaceAll("&amp;","&");
	}
	
	/**@return the ID of the URI at position {@code pos} of a query result */
	public static String getID(List<String[]> result, int pos){
		return removeNS(SSAP_sparql_response.getCellValue(result.get(pos)));
	}
	
	/**@return the ID of the URI at position {@code pos} of a query result */
	public static String getID(String[] resultForVar){
		return removeNS(SSAP_sparql_response.getCellValue(resultForVar));
	}
	
	/**@return the value at position {@code pos} of a query result */
	public static String getString(List<String[]> result, int pos){
		return decodeXMLChars(SSAP_sparql_response.getCellValue(result.get(pos)));
	}
	
	/**@return the value at position {@code pos} of a query result */
	public static String getString(String[] resultForVar){
		return decodeXMLChars(SSAP_sparql_response.getCellValue(resultForVar));
	}
	
	/**@return the value at position {@code pos} of a query result */
	public static int getInt(List<String[]> result, int pos){
		return Integer.parseInt(SSAP_sparql_response.getCellValue(result.get(pos)));
	}
	
	/**@return the value at position {@code pos} of a query result */
	public static int getInt(String[] resultForVar){
		return Integer.parseInt(SSAP_sparql_response.getCellValue(resultForVar));
	}
	
	/**Removes the namespace from the URI */
	public static String removeNS(String uri){
		int i=uri.indexOf("#");
		return i<0?uri:uri.substring(i+1);
	}
}
