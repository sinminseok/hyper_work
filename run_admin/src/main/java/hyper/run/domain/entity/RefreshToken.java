package hyper.run.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "admin_user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private AdminUser admin;

    @Column(name = "token",nullable = false)
    private String token;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public void updateRefreshToken(String refreshToken, long expiration) {
        this.token = refreshToken;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusSeconds(expiration);
    }
}
