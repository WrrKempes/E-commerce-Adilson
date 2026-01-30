package com.nicolaseduardo.e_commerce_adilson.models.book

import com.nicolaseduardo.e_commerce_adilson.models.author.Author
import jakarta.persistence.*

@Entity
@Table(name = "books")
data class Book(

    /** PK é VARCHAR(255). Usa slug: 'extase','sempre','versos' etc. */
    @Id
    @Column(length = 255, nullable = false)
    val id: String,

    /** Coluna textual legada (no CSV / baseline). Continua existindo, mas não é fonte de verdade futura. */
    @Column(nullable = false)
    val author: String,

    @Column(nullable = false)
    val category: String,

    @Column(columnDefinition = "TEXT", nullable = true)
    val description: String? = null,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    val price: Double,

    @Column(nullable = false)
    var stock: Int = 0,

    @Column(nullable = false)
    val title: String,

    /**
     * Novo ponteiro para o autor real (FK authors.id).
     * O banco já tem a coluna author_id e a FK fk_books_author, então não gera migração nova.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "author_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "fk_books_author")
    )
    val authorRef: Author? = null
)