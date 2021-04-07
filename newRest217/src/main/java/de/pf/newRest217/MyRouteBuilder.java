package de.pf.newRest217;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.model.SetHeaderDefinition;

import static de.pf.newRest217.Utils.javascript;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

	/**
	 * Let's configure the Camel routing rules using Java code...
	 */
	public void configure() {
		//Tracing anschalten
		//getContext().setTracing(true);

		
		//activeMQ registrieren
		getContext().addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616"));
		
		//@formatter:off

        //er nimmt alle Dateien, die in das Verzeichnis Dest kommen und verarbeitet sie
		//from("file:src/data?noop=true")
		
        
		//das heißt, wir schucken an diese Adresse einen Request POST) über Postman
		from("jetty:http://0.0.0.0:8081/orders/")
         //hier wird die Datei convert.js aufgerufen und der Inhalt wird umformatiert
        .process(javascript("convert.js"))
         //.enrich: hier erfolgt der Zugriff auf eine andere Route, um zusätzliche Informationen abzufragen, dies ist der Zugriff auf den Restservice unten
        .enrich("direct:geocoder",Utils.headerEnricherStrategy("ziel")) 
        
        .log("Ziel: ${header.ziel}")
        .filter(header("ziel").isEqualTo(""))
    		.setHeader(Exchange.HTTP_RESPONSE_CODE,constant(400))
    		.setHeader(Exchange.CONTENT_TYPE,constant("application/jason"))
    		.setBody(constant("{\"error\":\"Ziel nicht gefunden.\"}"))
    		.stop()
    	.end()
        //file-Inhalt wird ausgegeben
        .log("${body}") 
         //file wird nach Folder dest kopiert
        //.to("file:dest");
        //Übertragung zu ActiceMQ
        //.to("activemq:bestellung")
        .inOnly("activemq:bestellung")
		.setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204));
        
        
        //Route zum Geocoder 
		from("direct:geocoder")
        .setProperty("plz").jsonpath("$.adresse.plz")
        .setProperty("city").jsonpath("$.adresse.city")
        .log("PLZ: ${property.plz}")
        
              .setHeader(Exchange.HTTP_PATH, simple("restservice/webapi/myresource/geo"))
              .setHeader(Exchange.HTTP_QUERY, simple("n1=${property.city}"))
              .setHeader(Exchange.HTTP_METHOD, simple("GET"))
              .to("jetty:http://localhost:8080?bridgeEndpoint=true")
              //Hier hole ich mir die daten aus dem XML-File, das vom Rest-Service kommt
              // .setBody().xpath("//gml:pos/text()",new Namespaces("gml","http://abc.de/x/y/z")) --> mit namespace
              .setBody().xpath("//pos/text()")
              //hier wird nun durch einen regulären Ausdruck im Body (bzw. dem Strint) jedes Blank durch ein Komma ersetzt
              .setBody(body().regexReplaceAll(" ", ","))
              .log("${body}");
        
         
        
        //hier wird die Nachricht des namens Bestellung wieder vom ActiveMQ abgeholt und verarbeitet
        //dieses kläuft in einem seperaten Thread, ich kann dies in 2 Threds aufteilen, from("activemq:bestellung?concurrentComsumer=2")
        from("activemq:bestellung")
        .log("Verabeitung beginn: ${header.ziel}")
        .to("file:dest");
        
        
        

		//@formatter:on

	}

}
