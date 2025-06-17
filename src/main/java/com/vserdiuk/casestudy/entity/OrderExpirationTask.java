package com.vserdiuk.casestudy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_expiration_tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderExpirationTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 8896860977190545976L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status; // PENDING, SENT, FAILED
}