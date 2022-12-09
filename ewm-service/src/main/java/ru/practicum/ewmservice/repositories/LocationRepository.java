package ru.practicum.ewmservice.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmservice.models.location.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query(value = "SELECT * " +
            "FROM locations as l " +
            "where distance(l.lat, l.lon, ?1, ?2) <= l.radius",
            nativeQuery = true)
    Optional<Location> findLocationByCoordinate(double lat, double lon);

    @Query("select l from Location as l where l.name is not null")
    List<Location> findLocationsWhereNameNotNull(Pageable pageable);
}
