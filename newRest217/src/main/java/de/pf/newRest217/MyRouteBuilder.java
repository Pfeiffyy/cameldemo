package de.pf.newRest217;


import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import static de.pf.newRest217.Utils.javascript;


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
        
        from("direct:geocoder")
        .setProperty("plz").jsonpath("$.adresse.plz")
        .setProperty("city").jsonpath("$.adresse.city")
        .log("PLZ: ${property.plz}")
        
              .setHeader(Exchange.HTTP_PATH, simple("restservice/webapi/myresource/geo"))
              .setHeader(Exchange.HTTP_QUERY, simple("n1=Stein"))
              .setHeader(Exchange.HTTP_METHOD, simple("GET"))
              .to("jetty:http://localhost:8180?bridgeEndpoint=true")
              .log("${body}");
        
        
        
        
//        from("direct:geocoder")
//        .setBody(constant("9.99,48.7777"));
		//@formatter:on
   
    }

}
