package kluster.klusterweb.controller;

import kluster.klusterweb.dto.RepositoryDto;
import kluster.klusterweb.service.GithubService;
import kluster.klusterweb.service.JgitUtil;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;


@Controller
@RequiredArgsConstructor
@RequestMapping("/github")
public class GithubController {

    private final GithubService githubService;

    @Value("${github.access-token}")
    private String githubAccessToken;

    @GetMapping("/main-page")
    public String githubPageMain() {
        return "github/index";
    }

    @GetMapping("/create-repository")
    public String createRepository() {
        return "github/create";
    }

    @GetMapping("/create-repository/{repositoryId}")
    public String repository(@PathVariable Long repositoryId, ModelMap map) {
        return "github/detail";
    }

    @GetMapping("/commit-and-push")
    public String commitAndPush(){return "github/commitAndPush";}

    @PostMapping("/create-repository")
    public ResponseEntity<String> createRepository(@ModelAttribute("repositoryRequest") RepositoryDto.RepositoryRequestDto request) {
        // String githubAccessToken = githubService.getGithubAccessToken(request);
        return githubService.createRepository(githubAccessToken, request.getRepositoryName()); // 나중에 사용자 이름으로 githubaccesstoken 접근
    }

    @PostMapping("/commit-and-push")
    public void commitAndPush(String repositoryName) throws Exception {
        // githubService.commitAndPush(githubAccessToken, repositoryName);
        File dir = new File("/Users/kimtaeheon/Desktop/test");
        JgitUtil.checkOut(dir);
        Git git = JgitUtil.open(dir);
        JgitUtil.remoteAdd(git);
        JgitUtil.pull(git);
        JgitUtil.add(git, "404.md");
        JgitUtil.rm(git, "ReadMe.md");
        JgitUtil.commit(git, "8888");
        JgitUtil.push(git);
    }

    @PostMapping("/delete-repository")
    public void deleteRepository(@ModelAttribute("repositoryRequest") RepositoryDto.RepositoryRequestDto request) {
        githubService.deleteRepository(request.getGithubAccessToken(),request.getRepositoryName());
    }
}
