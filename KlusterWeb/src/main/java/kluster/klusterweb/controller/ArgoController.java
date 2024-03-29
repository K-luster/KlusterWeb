package kluster.klusterweb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.ArgoApi.ArgoApiRequestDto;
import kluster.klusterweb.dto.RepositoryName;
import kluster.klusterweb.service.ArgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/argo")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ArgoController {

    private final ArgoService argoService;

    @GetMapping("/get-all-applications")
    public ResponseDto<Object> getAllApplications(@RequestHeader(value = "Authorization") String tokenInfo) {
        return ResponseUtil.SUCCESS("모든 애플리케이션을 가져왔습니다", argoService.getAllApplications(tokenInfo));
    }

    @PostMapping("/make-application")
    public ResponseDto<Object> makeApplication(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody ArgoApiRequestDto argoApiRequestDto) {
        return ResponseUtil.SUCCESS("애플리케이션을 생성했습니다.", argoService.makeApplications(tokenInfo, argoApiRequestDto));
    }

    @PostMapping("/get-pod-info")
    public ResponseDto<Object> getPodInfo(@RequestBody RepositoryName repositoryName) throws JsonProcessingException {
        System.out.println("repositoryName = " + repositoryName);
        return ResponseUtil.SUCCESS("[Deployment, StatefulSet, Pod, DaemonSet]에 해당하는 pod들입니다.", argoService.getDeploymentService(repositoryName.getRepositoryName()));
    }
}
