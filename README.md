# spring-ecommerce-product

## Features
### Step 1.1: introduce class `product` and its id will be automatically handled
- [x] Create a Product class
  - [x] contains id: Long, name: String, price: Double, imageUrl: String
  - [x] use AtomicLong to create the Id
- [x] Create ProductController
  - [x] use @RestController to return always JSON
  - [x] Create the "Database" in the form of HashMap()
  - [x] Create CRUD operations
- [x] Create a GlobalControllerAdvise to handle Exceptions

### Step 1.2: inject product service dependency to controller
- [x] Implement a controller that return html
- [x] Detach the "Database" to be accessible by the two controllers
- [x] Create a ProductService to simulate the connection with a real DataBase
- [x] Inject the ProductService dependency to the controllers
- [x] Create a template html with the list of all products
- [x] Add JS for CRUD request in the frontend.
- [x] Display image instead of a string on image URL

### Step 1.3: introduce H2 database and replace product service with *product repository
#### *contains helper functions for building responses
- [x] Configure H2 database
- [x] Create product repository for database operation
- [x] Create data schema and initialize and insert data to it
- [x] Modify the controller to use the new product repository

### Step 2.1: Product Validation System
- [x] **Comprehensive product validation**
  - [x] **Product name validation**
    - [x] Maximum 15 characters including spaces
    - [x] Allow only these special characters: `( )`, `[]`, `+`, `-`, `&`, `/`, `_`
    - [x] Ensure unique names across all products (needs custom `@UniqueProductName` validator)
  - [x] **Product price validation**
    - [x] Must be greater than 0 (using `@DecimalMin`)
  - [x] **Product image URL validation**
    - [x] Must start with `http://` or `https://` (using `@Pattern` regex)
- [x] **Proper error handling with Jakarta Bean Validation**
  - [x] Return appropriate HTTP status codes for validation failures (400 Bad Request)
  - [x] Provide clear, specific error messages for each validation case
  - [x] Handle duplicate name conflicts with meaningful responses
- [x] **Implementation details**
  - [x] Created `ProductRequest.kt` with validation annotations
  - [x] Used `@Valid` in controller methods
  - [x] Custom function to handle `MethodArgumentNotValidException` in GlobalControllerAdvice 

### Step 2.2: clients can register and login as a user
- [x] **User registration feature**
  - [x] Accept email and password in JSON format
  - [x] Validate email format and password requirements
  - [x] Generate and return JWT access token upon successful registration
  - [x] Store user credentials securely in database (hash passwords)
- [x] **User login feature**
  - [x] Accept email and password credentials
  - [x] Verify credentials against registered users
  - [x] Issue JWT token for authenticated users
  - [x] Include user claims (id, name, role) in token
- [x] **JWT token management**
  - [x] Add JJWT library dependency
  - [x] Generate secure tokens with configurable expiration
  - [x] Implement token validation for protected endpoints
  - [x] Create token service for generation and verification
- [x] **Database design**
  - [x] Create `members` table with id, email, password, name, role columns
  - [x] Create `MemberRepository` with JDBC operations
  - [x] Create `Member` domain model and `MemberRequest` DTO
- [x] **Authentication error handling**
  - [x] Return `401 Unauthorized` for missing header, invalid tokens
  - [x] Return `403 Forbidden` for incorrect login attempts
  - [x] Provide clear error messages for authentication failures

### Step 2.3: authorized users can add products to their cart
- [x] **User authentication integration**
  - [x] allow users to do [1]-[3] using the `token` which user received after login
- [x] **Cart management features**
  - [x] **Get cart contents**
    - [x] Retrieve all products in user's personal cart
    - [x] Return cart items with product details and quantities
  - [x] **Add to cart**
    - [x] Add products to user's personal cart
    - [x] Handle quantity and duplicate product additions
  - [x] **Remove from cart**
    - [x] Remove specific items from cart
    - [x] Support clearing entire cart
- [x] **Database design**
  - [x] Create `cart` table with user_id, product_id, quantity, added_at columns
  - [x] Create `CartRepository` with JDBC operations
  - [x] Create `Cart` domain model and cart DTOs (`AddToCartRequest`, `UpdateQuantityRequest`)
