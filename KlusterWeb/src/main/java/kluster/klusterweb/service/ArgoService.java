package kluster.klusterweb.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.ArgoApiDto.*;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

    public RestTemplate makeRestTemplate() throws KeyStoreException, KeyManagementException, NoSuchAlgorithmException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
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


    public List<ArgoApplicationResponseDto> getAllApplications(String jwtToken) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        String namespace = member.getGithubName();
        RestTemplate restTemplate = this.makeRestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<ArgoNameSpaceApplicationResponseDto> response = restTemplate.exchange(apiUrl
                , HttpMethod.GET, requestEntity, ArgoNameSpaceApplicationResponseDto.class);
        List<Items> items = response.getBody().getItems();
        List<ArgoApplicationResponseDto> argoApplicationResponseDtos = items.stream()
                .filter(item -> item.getSpec().getDestination().getNamespace().equals(namespace))
                .map(item -> ArgoApplicationResponseDto.builder()
                        .name(item.getMetadata().getName())
                        .repoURL(item.getSpec().getSource().getRepoUrl())
                        .build())
                .collect(Collectors.toList());
        return argoApplicationResponseDtos;
    }

    public ResponseEntity<ArgoApiResponseDto> makeApplications(String jwtToken, ArgoApiRequestDto requestDto) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String email = jwtTokenProvider.extractSubjectFromJwt(jwtToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 없습니다."));
        RestTemplate restTemplate = this.makeRestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        requestDto.getSpecDto().getDestinationDto().setNamespace(member.getGithubName());
        HttpEntity<ArgoApiRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
        ResponseEntity<ArgoApiResponseDto> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                ArgoApiResponseDto.class
        );
        return responseEntity;
    }

}
