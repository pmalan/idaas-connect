// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;
import java.util.Map;
import org.apache.camel.model.dataformat.JsonLibrary;
import java.util.HashMap;
import java.util.Base64;

public class SendMessage extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      // Write your routes here, for example:
      from("knative:channel/wf-response")
        .routeId("java")
        .convertBodyTo(String.class)
        .removeHeader("*")
        .unmarshal().json(JsonLibrary.Jackson,Map.class)
        .setBody()
        .simple("${body[payload]}")
        .convertBodyTo(String.class)
        .process(exchange -> {
              exchange.getIn().setBody(new String(Base64.getDecoder().decode(exchange.getIn().getBody(String.class).getBytes())));
          
          })    
        .unmarshal().json(JsonLibrary.Jackson,Map.class)
        
        .setBody()
          .simple("iDaas: ${body[resourceType]} with ID: ${body[id]} successful")
          // properties:
          //authorizationToken: '2005542532:AAGCekO5pbXrcJqinfnoQick-hSW1cx16r8'
      //chatId: t.me/pietersmalan_bot
      //message: Hello world
        .to("telegram:bots?authorizationToken=2005542532:AAGCekO5pbXrcJqinfnoQick-hSW1cx16r8&chatId=2083772066");

  }
}