- [x] **JWT design**
  - [x] use header `Authorization: Bearer <token>`
- [x] **Authentication interceptor layer**
  - [x] Use existing `TokenService` for JWT token parsing and validation
  - [x] `AuthInterceptor` handles cart endpoint authentication
  - [x] Handle authentication exceptions properly with 401/403 responses

### Step 2.4: authorized admin can get specific N-products and N-users
- [x] **Admin authorization system**
  - [x] Implement `HandlerInterceptor` for role-based access control
  - [x] Restrict `/admin/*` endpoints to ADMIN role only
  - [x] Create `AuthInterceptor` to check user permissions
  - [x] Register interceptor in WebMvcConfigurer
- [x] **Top products analytics**
  - [x] Get top 5 most added products to cart in last 30 days
  - [x] Handle tie-breaking by most recent addition time
  - [x] Use SQL: `WHERE`, `DATE`, `GROUP BY`, `ORDER BY`, `LIMIT`
  - [x] Response includes:
    - [x] Product name
    - [x] Number of times added to cart (sum of quantities)
    - [x] Most recent addition timestamp
- [x] **Active users analytics**
  - [x] Get members who added items to cart in last 7 days
  - [x] Ensure unique member list (no duplicates)
  - [x] Use SQL: `EXISTS`, `DISTINCT`, `JOIN`
  - [x] Response includes:
    - [x] Member ID
    - [x] Member name
    - [x] Member email
- [x] **SQL optimization and implementation**
  - [x] Create analytics queries in repository layer
  - [x] Add database indexes for analytics performance
  - [x] Implement proper error handling for admin endpoints
  - [x] Use existing CartRepository (follows established architecture pattern)

# spring-ecommerce-orders-jpa

## Features
### TODO
- decide: where/how to validate elements of each entity
  -  Controller(*service: *Service)
    - fun (@Valid @RequestParam *repository: Repository) 
- how to handle: 
  - adding product to cart
  - choosing product option
  - how to decrease the quantity of a product option? 
    - both stock : productOption -> product
      - cases: Mango(Quantity: 10) (option: apple Mango(Q:5). ... ) 
        - a member wants to buy apple mango less thn 5 (OK)\
        - -> in the dto/ProductRequest
         
### Flow in Case: a member add a product(with options >= 1) to a cart
- related part
  - controller/CartController
    - fun `addToCart`
      - try (CartItemService.addTocart) 
    - fun `deleteCartItem`
      - @DeleteMapping("/delete/{id}") 
      - try (CartItemService.deleteCart) -> throw ResponseStatusException -> throw Exception
  - request/AddToCartRequest
    - option1: memberId, productId, Set<optionId> 
      - + entity: data class productOption(var id: Long, var name: String, var price: Double, var img: String, var quantity: Int)
    - option2: memberId, productId, Map<quantity, optionId>, 

  - service/CartItemServiceImpl(request RequestAddToCart)
    - validate `request`
    - search `matchedProduct` 
      - -> if not found, throw exception
      - -> if found,
    - validate `product option` 
      - -> if option not found, throw "choose an option"
      - -> if `prouctOption's Id` found, check the `quantity of that productOption`
    - save the info of `cartItem` 
    - return that item(cartItem)

  - model/Product 
  - + service/ProductService 
    - include `fun updateStock`: change 'quantity' directly inside
  - + service/ProductOptionService
    - include `fun updateStock`: change 'quantity' directly inside
  - + service/CartItemService
   - include `fun updateCartItem` 
- example: sending request
```aiignore
### 14. Create New Product (Admin can do this via products API)
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "name": "New Product",
  "price": 29.99,
  "imageUrl": "https://example.com/image.jpg"
  //HERE: product option part 
}
```


### Step 1.1(Entity Mapping), Step 1.2(Pagination), Step 1.3(Product Option)
- [] setup for migration 
  - Update build.gradle.kts:
  - Update application.properties
  - Decide: use MySQL-compatible behaviors along with H2

