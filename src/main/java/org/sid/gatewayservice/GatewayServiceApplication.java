package org.sid.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableHystrix
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

   @Bean
   RouteLocator staticRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r->r.path("/customers/**").uri("lb://CUSTOMER-SERVICE").id("r1"))
                .route(r->r.path("/products/**").uri("lb://INVENTORY-SERVICE").id("r2"))
                .route(r->r.path("/publicCountries/**")
                        .filters(f->f
                                .addRequestHeader("x-rapidapi-host","restcountries-v1.p.rapidapi.com")
                                .addRequestHeader("x-rapidapi-key","56ea3dc6aamsh03246095835aa59p1d69d5jsndb07f873307f")
                                .rewritePath("/publicCountries/(?<segment>.*)","/${segment}")
                                .hystrix(h->h.setName("countries").setFallbackUri("forward:/defaultCountries")))
                        .uri("https://restcountries-v1.p.rapidapi.com").id("r3"))
                .route(r->r.path("/muslim/**")
                        .filters(f->f
                                .addRequestHeader("x-rapidapi-host","muslimsalat.p.rapidapi.com")
                                .addRequestHeader("x-rapidapi-key","56ea3dc6aamsh03246095835aa59p1d69d5jsndb07f873307f")
                                .rewritePath("/muslim/(?<segment>.*)","/${segment}")
                        .hystrix(h->h.setName("muslimsalat").setFallbackUri("forward:/defaultSalat")))
                        .uri("https://muslimsalat.p.rapidapi.com").id("r4"))
                .build();
    }

    @Bean
    DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp){
        return  new DiscoveryClientRouteDefinitionLocator(rdc,dlp);
    }
}
@RestController
class CircuitBreakerRestController{
    @GetMapping("/defaultCountries")
    public Map<String,String> countries(){
        Map<String,String> data = new HashMap<>();
        data.put("message","default Countries");
        data.put("countries","Maroc, Algérie, Tunisie .....");
        return data;
    }
    @GetMapping("/defaultSalat")
    public Map<String,String> salat(){
        Map<String,String> data = new HashMap<>();
        data.put("message","Horaire Salawat En NWakchout");
        data.put("Hajr","7:00");
        data.put("Addohr","14:00");
        return data;
    }
}
