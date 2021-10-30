// camel-k: language=java name=IDaaSPublisher

//import java.util.Map;
//import java.util.HashMap;

import org.apache.camel.builder.RouteBuilder;




public class iDaaSPublish extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("knative:channel/idaas-event-channel")
            .to("kafka:idaas.incoming.connector?brokers=daas-cluster-kafka-0.daas-cluster-kafka-brokers.amqstreans.svc:9092&groupId=idaas-kafka");
            


        
    }
     
}
