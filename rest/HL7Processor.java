
// camel-k: language=java name=HL7Processor dependency=camel-jackson dependency=camel-fhir
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.PredicateBuilder;

import java.util.Map;
import org.apache.camel.model.dataformat.JsonLibrary;
import java.util.HashMap;
import java.util.Base64;
import ca.uhn.fhir.rest.api.PreferReturnEnum;



public class HL7Processor extends RouteBuilder {

    Map<String, String> eventPayload = new HashMap<String, String>();
    ca.uhn.fhir.rest.api.PreferReturnEnum preferReturn;
   
    @Override
    public void configure() throws Exception {

        from("knative:endpoint/default").routeId("HL7Processor")
            .choice()
                .when( PredicateBuilder.and( header("ce-resource").isEqualTo("Claim"),header("ce-operation").isEqualTo("POST")))
                    .log(" --> Workflow required")
                    //.setHeader("ce-operation",simple("workflow-request"))
                    .setHeader("ce-type",simple("workflow-request"))
                    .process(exchange -> { 
          
                        eventPayload.clear();
                        eventPayload.put("resource",exchange.getIn().getHeader("ce-resource").toString());
                        eventPayload.put("resourceId",exchange.getIn().getHeader("ce-resourceId").toString());
                        eventPayload.put("excahngeId",exchange.getExchangeId());
                        eventPayload.put("channel",exchange.getIn().getHeader("ce-channel").toString());
                        eventPayload.put("operation",exchange.getIn().getHeader("ce-operation").toString());
                        eventPayload.put("correlationId",exchange.getIn().getHeader("ce-correlationId").toString()); 
                        eventPayload.put("synchronous",exchange.getIn().getHeader("ce-synchronous").toString());
                        eventPayload.put("format","HL7");
                        eventPayload.put("payload",exchange.getIn().getBody(String.class));
                        exchange.getIn().setBody(eventPayload);

                    })
                    .marshal().json(JsonLibrary.Jackson,Map.class)
                    .convertBodyTo(String.class)
                    
                    //.to("knative:endpoint/default")
                    .to("knative:endpoint/claims-process-wf") 
                    .log("------ Claim and POST -> Received: ${body[resource]}")
                    .to("log:info?multiline=true&showAll=true")
                    .endChoice()
                .otherwise()
                    .log(" --> No associated workflow")
                     
                //.unmarshal().json(JsonLibrary.Jackson, Map.class)
                    .log("Received: ${body}")
                    .to("log:info?multiline=true&showAll=true");
        from("knative:channel/wf-response")
            .routeId("ContentProcessor")
            .log("${body}")
            .convertBodyTo(String.class)
            .removeHeader("*")
            .unmarshal().json(JsonLibrary.Jackson,Map.class)
        
            .log("${body[format]}")
            .setHeader("ce-type",simple("${body[format]}"))
            .setHeader("ce-subject",simple("${body[resource]}"))
            .setHeader("ce-resourceId",simple("${body[resourceId]}"))
            .setHeader("ce-exchangeId",simple("${body[exchangeId]}"))
            .setHeader("ce-resource",simple("${body[resource]}"))
            .setHeader("ce-synchronous",simple("${body[synchronous]}")) 
            .setHeader("ce-correlationId",simple("${body[correlationId]}"))
            .setHeader("ce-channel",simple("${body[channel]}"))
            .setHeader("ce-operation",simple("POST"))

            .setBody()
            .simple("${body[payload]}")
            .convertBodyTo(String.class)
            .log(" --> Workflow response")
            .to("log:info?multiline=true&showAll=true")
            .to("direct:fhir-interaction");


        from("direct:fhir-interaction")
            .choice()
                .when(header("ce-operation").isEqualTo("POST"))
                    .process(exchange -> {
                        exchange.getIn().setBody(new String(Base64.getDecoder().decode(exchange.getIn().getBody(String.class).getBytes())));
                    
                    }) 
                    .convertBodyTo(String.class)
                    .setHeader("CamelFhir.resource",simple("${header.ce-resource}"))
                    .setHeader("CamelFhir.serverUrl",simple("https://fhir-server.apps.os.pietersmalan.com"))
                    .to("fhir:create/resource?encoding=JSON&inBody=resourceAsString&fhirVersion=R4&serverUrl=https://fhir-server.apps.os.pietersmalan.com/fhir/&exchangePattern=InOut")
                    .convertBodyTo(String.class)
                    .to("log:info?multiline=true&showAll=true")
                .when(header("ce-operation").isEqualTo("GET"))
                    .to("fhir:read/resourceById?encoding=JSON&serverUrl=https://fhir-server.apps.os.pietersmalan.com");

    }

}
 