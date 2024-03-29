package kluster.klusterweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.ArgoApi.*;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class ArgoService {

    @Value("${argocd.url}")
    private String argoApiUrl;

    @Value("${argocd.cookie}")
    private String cookieValue;

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RestTemplate makeRestTemplate() {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(3 * 1000);
        requestFactory.setReadTimeout(50 * 1000);
        return new RestTemplate(requestFactory);
    }


    public List<ArgoApplicationResponseDto> getAllApplications(String jwtToken) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(()
                -> new RuntimeException("해당하는 이메일이 없습니다."));
        String namespace = member.getGithubName().toLowerCase();
        RestTemplate restTemplate = this.makeRestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<ArgoNameSpaceApplicationResponseDto> response = restTemplate.exchange(apiUrl
                , HttpMethod.GET, requestEntity, ArgoNameSpaceApplicationResponseDto.class);
        List<Items> items = Objects.requireNonNull(response.getBody()).getItems();
        return items.stream()
                .filter(item -> item.getSpec().getDestination().getNamespace().equals(namespace))
                .map(item -> ArgoApplicationResponseDto.builder()
                        .name(item.getMetadata().getName())
                        .repoURL(item.getSpec().getSource().getRepoUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public ResponseEntity<ArgoApiResponseDto> makeApplications(String jwtToken, ArgoApiRequestDto requestDto) {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        RestTemplate restTemplate = this.makeRestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        requestDto.getSpecDto().getDestinationDto().setNamespace(member.getGithubName().toLowerCase());
        requestDto.getSpecDto().getSyncPolicyDto().setSyncOptions(new ArrayList<>());
        HttpEntity<ArgoApiRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
        return restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                ArgoApiResponseDto.class
        );
    }

    public ArrayList<String> getDeploymentService(String repositoryName) throws JsonProcessingException {
        String url = String.format("https://kluster.iptime.org:9001/api/v1/applications/%s", repositoryName);
        RestTemplate restTemplate = this.makeRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        ArrayList<String> results = new ArrayList<>();
        JsonNode resources = jsonNode.get("status").get("resources");
        String[] validKinds = {"Deployment", "StatefulSet", "Pod", "DaemonSet"};

        for (JsonNode resource : resources) {
            String kind = resource.get("kind").asText();
            if (isKindValid(kind, validKinds)) {
                String resourceName = resource.get("name").asText();
                results.add(resourceName);
            }
        }
        return results;
    }
    private boolean isKindValid(String kind, String[] validKinds) {
        for (String validKind : validKinds) {
            if (kind.equals(validKind)) {
                return true;
            }
        }
        return false;
    }
}
