 package de.pf.cameldemo;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        from("file:src/data?noop=true")
        .process(Utils.javascript("convert.js"))//hier wird die Datei convert.js aufgerufen und der Inhalt wird umformatiert
        //.enrich: hier erfolgt der Zugriff auf eine andere Route, um zusätzliche Informationen abzufragen
        .enrich("direct:geocoder")
        .log("${body}") //file-Inhalt wird ausgegeben
        .to("file:dest");//file wird kopiert
    }

}
