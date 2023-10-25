package kluster.klusterweb.controller;

import io.swagger.annotations.ApiOperation;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.Github.CommitPushDto;
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

    @ApiOperation("Docker-compose가 있는 경우의 CI 과정을 진행합니다.")
    @PostMapping("/auto-ci")
    public ResponseDto<?> dockerComposeAutoCI(HttpServletRequest request, @RequestBody CommitPushDto commitPushDto) throws GitAPIException {
        return ResponseUtil.SUCCESS("Docker-compose 자동 CI가 진행됩니다.", dockerComposeService.dockerComposeCI(
                request.getHeader("Authorization"),
                commitPushDto.getRepositoryName(),
                commitPushDto.getLocalRepositoryPath(),
                commitPushDto.getBranchName()));
    }
}
