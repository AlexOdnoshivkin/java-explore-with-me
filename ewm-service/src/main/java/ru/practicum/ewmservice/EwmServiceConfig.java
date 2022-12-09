package ru.practicum.ewmservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewmservice.models.location.LocationFilter;

@Configuration
public class EwmServiceConfig {
    @Bean
    public LocationFilter getLocationFilter() {
        return new LocationFilter();
    }
}
