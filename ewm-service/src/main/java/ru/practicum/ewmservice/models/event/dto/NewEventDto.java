package ru.practicum.ewmservice.models.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.ewmservice.models.location.dto.LocationDto;
import ru.practicum.ewmservice.validation.AfterTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotNull
    @Size(min = 20, max = 2000, message = "Краткое описание должно быть длиной от 20 до 2000 символов")
    private String annotation;
    @NotNull
    private Long category;
    @NotNull
    @Size(min = 20, max = 7000, message = "Полное описание должно быть длиной от 20 до 7000 символов")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @AfterTime
    @NotNull
    private LocalDateTime eventDate;
    @NotNull
    private LocationDto location;
    private boolean paid;
    @PositiveOrZero
    private int participantLimit;
    private boolean requestModeration;
    @NotNull
    @Size(min = 3, max = 120, message = "Заголовок должен быть длиной от 3 до 120 символов")
    private String title;
}
