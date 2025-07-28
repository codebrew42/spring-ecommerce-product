package ecommerce.integration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
class ProductControllerIntegrationTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["j", " j", "j ", "j j", " j j "])
    fun `should accept - valid names`(validName: String) {
        val validRequest = """{
            "name" : "$validName",
            "price" : 1.0,
            "imageUrl" : "http://google.com"
        }"""
        val headers = createHeaders()
        val response =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(validRequest, headers),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @ParameterizedTest
    @ValueSource(strings = ["123456789012345", " ()[]+-&/_aaaa "])
    fun `should accept - valid long names`(validName: String) {
        val request = """{
            "name" : "$validName",
            "price" : 1.0,
            "imageUrl" : "http://google.com"
            }"""
        val headers = createHeaders()
        val response =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(request, headers),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  "])
    fun `should throw - invalid empty names`(invalidName: String) {
        val request = """{
            "name" : "$invalidName",
            "price" : 1.0,
            "imageUrl" : "http://google.com"
            }"""
        val headers = createHeaders()
        val response =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(request, headers),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1234567890123456", " ()[]+-&/_aaaabbb "])
    fun `should throw - invalid too long names`(invalidName: String) {
        val request = """{
            "name": "$invalidName",
            "price": 1.0,
            "imageUrl": "http://google.com"
        }"""
        val headers = createHeaders()
        val response =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(request, headers),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["hi!", "pct%", "bck\\slash", "crly{}", "hi@", "hi#", "tlda~", "emji🙂", "korean한글", "comm,a"],
    )
    fun `should throw - invalid names with not allowed special characters`(invalidName: String) {
        val request = """{
            "name": "$invalidName",
            "price": 1.0,
            "imageUrl": "http://google.com"
        }"""
        val headers = createHeaders()
        val response =
            testRestTemplate.postForEntity(
                "/api/products",
                HttpEntity(request, headers),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
