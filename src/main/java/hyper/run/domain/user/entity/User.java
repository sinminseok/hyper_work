package hyper.run.domain.user.entity;

import hyper.run.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "user")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id", updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "brith", nullable = true)
    private String brith;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    @Setter
    @Column(name = "refreshToken", nullable = true)
    private String refreshToken;
}
