package kluster.klusterweb.service;

import kluster.klusterweb.domain.Member;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GithubService {

    private static final String GITHUB_REPO_URL = "https://api.github.com/user/repos";
    //private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public static Git init(File dir) throws Exception {
        return Git.init().setDirectory(dir).call();
    }

    // jwt 토큰으로 github 토큰 찾기
//    public String getGithubAccessToken(HttpServletRequest request) {
//        String token = jwtTokenProvider.resolveToken(request);
//        token = jwtTokenProvider.getToken(token);
//        String email = jwtTokenProvider.getEmail(token);
//        Optional<Member> member = memberRepository.findByEmail(email);
//        if (member.isPresent()) {
//            Member findMember = member.get();
//            return findMember.getGithubAccessToken();
//        } else {
//            throw new RuntimeException("존재하지 않는 이메일입니다.");
//        }
//    }

    // jwt 토큰 반환
//    public String getAccessToken(HttpServletRequest request) {
//        String token = jwtTokenProvider.resolveToken(request);
//        token = jwtTokenProvider.getToken(token);
//        return token;
//    }


    public ResponseEntity<String> createRepository(String githubToken, String repositoryName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\"name\":\"" + repositoryName + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(GITHUB_REPO_URL, HttpMethod.POST, requestEntity, String.class);
        return responseEntity;
    }

    public void commitAndPush(String githubToken, String repositoryName) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

    }

    public void deleteRepository(String githubToken, String repositoryName){
        String apiUrl = GITHUB_REPO_URL + "jake-huen/" + repositoryName;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.DELETE, URI.create(apiUrl));
        ResponseEntity<Void> responseEntity = restTemplate.exchange(requestEntity, Void.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("리포지토리가 성공적으로 삭제되었습니다.");
        } else {
            System.err.println("리포지토리 삭제 실패: " + responseEntity.getStatusCode());
        }
    }


}
