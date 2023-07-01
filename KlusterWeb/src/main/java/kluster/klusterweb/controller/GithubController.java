package kluster.klusterweb.controller;

import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class GithubController {

    private final GithubService githubService;

    @PostMapping("/github/create-repository")
    public ResponseEntity<String> createRepository(HttpServletRequest request, @RequestBody String repositoryName) {
        String githubAccessToken = githubService.getGithubAccessToken(request);
        return githubService.createRepository(githubAccessToken, repositoryName);
    }
}
