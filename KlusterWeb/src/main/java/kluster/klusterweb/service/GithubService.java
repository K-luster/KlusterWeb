package kluster.klusterweb.service;

import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GithubService {

    private static final String GITHUB_REPO_URL = "https://api.github.com/user/repos";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    // jwt 토큰으로 github 토큰 찾기
    public String getGithubAccessToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        token = jwtTokenProvider.getToken(token);
        String email = jwtTokenProvider.getEmail(token);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            Member findMember = member.get();
            return findMember.getGithubAccessToken();
        } else {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
    }

    // jwt 토큰 반환
    public String getAccessToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        token = jwtTokenProvider.getToken(token);
        return token;
    }


    public ResponseEntity<String> createRepository(String accessToken, String repositoryName) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", repositoryName);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody);
        ResponseEntity<String> responseEntity = restTemplate.exchange(GITHUB_REPO_URL, HttpMethod.POST, requestEntity, String.class);

        return responseEntity;
    }
}
