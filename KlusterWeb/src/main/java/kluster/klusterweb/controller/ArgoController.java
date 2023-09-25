package kluster.klusterweb.controller;

import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.service.ArgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/argo")
@CrossOrigin(allowedHeaders = "*")
@RequiredArgsConstructor
public class ArgoController {

    private final ArgoService argoService;

    @GetMapping("/get-all-applications")
    public ResponseDto getAllApplications(){
        return ResponseUtil.SUCCESS("모든 애플리케이션을 가져왔습니다", argoService.getAllApplications());
    }
}
