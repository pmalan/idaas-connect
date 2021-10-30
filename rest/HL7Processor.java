// camel-k: language=java name=HL7Processor dependency=camel-jackson 
import org.apache.camel.builder.RouteBuilder;

import java.util.Map;
import org.apache.camel.model.dataformat.JsonLibrary;
import java.util.HashMap;

public class HL7Processor extends RouteBuilder {

    Map<String,String> eventPayload=new HashMap<String,String>();
    @Override
    public void configure() throws Exception {

        from("knative:endpoint/default").routeId("HL7Processor")
                .log("Resource: ${header.ce-resource}")
                .log("Operation: ${header.ce-operation}")
                
                
                .choice()
                    .when(simple("${header.ce-resource} == 'Claim' && ${header.ce-operation} == 'POST'"))
                        .log(" --> Workflow required")
                        .setHeader("ce-type",simple("workflow-request"))
                        .setHeader("ce-source",simple("HL7Processor"))
                        .process(exchange -> { 
          
                            eventPayload.clear();
                            eventPayload.put("resource",exchange.getIn().getHeader("ce-resource").toString());
                            eventPayload.put("resourceId",exchange.getIn().getHeader("ce-resourceId").toString());
                            eventPayload.put("excahngeId",exchange.getExchangeId());
                            eventPayload.put("channel",exchange.getIn().getHeader("ce-channel").toString());
                            eventPayload.put("operation","workflow-request");
                            eventPayload.put("correlationId",exchange.getIn().getHeader("ce-correlationId").toString()); 
                            eventPayload.put("synchronous",exchange.getIn().getHeader("ce-synchronous").toString());
                            eventPayload.put("format","HL7");
                            eventPayload.put("payload",exchange.getIn().getBody(String.class));
                            exchange.getIn().setBody(eventPayload);

                        })
                        .marshal().json(JsonLibrary.Jackson,Map.class)
                        //.convertBodyTo(String.class)
                        
                        .to("knative:channel/wf-response")
                    .otherwise()
                        .log(" --> No associated workflow")
                    
                .end();
                //.unmarshal().json(JsonLibrary.Jackson, Map.class)
                //.log("Received: ${body[resource]}")
                //.to("log:info?multiline=true&showAll=true");
        from("knative:channel/wf-response")
            .log(" --> Workflow response")
            .to("log:info?multiline=true&showAll=true");
        

    }

}
