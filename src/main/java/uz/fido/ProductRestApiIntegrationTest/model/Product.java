package uz.fido.ProductRestApiIntegrationTest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull(message = "Price must not be null")
    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private Double price;

    public Product(String name, Double price) {
        this.name = name;
        this.price = price;
    }
}