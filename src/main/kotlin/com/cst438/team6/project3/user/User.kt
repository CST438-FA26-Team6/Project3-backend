package com.cst438.team6.project3.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name= "users")
class User (
    @Column(nullable=false)
    var username: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole= UserRole.USER,

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?=null
){
}