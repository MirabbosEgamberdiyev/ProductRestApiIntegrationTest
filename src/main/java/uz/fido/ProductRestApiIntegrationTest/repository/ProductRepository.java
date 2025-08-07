package uz.fido.ProductRestApiIntegrationTest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.fido.ProductRestApiIntegrationTest.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Additional query methods can be added here if needed
    List<Product> findByNameContainingIgnoreCase(String name);
    Optional<Product> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}