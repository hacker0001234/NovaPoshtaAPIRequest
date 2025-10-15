package com.example.backend.Controllers;

import com.example.backend.DTOs.CityDTO;
import com.example.backend.DTOs.DepartmentDTO;
import com.example.backend.Services.MainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class MainController {

    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping("/get/cities")
    public Flux<CityDTO> getTest(){
        return mainService.getCities();
    }

    @GetMapping("/get/department/{ref}")
    public Mono<List<DepartmentDTO>> getDepartments(@PathVariable("ref") String cityRef) {
        return mainService.getDepartmentsByCity(cityRef,"1")
                .collectList()
                .doOnNext(list -> System.out.println("🚀 Sending " + list.size() + " departments to frontend"));
    }
}
