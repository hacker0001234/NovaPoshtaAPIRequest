package com.example.backend.Services;

import com.example.backend.DTOs.CityDTO;
import com.example.backend.DTOs.DepartmentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class MainService {
    @Value("${nova-poshta.api-key}")
    private String apiKey;
    private final WebClient webClient;

    public MainService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.novaposhta.ua/v2.0/json/")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer ->
                                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                        ).build())
                .build();
    }

    public Flux<CityDTO> getCities() {
        String request = """
            {
              "apiKey": "879f72dd0d06961104925fd3231dc2c8",
              "modelName": "Address",
              "calledMethod": "getCities",
              "methodProperties": {}
            }
            """;

        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(json -> {
                    try {
                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        var root = mapper.readTree(json);
                        var data = root.get("data");
                        return Flux.fromIterable(data)
                                .map(node -> new CityDTO(
                                        node.get("Ref").asText(),
                                        node.get("Description").asText(),
                                        node.get("AreaDescription").asText()
                                ));
                    } catch (Exception e) {
                        return Flux.error(e);
                    }
                });
    }


    public Flux<DepartmentDTO> getDepartmentsByCity(String cityRef,String page) {
        String request = """
    {
      "apiKey": "%s",
      "modelName": "Address",
      "calledMethod": "getWarehouses",
      "methodProperties": { 
        "CityRef": "%s",
        "Limit": "100",
        "Page": "%s"
      }
    }
    """.formatted(apiKey, cityRef, page);

        System.out.println("📤 Request body: " + request);

        return webClient.post()
                .uri("https://api.novaposhta.ua/v2.0/json/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(json -> System.out.println("📥 Raw NP response length: " + json.length()))
                .flatMapMany(json -> {
                    try {
                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        var root = mapper.readTree(json);


                        var data = root.path("data");

                        System.out.println("✅ Parsed departments count: " + data.size());

                        return Flux.fromIterable(data)
                                .map(node -> new DepartmentDTO(
                                        node.path("Ref").asText(),
                                        node.path("Description").asText(),
                                        node.path("Number").asText()
                                ));

                    } catch (Exception e) {
                        e.printStackTrace();
                        return Flux.error(e);
                    }
                });
    }

}
