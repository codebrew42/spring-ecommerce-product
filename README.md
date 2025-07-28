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
-
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
    - [x] Allow only these special characters: `( )`, `[ ]`, `+`, `-`, `&`, `/`, `_`
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
  - [] (optional) custom function to handle `MethodArgumentNotValidException` in GlobalControllerAdvice 

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
- [ ] **User authentication integration**
  - [] allow users to do [1]-[3] using the `token` which user received after login
- [ ] **Cart management features**
  - [ ] **Get cart contents**
    - [ ] Retrieve all products in user's personal cart
    - [ ] Return cart items with product details and quantities
  - [ ] **Add to cart**
    - [ ] Add products to user's personal cart
    - [ ] Handle quantity and duplicate product additions
  - [ ] **Remove from cart**
    - [ ] Remove specific items from cart
    - [ ] Support clearing entire cart
- [ ] **Database design**
  - [ ] Create `cart` table with user_id, product_id, quantity, added_at columns
  - [ ] Create `CartRepository` with JDBC operations
  - [ ] Create `Cart` domain model and `CartRequest` DTO
- [ ] **Authentication service layer**
  - [ ] Create `MemberService` for token validation and user lookup
  - [ ] Implement JWT token parsing and validation
  - [ ] Handle authentication exceptions properly

### Step 2.4: authorized admin can get specific N-products and N-users
- [ ] **Admin authorization system**
  - [ ] Implement `HandlerInterceptor` for role-based access control
  - [ ] Restrict `/admin/*` endpoints to ADMIN role only
  - [ ] Create `AuthInterceptor` to check user permissions
  - [ ] Register interceptor in WebMvcConfigurer
- [ ] **Top products analytics**
  - [ ] Get top 5 most added products to cart in last 30 days
  - [ ] Handle tie-breaking by most recent addition time
  - [ ] Use SQL: `WHERE`, `DATE`, `GROUP BY`, `ORDER BY`, `LIMIT`
  - [ ] Response includes:
    - [ ] Product name
    - [ ] Number of times added to cart
    - [ ] Most recent addition timestamp
- [ ] **Active users analytics**
  - [ ] Get members who added items to cart in last 7 days
  - [ ] Ensure unique member list (no duplicates)
  - [ ] Use SQL: `EXISTS`, `DISTINCT`, `JOIN`
  - [ ] Response includes:
    - [ ] Member ID
    - [ ] Member name
    - [ ] Member email
- [ ] **SQL optimization and implementation**
  - [ ] Create analytics queries in repository layer
  - [ ] Create `AnalyticsRepository` for complex queries
  - [ ] Create response DTOs for analytics data
  - [ ] Implement proper error handling for admin endpoints
