package kluster.klusterweb.service;

import kluster.klusterweb.dto.PodDto;
import kluster.klusterweb.util.RestApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApiService {

    private final RestApiUtil restApiUtil;
    public List getResource(String resourceType) {


        try {
            ResponseEntity e = restApiUtil.execute(HttpMethod.GET, resourceType);

            String statusCode = String.valueOf(e.getStatusCode());
            Map response = (Map) e.getBody();

            List<Map> items = (List) response.get("items");

            log.debug("k8s items = {}", items);
            return items.stream()
                    .map(item -> {

                        Map metadata = (Map) item.get("metadata");
                        String name = (String) metadata.get("name");
                        String namespace = (String) metadata.get("namespace");
                        Map status = (Map) item.get("status");
                        String phase = (String) status.get("phase");

                        return PodDto.builder()
                                .name(name)
                                .status(phase)
                                .namespace(namespace)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("k8s items gathering fail. err={}", e.toString(), e);
            return null;
        }
    }
}