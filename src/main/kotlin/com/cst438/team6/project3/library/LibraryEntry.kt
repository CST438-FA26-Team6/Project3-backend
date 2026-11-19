package com.cst438.team6.project3.library

import com.cst438.team6.project3.title.Title
import com.cst438.team6.project3.user.User
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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(
    name = "library_entries",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_library_entry_user_title",
            columnNames = ["user_id", "title_id"]
        )
    ]
)
class LibraryEntry(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var title: Title,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LibraryStatus,

    @Column(nullable = false)
    var progress: Int = 0,

    var rating: Int? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
)
