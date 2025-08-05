package hyper.run.domain.entity;

import hyper.run.domain.user.entity.Role;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "admin_user")
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_user_id",nullable = false)
    private Long id;

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "role",nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private Role role;

    @Setter
    @Column(name = "refresh_token",nullable = false)
    private String refreshToken;



}
