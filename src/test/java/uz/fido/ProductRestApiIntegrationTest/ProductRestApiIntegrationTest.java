package uz.fido.ProductRestApiIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uz.fido.ProductRestApiIntegrationTest.model.Product;
import uz.fido.ProductRestApiIntegrationTest.repository.ProductRepository;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product REST API Integration Tests")
class ProductRestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    @DisplayName("Clean database before each test")
    void setUp() {
        productRepository.deleteAll();
        productRepository.flush();
    }

    private MvcResult createProductAndGetResult(Product product) throws Exception {
        return mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private Product createProductAndExtract(Product product) throws Exception {
        MvcResult result = createProductAndGetResult(product);
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, Product.class);
    }

    @Test
    @Order(1)
    @DisplayName("1. Create product test")
    void testCreateProduct() throws Exception {
        Product product = new Product("Laptop", 999.99);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    @Order(2)
    @DisplayName("2. Get product by ID test")
    void testGetProductById() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Phone", 499.99));

        mockMvc.perform(get("/api/products/" + createdProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.price").value(499.99));
    }

    @Test
    @Order(3)
    @DisplayName("3. Get all products test")
    void testGetAllProducts() throws Exception {
        createProductAndExtract(new Product("Mouse", 29.99));
        createProductAndExtract(new Product("Keyboard", 59.99));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Mouse", "Keyboard")))
                .andExpect(jsonPath("$[*].price", containsInAnyOrder(29.99, 59.99)));
    }

    @Test
    @Order(4)
    @DisplayName("4. Update product test")
    void testUpdateProduct() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Tablet", 299.99));
        Product updatedProduct = new Product("Updated Tablet", 349.99);

        mockMvc.perform(put("/api/products/" + createdProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(jsonPath("$.name").value("Updated Tablet"))
                .andExpect(jsonPath("$.price").value(349.99));
    }

    @Test
    @Order(5)
    @DisplayName("5. Delete product test")
    void testDeleteProduct() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Monitor", 199.99));

        mockMvc.perform(delete("/api/products/" + createdProduct.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + createdProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    @DisplayName("6. Create product with invalid data test")
    void testCreateProductWithInvalidData() throws Exception {
        // Test with empty name
        Product invalidProduct1 = new Product("", 999.99);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct1)))
                .andExpect(status().isBadRequest());

        // Test with negative price
        Product invalidProduct2 = new Product("Invalid Product", -10.00);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct2)))
                .andExpect(status().isBadRequest());

        // Test with null price
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Valid Name\",\"price\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("7. Get non-existent product test")
    void testGetNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("8. Update non-existent product test")
    void testUpdateNonExistentProduct() throws Exception {
        Product updatedProduct = new Product("Non-existent", 349.99);

        mockMvc.perform(put("/api/products/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("9. Update product with invalid data test")
    void testUpdateProductWithInvalidData() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Headphones", 89.99));

        // Test with empty name
        Product invalidUpdate1 = new Product("", 99.99);
        mockMvc.perform(put("/api/products/" + createdProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate1)))
                .andExpect(status().isBadRequest());

        // Test with negative price
        Product invalidUpdate2 = new Product("Valid Name", -20.00);
        mockMvc.perform(put("/api/products/" + createdProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("10. Delete non-existent product test")
    void testDeleteNonExistentProduct() throws Exception {
        mockMvc.perform(delete("/api/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("11. Get empty product list test")
    void testGetEmptyProductList() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(12)
    @DisplayName("12. Create products with duplicate names test")
    void testCreateDuplicateProductName() throws Exception {
        createProductAndExtract(new Product("Speaker", 79.99));

        // Should allow duplicate names with different prices
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Product("Speaker", 89.99))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Speaker"))
                .andExpect(jsonPath("$.price").value(89.99));
    }

    @Test
    @Order(13)
    @DisplayName("13. Partial update product price test")
    void testPartialUpdateProductPrice() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Camera", 499.99));

        String patchJson = "{\"price\": 599.99}";

        mockMvc.perform(patch("/api/products/" + createdProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(jsonPath("$.name").value("Camera"))
                .andExpect(jsonPath("$.price").value(599.99));
    }

    @Test
    @Order(14)
    @DisplayName("14. Partial update product name test")
    void testPartialUpdateProductName() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Old Camera", 499.99));

        String patchJson = "{\"name\": \"New Camera\"}";

        mockMvc.perform(patch("/api/products/" + createdProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProduct.getId()))
                .andExpect(jsonPath("$.name").value("New Camera"))
                .andExpect(jsonPath("$.price").value(499.99));
    }

    @Test
    @Order(15)
    @DisplayName("15. Bulk create multiple products test")
    void testCreateMultipleProducts() throws Exception {
        List<Product> products = Arrays.asList(
                new Product("Charger", 19.99),
                new Product("Headset", 49.99),
                new Product("Webcam", 69.99)
        );

        mockMvc.perform(post("/api/products/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(products)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Charger", "Headset", "Webcam")))
                .andExpect(jsonPath("$[*].price", containsInAnyOrder(19.99, 49.99, 69.99)));
    }

    @Test
    @Order(16)
    @DisplayName("16. Bulk create with empty list test")
    void testCreateMultipleProductsWithEmptyList() throws Exception {
        mockMvc.perform(post("/api/products/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    @DisplayName("17. Bulk create with invalid product test")
    void testCreateMultipleProductsWithInvalidData() throws Exception {
        List<Product> products = Arrays.asList(
                new Product("Valid Product", 19.99),
                new Product("", 49.99), // Invalid: empty name
                new Product("Another Valid", 69.99)
        );

        mockMvc.perform(post("/api/products/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(products)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(18)
    @DisplayName("18. Invalid HTTP method test")
    void testInvalidHttpMethod() throws Exception {
        // POST to a GET endpoint (should return method not allowed)
        mockMvc.perform(post("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Order(19)
    @DisplayName("19. Create product with very long name test")
    void testCreateProductWithLongName() throws Exception {
        String longName = "A".repeat(256); // Exceeds 255 character limit
        Product product = new Product(longName, 99.99);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    @DisplayName("20. Partial update non-existent product test")
    void testPartialUpdateNonExistentProduct() throws Exception {
        String patchJson = "{\"price\": 599.99}";

        mockMvc.perform(patch("/api/products/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(21)
    @DisplayName("21. Product exists endpoint test")
    void testProductExists() throws Exception {
        Product createdProduct = createProductAndExtract(new Product("Test Product", 123.45));

        // Test existing product
        mockMvc.perform(get("/api/products/exists/" + createdProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Test non-existing product
        mockMvc.perform(get("/api/products/exists/9999"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @Order(22)
    @DisplayName("22. Invalid JSON format test")
    void testInvalidJsonFormat() throws Exception {
        String invalidJson = "{\"name\":\"Test\",\"price\":}"; // Malformed JSON

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    @DisplayName("23. Null product creation test")
    void testNullProductCreation() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Order(24)
    @DisplayName("24. Concurrent operations test")
    void testConcurrentOperations() throws Exception {
        Product product1 = createProductAndExtract(new Product("Concurrent Test 1", 100.00));
        Product product2 = createProductAndExtract(new Product("Concurrent Test 2", 200.00));

        // Update both products concurrently (simulated)
        mockMvc.perform(put("/api/products/" + product1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Product("Updated 1", 150.00))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/products/" + product2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Product("Updated 2", 250.00))))
                .andExpect(status().isOk());

        // Verify both updates were successful
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Updated 1", "Updated 2")));
    }
}