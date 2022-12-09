package ru.practicum.ewmservice.httpClient;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewmservice.models.httpClientRequestEntity.EndpointHit;
import ru.practicum.ewmservice.models.httpClientRequestEntity.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@Slf4j

public class HttpClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String statServiceUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    HttpClient(@Value("${stat-server.url}") String statServiceUrl) {
        this.statServiceUrl = statServiceUrl;
    }

    public void postStat(Long id, String uri, String ip) {

        String timestamp = LocalDateTime.now().format(FORMATTER);
        EndpointHit endpointHit = new EndpointHit();
        HttpEntity<EndpointHit> request = new HttpEntity<>(endpointHit);
        endpointHit.setIp(ip);
        endpointHit.setUri(uri);
        endpointHit.setApp("java-explore-with-me");
        endpointHit.setId(id);
        endpointHit.setTimesTamp(timestamp);
        try {
            ResponseEntity<EndpointHit> response = restTemplate.exchange(statServiceUrl + "/hit",
                    HttpMethod.POST, request, EndpointHit.class);
            String status = response.getStatusCode().toString();
            log.info("Отправлен HTTP-запрос с параметрами: url: {}, Method: {}, request: {}",
                    statServiceUrl + "/hit", HttpMethod.POST, request);
            log.info("Получен ответ: status: {}", status);
        } catch (Exception e) {
            log.info("{}", e.getMessage());
        }

    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, ViewStats> getStat(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        String url = statServiceUrl + "/stats";
        String startString = start.format(FORMATTER);
        String endString = end.format(FORMATTER);
        String urlTemplate;
        Map<String, Object> parameters;

        if (uris == null) {
            urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("unique", unique)
                    .encode()
                    .toUriString();
            parameters = new HashMap<>(Map.of(
                    "unique", unique));
        } else {
            urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("uris", String.join(",", uris))
                    .queryParam("unique", unique)
                    .encode()
                    .toUriString();
            parameters = new HashMap<>(Map.of(
                    "uris", Arrays.toString(uris),
                    "unique", unique));
        }

        String dateParam = "&start=" + startString + "&end=" + endString;
        urlTemplate = urlTemplate + dateParam;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<ViewStats> viewStatsHttpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ViewStats[]> response = restTemplate.exchange(
                    urlTemplate, HttpMethod.GET, viewStatsHttpEntity, ViewStats[].class, parameters);
            log.info("Отправлен http-запрос с параметрами: url: {}, method: {}", urlTemplate, HttpMethod.GET);
            log.info("Получен ответ: status: {}, body: {}", response.getStatusCode(), response.getBody());

            Map<String, ViewStats> result = new HashMap<>();
            List<ViewStats> viewStatsList = List.of(Objects.requireNonNull(response.getBody()));
            for (ViewStats viewStats : viewStatsList) {
                result.put(viewStats.getUri(), viewStats);
            }
            return result;
        } catch (Exception e) {
            log.info("{}", e.getMessage());
            return null;
        }
    }
}
