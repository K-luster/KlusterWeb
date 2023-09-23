package kluster.klusterweb.controller;

import kluster.klusterweb.dto.CommitPushDto;
import kluster.klusterweb.dto.GitHubRepository;
import kluster.klusterweb.dto.GithubRepositoryDto;
import kluster.klusterweb.dto.RepositoryDto;
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
@RequestMapping("/github")
public class GithubController {

    private final GithubService githubService;

    @Value("${github.access-token}")
    private String githubAccessToken;

    @PostMapping("/create-repository")
    public ResponseEntity<String> createRepository(HttpServletRequest request, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        return githubService.createRepository(request.getHeader("Authorization"), repositoryName.getRepositoryName());
    }

    @PostMapping("/commit-and-push")
    public void commitAndPush(HttpServletRequest request, @RequestBody CommitPushDto commitPushDto) throws Exception {
        githubService.commitAndPush(request.getHeader("Authorization"), commitPushDto.getRepositoryName(), commitPushDto.getLocalRepositoryPath(), commitPushDto.getBranchName());
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
