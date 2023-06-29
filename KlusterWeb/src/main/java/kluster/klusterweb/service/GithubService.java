package kluster.klusterweb.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GithubService {

    private static final String GITHUB_REPO_URL = "https://api.github.com/user/repos";


    public ResponseEntity<String> createRepository(String accessToken, String repositoryName) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", repositoryName);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(GITHUB_REPO_URL, HttpMethod.POST, requestEntity, String.class);

        return responseEntity;
    }
}
