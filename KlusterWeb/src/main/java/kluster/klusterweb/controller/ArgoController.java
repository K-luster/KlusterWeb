package kluster.klusterweb.controller;

import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.ArgoApiDto.ArgoApiRequestDto;
import kluster.klusterweb.service.ArgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
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
    public ResponseDto getAllApplications(HttpServletRequest request) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return ResponseUtil.SUCCESS("모든 애플리케이션을 가져왔습니다", argoService.getAllApplications(request.getHeader("Authorization")));
    }

    @PostMapping("/make-application")
    public ResponseDto makeApplication(HttpServletRequest request, @RequestBody ArgoApiRequestDto argoApiRequestDto) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return ResponseUtil.SUCCESS("애플리케이션을 생성했습니다.", argoService.makeApplications(request.getHeader("Authorization"), argoApiRequestDto));
    }
}
