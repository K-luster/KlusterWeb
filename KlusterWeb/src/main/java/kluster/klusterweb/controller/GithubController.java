package kluster.klusterweb.controller;

import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.service.ArgoService;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
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

    @PostMapping("/create-repository")
    public ResponseDto createRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        try {
            return ResponseUtil.SUCCESS("Github 레포지토리가 생성되었습니다.",
                    githubService.createRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName(), repositoryName.getLocalPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/auto-ci")
    public ResponseDto buildDockerAndGithubAction(HttpServletRequest request, @RequestBody CommitPushDto commitPushDto) throws Exception {
        return ResponseUtil.SUCCESS("자동 CI 준비를 끝냈습니다",
                githubService.buildDockerAndGithubAction(request.getHeader("Authorization"), commitPushDto.getRepositoryName(), commitPushDto.getLocalRepositoryPath(), commitPushDto.getBranchName()));
    }

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

    @PostMapping("/get-all-repository")
    public List<GitHubRepository> getAllRepository(HttpServletRequest request) throws IOException {
        return githubService.getAllRepository(request.getHeader("Authorization"));
    }

    @PostMapping("/delete-repository")
    public void deleteRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        githubService.deleteRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName());
    }
}
