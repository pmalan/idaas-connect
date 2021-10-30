// camel-k: language=java dependency=camel-jackson 

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.Map;

public class ContentProcessor extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      // Write your routes here, for example:
      from("kafka:idaas.incoming.connector?brokers=daas-cluster-kafka-0.daas-cluster-kafka-brokers.amqstreans.svc:9092&groupId=idaas-kafka-contentprocessor")
        
        .routeId("ContentProcessor")
        .log("${body}")
        .convertBodyTo(String.class)
        .removeHeader("*")
        .unmarshal().json(JsonLibrary.Jackson,Map.class)
        
        //.process(exchange -> {
        //  exchange.getIn().getBody().to;
          
        //})
        .log("${body[format]}")
        .setHeader("ce-type",simple("${body[format]}"))
        .setHeader("ce-subject",simple("${body[resource]}"))
        .setHeader("ce-resourceId",simple("${body[resourceId]}"))
        .setHeader("ce-exchangeId",simple("${body[exchangeId]}"))
        .setHeader("ce-resource",simple("${body[resource]}"))
        .setHeader("ce-synchronous",simple("${body[synchronous]}")) 
        .setHeader("ce-operation",simple("${body[operation]}"))
        .setHeader("ce-correlationId",simple("${body[correlationId]}"))
        .setHeader("ce-channel",simple("${body[channel]}"))

        .setBody()
        .simple("${body[payload]}")
        .to("knative:endpoint/default");

  }
}
