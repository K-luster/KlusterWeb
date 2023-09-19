package kluster.klusterweb.controller;

import kluster.klusterweb.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static kluster.klusterweb.util.RestApiUtil.RESOURCE_TYPE_POD;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;

    @GetMapping("/pod")
    public List getPods(){
        return apiService.getResource(RESOURCE_TYPE_POD);
    }


}