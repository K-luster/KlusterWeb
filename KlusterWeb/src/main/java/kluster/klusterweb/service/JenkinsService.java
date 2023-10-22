package kluster.klusterweb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JenkinsService {

    @Value("${jenkins.secret-key}")
    private String jenkinsKey;

    @Value("${jenkins.url}")
    private String jenkinsUrl;

    public ResponseEntity<String> getAllJenkins(){
        String apiUrl = jenkinsUrl + "/job/k8s_deploy/api/json";

        String username = "jenkins";
        String password = jenkinsKey;

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.setBasicAuth(username, password);
            return execution.execute(request, body);
        });

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);
        return response;
    }

    public ResponseEntity<String> buildJenkins(){
        String apiUrl = jenkinsUrl + "/job/k8s_deploy/build";

        String username = "jenkins";
        String password = jenkinsKey;

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.setBasicAuth(username, password);
            return execution.execute(request, body);
        });

        // jenkins서버에 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, null, String.class);
        return response;
    }
}
