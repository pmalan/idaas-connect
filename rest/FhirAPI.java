// camel-k: language=java trait=route.enabled=true name=FHIR-REST-API

import java.util.Map;
import java.util.Base64;
import java.util.HashMap;

import org.apache.camel.builder.RouteBuilder;



public class FhirAPI extends RouteBuilder {



  Map<String,String> eventPayload=new HashMap<String,String>();
  @Override
  public void configure() throws Exception {

    rest("/fhir")
      
	    .get("/{resource}/{id}")
        .route()
        .routeId("FHIRAPI-get-resource")
        .setHeader("resource",simple("${header.resource}"))
        .setHeader("operation",simple("GET"))
        .setHeader("id",simple("${header.id}"))
        .setHeader("ContentType",simple("application/json"))
        .setHeader("format",simple("HL7"))
        .process(exchange -> {
          
          eventPayload.clear();
          eventPayload.put("resource",exchange.getIn().getHeader("resource").toString());
          eventPayload.put("resourceId",exchange.getIn().getHeader("id").toString());
          eventPayload.put("excahngeId",exchange.getExchangeId());
          eventPayload.put("channel","HTTP");
          eventPayload.put("operation","GET");
          eventPayload.put("correlationId",exchange.getExchangeId()); 
          eventPayload.put("synchronous","true");
          eventPayload.put("format","HL7");
          eventPayload.put("payload",""); 
          exchange.getIn().setBody(eventPayload);
        }) 
        .marshal().json()
        .to("knative:channel/idaas-event-channel")
        .transform().simple("{ \"claimId\": 5234, \"ClaimedSubmitted\": \"OK\"}")
        
        .endRest()
      
      .post("/{resource}")
        .route()
        .routeId("FHIRAPI-post-resource")
        .setHeader("resource",simple("${header.resource}"))
        .setHeader("operation",simple("POST"))
        .setHeader("id",simple(""))
        .setHeader("ContentType",simple("application/json"))
        .setHeader("format",simple("HL7"))
        .setHeader("channel",simple("HTTP"))
        .setHeader("synchronous",simple("true"))
        .process(exchange -> {
        
          eventPayload.clear();
          eventPayload.put("resource",exchange.getIn().getHeader("resource").toString());
          eventPayload.put("resourceId","");
          eventPayload.put("excahngeId",exchange.getExchangeId());
          eventPayload.put("channel","HTTP");
          eventPayload.put("operation","POST");
          eventPayload.put("correlationId",exchange.getExchangeId()); 
          eventPayload.put("synchronous","true");
          eventPayload.put("format","HL7");
          eventPayload.put("payload",Base64.getEncoder().encodeToString(exchange.getIn().getBody(String.class).getBytes())); 
          exchange.getIn().setBody(eventPayload);
        }) 
      .marshal().json()
      .to("knative:channel/idaas-event-channel")
      .transform().simple("{ \"claimId\": 5234, \"ClaimedSubmitted\": \"OK\"}")
      
      .endRest();
  }

  


}
