package uz.fido.ProductRestApiIntegrationTest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.fido.ProductRestApiIntegrationTest.exception.ProductNotFoundException;
import uz.fido.ProductRestApiIntegrationTest.model.Product;
import uz.fido.ProductRestApiIntegrationTest.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.getId() != null) {
            product.setId(null); // Ensure new entity
        }
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        if (id == null || id <= 0) {
            throw new ProductNotFoundException("Invalid product ID: " + id);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product partialUpdateProduct(Long id, Map<String, Object> updates) {
        if (id == null || id <= 0) {
            throw new ProductNotFoundException("Invalid product ID: " + id);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    if (value instanceof String) {
                        product.setName((String) value);
                    }
                    break;
                case "price":
                    if (value instanceof Number) {
                        product.setPrice(((Number) value).doubleValue());
                    }
                    break;
            }
        });

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new ProductNotFoundException("Invalid product ID: " + id);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
    }

    @Transactional
    public List<Product> createProducts(List<Product> products) {
        // Remove any existing IDs to ensure new entities
        products.forEach(product -> product.setId(null));
        return productRepository.saveAll(products);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return id != null && id > 0 && productRepository.existsById(id);
    }
}