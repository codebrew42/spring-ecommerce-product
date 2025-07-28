package ecommerce.unit.validator

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class UniqueProductNameValidatorTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `should throw - duplicate product names`() {
        // fist product - should succeed
        val validRequest = """{
            "name" : "duplicate",
            "price" : 1.0,
            "imageUrl" : "http://google.com"
        }"""
        val header = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val response =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(validRequest, header),
                String::class.java,
            )

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        // second product (duplicate) - should throw
        val duplicateResponse =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(validRequest, header),
                String::class.java,
            )
        Assertions.assertThat(duplicateResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
