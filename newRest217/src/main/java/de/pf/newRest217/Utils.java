package de.pf.newRest217;

import org.apache.camel.Processor;
import org.apache.camel.builder.script.ScriptBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class Utils {
	public static Processor javascript(String scriptResourceName) {
		try {
			String driver = "jvm-npm.js";
			return new ScriptBuilder("javaScript", ""
					+ "load({name:'"+driver+"',script:com.google.common.io.CharStreams.toString("+
					"new java.io.InputStreamReader(java.lang.Thread.currentThread().getContextClassLoader()."+
					"getResourceAsStream('"+driver+"'), com.google.common.base.Charsets.UTF_8))});\n"
					+ Resources.toString(Resources.getResource(scriptResourceName), Charsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	/**
	 * Creates a strategy, which sets the additional body as a header on the original exchange.
	 */
	public static AggregationStrategy headerEnricherStrategy(String headerName) {
		return (original, newExchange) -> {
			System.out.println("***************************************************************");
			System.out.println(headerName);
			System.out.println(original);
			System.out.println(newExchange.getIn().getBody());
			System.out.println("***************************************************************");
			original.getIn().setHeader(headerName, newExchange.getIn().getBody());
			return original;
		};
	}	

}
