package kluster.klusterweb.controller;

import io.swagger.annotations.ApiOperation;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.DeployRequestDto;
import kluster.klusterweb.dto.Github.CommitPushDto;
import kluster.klusterweb.service.ArgoService;
import kluster.klusterweb.service.DockerComposeService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*")
@RequestMapping("/docker-compose")
public class DockerComposeController {

    private final DockerComposeService dockerComposeService;
    private final ArgoService argoService;

    @ApiOperation("Docker-compose가 있는 경우의 CI 과정을 진행합니다.")
    @PostMapping("/auto-ci")
    public ResponseDto<Object> dockerComposeAutoCI(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody CommitPushDto commitPushDto) throws GitAPIException {
        return ResponseUtil.SUCCESS("Docker-compose 자동 CI가 진행됩니다.", dockerComposeService.dockerComposeCI(
                tokenInfo,
                commitPushDto.getRepositoryName(),
                commitPushDto.getLocalRepositoryPath(),
                commitPushDto.getBranchName()));
    }

    @ApiOperation("Docker-compose가 있는 경우의 CD 과정 API")
    @PostMapping("/auto-cd")
    public ResponseDto<Object> dockerComposeAutoCD(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody DeployRequestDto deployRequestDto){
        return ResponseUtil.SUCCESS("배포된 애플리케이션이 생성되었습니다.", argoService.makeApplications(tokenInfo, deployRequestDto.getArgoApiRequestDto()));
    }
}