- [] update Entities
  - [] consider Mapping: `oneToMany/ManyToOne/OneToOne` + `fetch type: lazy/eager`
  - [] 1.Product
    - [] add `var stock: Int/Long/BigInt`
    - [] add `var hasStock: Boolean`
    - use `fun save` to add product's quantity
    - ProductOption
      - [] add `List<ProductOption>` or `Set<ProductOption>`
      - [] add `fun validate size of ProductOption`
        - one product has at least one option
        - rror handling: throw error when `options.isEmpty()`
      - [] add `fun addOption`
        - [] error handling: throw error in duplicate names case
      - [] check: add `fun removeOption`
        - [] error handling: throw error when `size of ProductOption < 1`
      - [] check: add `fun findOption`
  - [] 2.CartItem
    - [] val id: Long = 0, val memberId = UUID,... 
```aiignore
data class Cart(
    val id: Long? = null, //TODO: 0
    val memberId: Long, //TODO: consider `UUID`
    val productId: Long,
    val quantity: Int,
    val addedAt: LocalDateTime? = null,
)
```
  - [] 3.Member
  - [] 4.ProductOption
    - `class productOption(var id: Long, var name: String, var price: Double, var img: String, var quantity: Int)`
    - [] add `name` + validation
      - [] size: up to 50 characters, including spaces 
      - [] only allowed these special characters: `(, ), [, ], +, -, &, /, _`
        - [] use `exception` related class
      - [] unique: can't have duplicate option names in one product
    - [] add `quantity` + validation
      - [] range: at least 1 and less than 100,000,000 

- [] update Repositories
  - [] write override functions with return type: `Page<Product>`, `Slice<Product>`, `List<Product>`
  - [] import `pageable` and include it as a parameter

  - 1.`interface ProductRepository : JpaRepository<Product, Long>`
  - 2.`interface MemberRepository : JpaRepository<Member, Long>`
  - 3.`interface CartRepository : JpaRepository<Cart, Long>`



#### advice: Repository

    - Basic CRUD operations (save, findById, findAll, delete) are provided automatically
    - write prototypes of `Custom query methods` - JPA generates SQL from method names
```
    fun findByName(name: String): Optional<Product>
    fun findByNameContaining(keyword: String): List<Product>
    fun findByPriceGreaterThan(price: Double): List<Product>
    fun findByPriceBetween(minPrice: Double, maxPrice: Double): List<Product>
```
  - Custom SQL queries when method naming isn't enough
```
    @Query("SELECT p FROM Product p WHERE p.price > :price ORDER BY p.name")
    fun findExpensiveProducts(@Param("price") price: Double): List<Product>

    @Query(value = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?1)", nativeQuery = true)
    fun findByNameIgnoreCase(name: String): List<Product>
```
  - can also modify queries
```
    @Modifying
    @Query("UPDATE Product p SET p.price = p.price * 1.1 WHERE p.id = :id")
    fun increasePriceBy10Percent(@Param("id") id: Long) 
```
  - 2.`interface MemberRepository : JpaRepository<Product, Long>`
```
  fun findByEmail
  fun existsByEmail
```
  - 3. `interface CartRepository`
```aiignore
    fun findByMember(member: Member): List<Wish>
    fun findByMemberAndProduct(member: Member, product: Product): Optional<Wish>
    fun deleteByMemberAndProduct(member: Member, product: Product)
    
    // For admin analytics - top 5 most wished products in last 30 days
    @Query("""
        SELECT w.product, COUNT(w) as wishCount, MAX(w.createdAt) as lastWished
        FROM Wish w 
        WHERE w.createdAt >= :since 
        GROUP BY w.product 
        ORDER BY wishCount DESC, lastWished DESC
    """)
    fun findTopWishedProductsSince(@Param("since") since: LocalDateTime, pageable: Pageable): List<Any[]>
    
    // Members who added items in last 7 days
    @Query("""
        SELECT DISTINCT w.member 
        FROM Wish w 
        WHERE w.createdAt >= :since
    """)
    fun findActiveMembersSince(@Param("since") since: LocalDateTime): List<Member>
}
```
- [] write tests for testing repositories using `@DataJpaTest`

### 🧠 Migration Tip


```aiignore
//When migrating from JDBC where you manually check for nulls:

User user = resultSet.next() ? mapRow(resultSet) : null;

//Switch to:

Optional<User> user = userRepository.findById(id);

```