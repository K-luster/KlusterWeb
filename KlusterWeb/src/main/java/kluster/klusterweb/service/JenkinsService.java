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

/**
 *
 * Job 생성 [POST]
 * http://[jenkins url]/createItem?name=[job name]
 *
 * Job 조회 [GET]
 * http://[jenkins url]/job/[job name]/api/json or xml
 *
 * Job 빌드 수행 [POST]
 * http://[jenkins url]/job/[job name]/build
 *
 * Job 빌드 결과 조회 [GET]
 * http://[jenkins url]/job/[job name]/[build number]/api/json or xml
 *
 * Job 빌드 결과 조회 - 마지막 성공 빌드 [GET]
 * http://[jenkins url]/job/[job name]/lastStableBuild/api/json or xml
*/
