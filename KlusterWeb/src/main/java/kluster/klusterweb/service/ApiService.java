package kluster.klusterweb.service;

import kluster.klusterweb.dto.ContainerDto;
import kluster.klusterweb.dto.PodDetailDto;
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
    public List getPodResource(String resourceType, String apiName) {

        try {
            ResponseEntity e = restApiUtil.execute(HttpMethod.GET, resourceType, apiName);
            if (apiName.equals("pod_list"))
            {
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
            }
            else{
                String statusCode = String.valueOf(e.getStatusCode());
                Map response = (Map) e.getBody();

                List<Map> items = (List) response.get("items");

                log.debug("k8s items = {}", items);
                return items.stream()
                        .map(item -> {
                            //pod정보 불러오기
                            Map metadata = (Map) item.get("metadata");
                            String name = (String) metadata.get("name");
                            String namespace = (String) metadata.get("namespace");

                            //각 파드별
                            List<Map> containers = (List<Map>) item.get("containers");
                            List<ContainerDto> containerDtos = containers.stream()
                                    .map(container -> {
                                        String containerName = (String) container.get("name");
                                        Map usage = (Map) container.get("usage");
                                        String cpu = (String) usage.get("cpu");
                                        String memory = (String) usage.get("memory");
                                        return new ContainerDto(containerName, cpu, memory);
                                    })
                                    .collect(Collectors.toList());

                            return new PodDetailDto(name, namespace, containerDtos);
                        })
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("k8s items gathering fail. err={}", e.toString(), e);
            return null;
        }
    }
}