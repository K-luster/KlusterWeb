package kluster.klusterweb.controller;

import kluster.klusterweb.dto.RepositoryDto;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class GithubController {

    private final GithubService githubService;

    @Value("${github.access-token}")
    private String githubAccessToken;

    @GetMapping("/github/main-page")
    public String githubPageMain() {
        return "github/index";
    }

    @GetMapping("/github/create-repository")
    public String createRepository() {
        return "github/create";
    }

    @GetMapping("/github/create-repository/{repositoryId}")
    public String repository(@PathVariable Long repositoryId, ModelMap map) {
        return "github/detail";
    }

    @PostMapping("/github/create-repository")
    public ResponseEntity<String> createRepository(@ModelAttribute("repositoryRequest") RepositoryDto.RepositoryRequestDto request) {
        // String githubAccessToken = githubService.getGithubAccessToken(request);
        return githubService.createRepository(githubAccessToken, request.getRepositoryName()); // 나중에 사용자 이름으로 githubaccesstoken 접근
    }
}
