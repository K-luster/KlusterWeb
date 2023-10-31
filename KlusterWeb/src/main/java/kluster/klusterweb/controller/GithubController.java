package kluster.klusterweb.controller;

import io.swagger.annotations.ApiOperation;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.domain.Project;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.dto.Github.ActionCompletedDto;
import kluster.klusterweb.dto.Github.CommitPushDto;
import kluster.klusterweb.dto.Github.GitHubRepository;
import kluster.klusterweb.dto.Github.RepositoryDto;
import kluster.klusterweb.repository.ProjectRepository;
import kluster.klusterweb.service.ArgoService;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
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
    private final ArgoService argoService;

    @ApiOperation("레포지토리 생성합니다")
    @PostMapping("/create-repository")
    public ResponseDto<Object> createRepository(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) throws GitAPIException, IOException {
        return ResponseUtil.SUCCESS("Github 레포지토리가 생성되었습니다.",
                githubService.createGitHubRepository(tokenInfo, repositoryName.getRepositoryName(), repositoryName.getLocalPath()));
    }

    @ApiOperation("모든 레포지토리를 가져옵니다.")
    @PostMapping("/get-all-repository")
    public List<GitHubRepository> getAllRepository(@RequestHeader(value = "Authorization") String tokenInfo) throws IOException {
        return githubService.getAllRepository(tokenInfo);
    }

    @ApiOperation("레포지토리를 삭제합니다.")
    @PostMapping("/delete-repository")
    public void deleteRepository(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody RepositoryDto.RepositoryRequestDto repositoryName) {
        githubService.deleteRepository(tokenInfo, repositoryName.getRepositoryName());
    }

    @ApiOperation("자동으로 CI 과정을 진행합니다.")
    @PostMapping("/auto-ci")
    public ResponseDto<Object> autoCI(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody CommitPushDto commitPushDto) throws Exception {
        return ResponseUtil.SUCCESS("자동 CI를 진행합니다",
                githubService.autoCI(tokenInfo, commitPushDto.getRepositoryName(), commitPushDto.getLocalRepositoryPath(), commitPushDto.getBranchName()));
    }

    // github action에서 보내는 POST API
    @PostMapping("/action-completed")
    public ResponseDto<Object> receiveNotification(@RequestBody ActionCompletedDto actionCompletedDto) {
        return ResponseUtil.SUCCESS("Action 완료", githubService.actionCompleted(actionCompletedDto.getGithubUsername()));
    }

    @ApiOperation("자동으로 CD 과정을 진행합니다.")
    @PostMapping("/auto-cd")
    public ResponseDto<Object> autoCD(@RequestHeader(value = "Authorization") String tokenInfo, @RequestBody DeployRequestDto deployRequestDto) {
        Boolean fileAdd = githubService.autoCD(
                tokenInfo,
                deployRequestDto.getLocalRepositoryPath(),
                deployRequestDto.getServiceName(),
                deployRequestDto.getReplicaCount());
        if (fileAdd) {
            return ResponseUtil.SUCCESS("배포된 애플리케이션이 생성되었습니다.", argoService.makeApplications(tokenInfo, deployRequestDto.getArgoApiRequestDto()));
        } else {
            return ResponseUtil.FAILURE("아직 CI과정이 완료되지 않았습니다.", null);
        }
    }
}
