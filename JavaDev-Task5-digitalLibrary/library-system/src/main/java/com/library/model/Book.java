package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @Column(unique = true)
    @NotBlank
    private String isbn;

    private String category;

    @Min(0)
    private int quantity;          // total copies owned by the library

    @Min(0)
    private int availableQuantity; // copies currently on the shelf

    public boolean isAvailable() {
        return availableQuantity > 0;
    }
}
