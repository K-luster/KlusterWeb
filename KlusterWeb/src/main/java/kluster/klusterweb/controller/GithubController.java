package kluster.klusterweb.controller;

import io.swagger.annotations.ApiOperation;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.dto.Github.CommitPushDto;
import kluster.klusterweb.dto.Github.GitHubRepository;
import kluster.klusterweb.dto.Github.RepositoryDto;
import kluster.klusterweb.service.ArgoService;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*")
@RequestMapping("/github")
public class GithubController {

    private final GithubService githubService;
    private final ArgoService argoService;

    @ApiOperation("레포지토리 생성합니다")
    @PostMapping("/create-repository")
    public ResponseDto createRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        try {
            return ResponseUtil.SUCCESS("Github 레포지토리가 생성되었습니다.",
                    githubService.createRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName(), repositoryName.getLocalPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation("모든 레포지토리를 가져옵니다.")
    @PostMapping("/get-all-repository")
    public List<GitHubRepository> getAllRepository(HttpServletRequest request) throws IOException {
        return githubService.getAllRepository(request.getHeader("Authorization"));
    }

    @ApiOperation("레포지토리를 삭제합니다.")
    @PostMapping("/delete-repository")
    public void deleteRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        githubService.deleteRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName());
    }

    @ApiOperation("자동으로 CI 과정을 진행합니다.")
    @PostMapping("/auto-ci")
    public ResponseDto autoCI(HttpServletRequest request, @RequestBody CommitPushDto commitPushDto) throws Exception {
        return ResponseUtil.SUCCESS("자동 CI 준비를 끝냈습니다",
                githubService.continuousIntegration(request.getHeader("Authorization"), commitPushDto.getRepositoryName(), commitPushDto.getLocalRepositoryPath(), commitPushDto.getBranchName()));
    }

    @ApiOperation("자동으로 CD 과정을 진행합니다.")
    @PostMapping("/auto-cd")
    public ResponseDto autoCD(HttpServletRequest request, @RequestBody DeployRequestDto deployRequestDto) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        githubService.deploy(
                request.getHeader("Authorization"),
                deployRequestDto.getLocalRepositoryPath(),
                deployRequestDto.getRepositoryName(),
                deployRequestDto.getServiceName(),
                deployRequestDto.getReplicaCount());
        return ResponseUtil.SUCCESS("애플리케이션이 생성되었습니다.", argoService.makeApplications(request.getHeader("Authorization"), deployRequestDto.getArgoApiRequestDto()));
    }
}
