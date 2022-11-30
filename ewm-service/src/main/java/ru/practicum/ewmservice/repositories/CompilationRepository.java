package ru.practicum.ewmservice.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmservice.models.compilation.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("select c from Compilation as c where c.pinned=?1 order by c.id")
    List<Compilation> searchCompilation(Boolean pinned, Pageable pageable);
}
