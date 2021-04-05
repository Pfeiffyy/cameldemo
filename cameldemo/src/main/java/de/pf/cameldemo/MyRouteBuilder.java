package de.pf.cameldemo;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import static de.pf.cameldemo.Utils.javascript;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

	/**
	 * Let's configure the Camel routing rules using Java code...
	 */
	public void configure() {
		//@formatter:off

        from("file:src/data?noop=true")
        .process(javascript("convert.js"))//hier wird die Datei convert.js aufgerufen und der Inhalt wird umformatiert
        .enrich("direct:geocoder",Utils.headerEnricherStrategy("ziel")) //.enrich: hier erfolgt der Zugriff auf eine andere Route, um zusätzliche Informationen abzufragen
        .log("Ziel: ${header:ziel}")
        .log("${body}") //file-Inhalt wird ausgegeben
        .to("file:dest");//file wird kopiert
        
        /*
         * INPUT:
         * body: Bestellung als JSON-Object
         * Output:
         * body: Geokoordinate --> http://localhost:8180/restservice/webapi/myresource/geo?n1=Stein
         */
//        from("direct:geocoder")
//        .setProperty("plz").jsonpath("$.adresse.plz")//hiermit extrajhieren wir den Wert der PLZ aus dem JSON-Objekt und schreiben es in die Property PLZ
//        .setProperty("city").jsonpath("$.adresse.city")//hiermit extrajhieren wir den Wert der PLZ aus dem JSON-Objekt und schreiben es in die Property PLZ
//        .log("PLZ: ${exchangeProperty.plz}")
//        //Zugriff auf den Webservice
//        //.setHeader(Exchange.HTTP_PATH,simple("/restservice/webapi/myresource/geo"))
//        //.setHeader(Exchange.HTTP_QUERY,simple("n1=${exchangeProperty.plz}"))
//        .setHeader(Exchange.HTTP_METHOD,simple("GET"))       
//        .to("jetty:http://www.google.co.uk?bridgeEndpoint=true&throwExceptionOnFailure=false")
//        .log("${body}")
//        .setBody(constant("9.053553148,48.5236164"));
//        //@formatter:on
        
        
        from("direct:geocoder")
        .setProperty("plz").jsonpath("$.adresse.plz")
        .setProperty("city").jsonpath("$.adresse.city")
              .setHeader(Exchange.HTTP_PATH, simple("/geocoding/"))
              .setHeader(Exchange.HTTP_QUERY, simple("address='${exchangeProperty.plz} ${exchangeProperty.city} DE'"))
              .setHeader(Exchange.HTTP_METHOD, simple("GET"))
              .to("jetty:http://services.gisgraphy.com?bridgeEndpoint=true")
              .setProperty("latitude", xpath("/results/result[geocodingLevel='CITY']/lat/text()"))
              .setProperty("longitude", xpath("/results/result[geocodingLevel='CITY']/lng/text()"))
              .setBody().simple("${exchangeProperty.latitude},${exchangeProperty.longitude}")
              .log("${body}");
  
        
        
     
        
        
	}

}
