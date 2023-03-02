package ru.practicum.ewmservice.models.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.core.lang.Nullable;
import lombok.Data;
import ru.practicum.ewmservice.validation.AfterTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {
    @Size(min = 20, max = 2000, message = "Краткое описание должно быть длиной от 20 до 2000 символов")
    private @Nullable String annotation;
    private Long category;
    @Size(min = 20, max = 7000, message = "Полное описание должно быть длиной от 20 до 7000 символов")
    private @Nullable String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @AfterTime
    private @Nullable LocalDateTime eventDate;
    @NotNull
    private Long eventId;
    private boolean paid;
    private int participantLimit;
    @Size(min = 3, max = 120, message = "Заголовок должен быть длиной от 3 до 120 символов")
    private @Nullable String title;
}
