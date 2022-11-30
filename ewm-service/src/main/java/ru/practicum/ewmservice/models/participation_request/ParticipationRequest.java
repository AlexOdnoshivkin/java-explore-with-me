package ru.practicum.ewmservice.models.participation_request;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    @Column(name = "event", nullable = false)
    private Long event;
    @Column(name = "requester", nullable = false)
    private Long requester;
    @Column(name = "status", nullable = false)
    private Status status;

}
