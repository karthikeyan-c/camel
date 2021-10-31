package com.kc.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CamelAppExamples extends RouteBuilder {

    Customer customer = new Customer();

    @Override
    public void configure() throws Exception {
        // 1. Rest API expose.
        restConfiguration()
                .component("undertow")
                .contextPath("api")
//                .apiContextPath("/api-doc")
                .host("localhost")
                .port("8081");

        rest("/v1")
                .get("/customer/{custId}").description("this is get api")
                .to("direct:getcustomer")
                .post("/customer").description("this is post api")
                .to("direct:postcustomer");

        from("direct:getcustomer")
                .log("getting value ${header.custId}")
                .process(exchange -> {
                    String custId = (String) exchange.getIn().getHeader("custId");
                    log.info("Inside GET for : "+ custId);
                    exchange.getIn().setBody("Returning: " + customer.getElement(custId));
                });

        from("direct:postcustomer")
                .log("getting value to post ${body}")
                .unmarshal().json(JsonLibrary.Gson, Customer.class)
                .process(exchange -> {
                    Customer body = (Customer) exchange.getIn().getBody();
//                    log.info(body.toString());
                    customer.addElement(customer);
                    exchange.getIn().setBody(body);
//                    Customer newCustomer = (Customer) exchange.getIn().getBody();
//                    exchange.getIn().setBody("Returning ==>" + newCustomer);
//                    customer.addElement(newCustomer);
//                    exchange.getIn().setBody(customer.getElement(newCustomer.getCustId()));
                })
                .marshal().json(JsonLibrary.Gson);

        // 2. Http PUll.
        from("timer:poll?period=5000")
                .to("http://localhost:8081/api/v1/customer/cust1")
                .log("logging values ${body} ");

        from("timer:poll?period=10000")
                .process(exchange -> {
                    Customer customer = new Customer();
                    customer.setCustId("1");
                    customer.setCustName("KC");
                    exchange.getIn().setBody(customer);
                })
                .marshal().json(JsonLibrary.Gson)
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to("http://localhost:8081/api/v1/customer")
                .log("response values ${body}");
    }
}
