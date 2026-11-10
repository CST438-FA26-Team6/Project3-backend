package com.cst438.team6.project3.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name= "identities",
    uniqueConstraints = [UniqueConstraint(
        name="uk_identity_provider_user",
        columnNames = ["provider","provider_user_id"]
    )]
)
class Identity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider,

    @Column(name = "provider_user_id", nullable = false)
    var providerUserId: String,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?= null
) {
}