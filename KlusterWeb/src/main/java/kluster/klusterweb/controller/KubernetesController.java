package kluster.klusterweb.controller;

import kluster.klusterweb.service.KubernetesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static kluster.klusterweb.util.RestApiUtil.RESOURCE_TYPE_POD;

@RestController
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*")
@RequestMapping("/api")
public class KubernetesController {

    private final KubernetesService kubernetesService;
    private static String apiName;
    @GetMapping("/pod_list")
    public List<?> getPods(HttpServletRequest request){
        return kubernetesService.getPodResource(request.getHeader("Authorization"),RESOURCE_TYPE_POD, "pod_list");
    }

    @GetMapping("/pod_detail")
    public List<?> getPodsDetail(HttpServletRequest request){
        return kubernetesService.getPodResource(request.getHeader("Authorization"),RESOURCE_TYPE_POD, "pod_detail");
    }
}