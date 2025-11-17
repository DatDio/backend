package com.mailshop_dragonvu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ROLES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "base_seq_gen", sequenceName = "ROLE_SEQ", allocationSize = 1)
public class Role extends BaseEntity {

    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

}
