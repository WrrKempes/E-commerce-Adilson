package com.nicolaseduardo.e_commerce_adilson.repositories

import com.nicolaseduardo.e_commerce_adilson.models.author.Author
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AuthorRepository : JpaRepository<Author, Long> {
    fun findByEmailIgnoreCase(email: String): Optional<Author>
    fun existsByEmailIgnoreCase(email: String): Boolean
}