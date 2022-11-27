package ru.practicum.ewmservice.models.httpClientRequestEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class EndpointHit {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private String timesTamp;
}
