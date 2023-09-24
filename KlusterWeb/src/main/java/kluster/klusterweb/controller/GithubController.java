package kluster.klusterweb.controller;

import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*")
@RequestMapping("/github")
public class GithubController {

    private final GithubService githubService;

    @PostMapping("/create-repository")
    public ResponseDto createRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        return ResponseUtil.SUCCESS("Github 레포지토리가 생성되었습니다.",
                githubService.createRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName()));
    }

    @PostMapping("/auto-ci")
    public ResponseDto buildDockerAndGithubAction(HttpServletRequest request, @RequestBody CommitPushDto commitPushDto) throws Exception {
        return ResponseUtil.SUCCESS("자동 CI 준비를 끝냈습니다",
                githubService.buildDockerAndGithubAction(request.getHeader("Authorization"), commitPushDto.getRepositoryName(), commitPushDto.getLocalRepositoryPath(), commitPushDto.getBranchName()));
    }

    @PostMapping("/auto-cd")
    public ResponseDto autoCD(HttpServletRequest request, @RequestBody DeployRequestDto deployRequestDto) {
        return ResponseUtil.SUCCESS("자동 CD 준비를 끝냈습니다.",
                githubService.deploy(
                        request.getHeader("Authorization"),
                        deployRequestDto.getLocalRepositoryPath(),
                        deployRequestDto.getRepositoryName(),
                        deployRequestDto.getServiceName(),
                        deployRequestDto.getReplicaCount()));
    }

    @PostMapping("/get-all-repository")
    public List<GitHubRepository> getAllRepository(HttpServletRequest request, @RequestBody GithubRepositoryDto githubRepositoryDto) throws IOException {
        return githubService.getAllRepository(request.getHeader("Authorization"), githubRepositoryDto.getUsername());
    }

    @PostMapping("/delete-repository")
    public void deleteRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        githubService.deleteRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName());
    }
}
