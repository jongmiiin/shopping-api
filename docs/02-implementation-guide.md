# 단계별 진행절차 (구현 가이드)

이 문서는 각 순위 구현 시 참조하는 실행 체크리스트다. base package는 `com.skala.shopapi`이며 `entity`/`dto`는
`data/` 래핑 없이 바로 하위 패키지로 둔다 (`src/main/java/com/skala/shopapi/{package}/{Class}.java`).

## 진행 규칙 (승인 게이트)
- 1순위 → 2순위는 이어서 진행한다.
- **3순위는 3-1 ~ 3-5를 하나씩 구현한다. 각 하위 단계를 완료하면 사용자에게 보고하고, 사용자가 결과를 확인한 뒤
  다음 단계 진행을 지시해야 착수한다.** 임의로 이어서 진행하지 않는다.
- **4순위(JWT)·5순위(Docker)는 3순위 전체가 끝난 뒤 사용자가 명시적으로 요청할 때만 착수한다.**
- 각 순위를 마치면 `요구사항 명세서`(01)의 해당 절, `API 명세서`(03)의 해당 절과 대조해 누락이 없는지 확인한다.

---

## 1순위 — CRUD 기능 구현

### common
- `Response<T>`: `int code`, `String message`, `T data` 필드. 정적 팩토리 `success(T data)`,
  `success(String message, T data)` 제공. `GlobalExceptionHandler`가 에러 응답도 이 타입으로 감싼다.
- `PagedList<T>`: `List<T> content`, `int totalPages`, `long totalElements`, `int offset`, `int count`.
  `Page<T>`(Spring Data)를 받아 변환하는 정적 팩토리 `of(Page<T> page, int offset, int count)` 제공.

### exception
- `Error` enum — 각 값은 (HTTP status, code, 기본 message)를 가진다:
  `DATA_NOT_FOUND(404)`, `DATA_DUPLICATED(409)`, `INSUFFICIENT_FUNDS(400)`, `INSUFFICIENT_QUANTITY(400)`,
  `NOT_AUTHENTICATED(401)`. (`INSUFFICIENT_FUNDS`/`INSUFFICIENT_QUANTITY`/`NOT_AUTHENTICATED`는 1순위에서
  값만 정의하고 실제로는 2·4순위에서 사용)
- `ResponseException extends RuntimeException`: `ResponseException(Error error)`,
  `ResponseException(Error error, String message)`, `getError()` 제공
- `ParameterException extends RuntimeException`: `ParameterException(String... fieldNames)` —
  누락된 필드명을 메시지에 포함
- `GlobalExceptionHandler` (`@RestControllerAdvice`): `ResponseException` → `error.httpStatus`로 매핑,
  `ParameterException` → 400, `NoResourceFoundException`(매핑된 핸들러가 없는 경로) → 404, 그 외 `Exception` → 500
  폴백. 모두 `Response<Void>`로 감싸 반환.
  (주의: `NoResourceFoundException`을 별도 처리하지 않고 `Exception.class` catch-all에만 맡기면 "/"나 오타 URL
  같은 정상적인 404 상황까지 500으로 잘못 응답한다 — 실제로 이 문제로 H2 콘솔 접속 시 500이 발생했었음)

### tools
- `StringUtil.isAnyEmpty(String... values)`: null 또는 빈 문자열이 하나라도 있으면 true

### entity
- `Product` (`@Entity @Table(name="product")`, Lombok `@Getter @Setter @NoArgsConstructor`)
  - `@Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id`
  - `String productName`, `Double productPrice`
  - 커스텀 생성자 `Product(String productName, Double productPrice)`
- `Customer` (`@Entity @Table(name="customer")`)
  - `@Id String customerId` (자동증가 아님)
  - `String customerPassword`, `Double customerPoint`
  - 커스텀 생성자 `Customer(String customerId, Double customerPoint)`
- `OrderItem` (`@Entity @Table(name="order_item")`)
  - `@Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id`
  - `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id") Customer customer`
  - `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id") Product product`
  - `Integer quantity`
  - 커스텀 생성자 `OrderItem(Customer customer, Product product, Integer quantity)`

### dto
- `OrderItemDto` (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`):
  `Long productId`, `String productName`, `Double productPrice`, `Integer quantity`
- `OrderListDto` (동일 어노테이션): `String customerId`, `Double customerPoint`, `List<OrderItemDto> products`

### repository
- `ProductRepository extends JpaRepository<Product, Long>` + `Optional<Product> findByProductName(String productName)`
- `CustomerRepository extends JpaRepository<Customer, String>` (커스텀 메서드 없음)
- `OrderItemRepository extends JpaRepository<OrderItem, Long>` +
  `List<OrderItem> findByCustomer_CustomerId(String customerId)`,
  `Optional<OrderItem> findByCustomerAndProduct(Customer customer, Product product)`

### service
- `ProductService` (`@Service @RequiredArgsConstructor`, `private final ProductRepository productRepository`):
  - `Response<PagedList<Product>> getAllProducts(int offset, int count)` — `PageRequest.of(offset, count)`
  - `Response<Product> getProductById(Long id)` — 없으면 `ResponseException(Error.DATA_NOT_FOUND)`
  - `Response<Product> createProduct(Product product)` — `StringUtil.isAnyEmpty(productName)` 또는
    `productPrice == null || productPrice <= 0`이면 `ParameterException("productName","productPrice")`;
    `findByProductName` 존재 시 `ResponseException(Error.DATA_DUPLICATED)`; `product.setId(null)` 후 save
    (주의: `Long id`는 boxed 타입이라 `0L`을 넣으면 Spring Data JPA `save()`가 기존 엔티티로 오인해 `merge()`를
    호출하다 `StaleObjectStateException`이 남 — 반드시 `null`로 세팅해 `persist()` 경로를 타게 해야 함)
  - `Response<Product> updateProduct(Product product)` — 검증 동일, 대상 없으면 `DATA_NOT_FOUND`, 있으면 save
  - `Response<Void> deleteProduct(Product product)` — 대상 없으면 `DATA_NOT_FOUND`, 있으면 delete
- `CustomerService` (`@Service @RequiredArgsConstructor`) — 1순위 범위 필드/메서드만 우선 작성:
  `private final CustomerRepository customerRepository; private final OrderItemRepository orderItemRepository`
  (`productRepository`, `sessionHandler` 필드는 2순위에서 추가)
  - `Response<PagedList<Customer>> getAllCustomers(int offset, int count)`
  - `@Transactional(readOnly=true) Response<OrderListDto> getCustomerById(String customerId)` —
    없으면 `DATA_NOT_FOUND`; `orderItemRepository.findByCustomer_CustomerId`로 보유상품 조회 →
    Stream으로 `OrderItemDto` 리스트 변환 → `OrderListDto.builder()...build()`
  - `Response<Customer> createCustomer(Customer customer)` — `customerId`/`customerPassword` 검증,
    ID 중복 시 `DATA_DUPLICATED`, 초기 포인트 세팅 후 save
  - `Response<Customer> updateCustomer(Customer customer)` — `customerId` 존재 확인 후 포인트 업데이트
  - `Response<Void> deleteCustomer(Customer customer)` — 존재 확인 후 삭제

### controller
- `ProductController` (`@RestController @RequestMapping("/api/products") @RequiredArgsConstructor`):
  `GET /list` (`@RequestParam(defaultValue="0") Integer offset, @RequestParam(defaultValue="10") Integer count`),
  `GET /{id}` (`@PathVariable Long id`), `POST` (`@RequestBody Product`), `PUT` (`@RequestBody Product`),
  `DELETE` (`@RequestBody Product`)
- `CustomerController` (`@RestController @RequestMapping("/api/customers") @RequiredArgsConstructor`):
  `GET /list`, `GET /{customerId}` (`@PathVariable String customerId`), `POST` (`@RequestBody Customer`),
  `PUT` (`@RequestBody Customer`), `DELETE` (`@RequestBody Customer`) — login/order/cancel은 2순위에서 추가

### resources
- `application.yml`:
  ```yaml
  spring:
    application:
      name: shopapi
    datasource:
      url: jdbc:h2:mem:shopapi
    jpa:
      hibernate:
        ddl-auto: update
      defer-datasource-initialization: true
      show-sql: true
    sql:
      init:
        mode: always
    h2:
      console:
        enabled: true
  server:
    port: 8080
  ```
- `data.sql`: 상품 3종 시드
  ```sql
  INSERT INTO product (product_name, product_price) VALUES ('무선마우스', 15000);
  INSERT INTO product (product_name, product_price) VALUES ('블루투스키보드', 29000);
  INSERT INTO product (product_name, product_price) VALUES ('USB허브', 39000);
  ```
- **H2 콘솔 주의**: Spring Boot 4.x는 `H2ConsoleAutoConfiguration`을 제거했다(`spring-boot-autoconfigure` 4.1.0
  jar 전체에 해당 클래스가 없음을 확인). `spring.h2.console.enabled=true`만으로는 콘솔이 뜨지 않으므로
  `config/H2ConsoleConfig`(`@ConditionalOnProperty(spring.h2.console.enabled)`)에서 서블릿을 직접 등록해야 한다.
  또한 H2가 제공하는 `org.h2.server.web.WebServlet`은 `javax.servlet` 기반이라 Jakarta 컨테이너(Tomcat 10+)에
  등록 불가 — 반드시 `org.h2.server.web.JakartaWebServlet`을 사용한다. 이 클래스를 컴파일 시점에 참조해야 하므로
  `com.h2database:h2`는 `runtimeOnly`가 아니라 `implementation`으로 선언해야 한다. 접속 경로는 `/h2-console/`
  (트레일링 슬래시 없이 접속하면 자동 302 리다이렉트), JDBC URL은 `jdbc:h2:mem:shopapi`, 사용자 `sa`, 비밀번호 없음.

---

## 2순위 — 나머지 API + Swagger 문서화

### dto 추가
- `OrderRequest` (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor`): `Long productId`, `Integer quantity`
- `CustomerSession` (동일): `String customerId`, `String customerPassword`

### common
- `SessionHandler` (`@Component`) — **JWT 이전 임시 구현**:
  - `void storeAccessToken(HttpServletResponse response, String customerId)` — 쿠키 `bff-access`에 customerId를
    Base64 인코딩해 저장 (`Cookie` 객체, `httpOnly(true)`, `path("/")`)
  - `String getCurrentCustomerId(HttpServletRequest request)` — 쿠키에서 `bff-access` 찾아 디코드,
    없으면 `ResponseException(Error.NOT_AUTHENTICATED)`
  - 4순위에서 내부 구현만 `JwtTokenProvider` 기반으로 교체하고 이 두 메서드 시그니처는 유지

### service — CustomerService 필드/메서드 추가
- 필드 추가: `private final ProductRepository productRepository; private final SessionHandler sessionHandler`
- `Response<Customer> loginCustomer(CustomerSession customerSession, HttpServletResponse response)` —
  검증(`ParameterException`) → `customerRepository.findById` 없으면 `DATA_NOT_FOUND` →
  비밀번호 불일치 시 `NOT_AUTHENTICATED` → `sessionHandler.storeAccessToken(response, customerId)` →
  `customer.setCustomerPassword(null)` 후 반환
- `@Transactional Response<Customer> placeOrder(OrderRequest order, HttpServletRequest request)` —
  `sessionHandler.getCurrentCustomerId(request)` → customer/product 조회(`DATA_NOT_FOUND`) →
  `customer.getCustomerPoint() < product.getProductPrice() * quantity`면 `INSUFFICIENT_FUNDS` →
  포인트 차감 → `orderItemRepository.findByCustomerAndProduct` 있으면 수량 누적, 없으면 신규 `OrderItem` 저장
- `@Transactional Response<Customer> cancelOrder(OrderRequest order, HttpServletRequest request)` —
  현재 customerId 추출 → 대상 `OrderItem` 조회(`DATA_NOT_FOUND`) → 취소 수량이 보유 수량 초과 시
  `INSUFFICIENT_QUANTITY` → 수량 차감(0이면 delete) → 포인트 환급

### controller — CustomerController 추가
- `POST /login` (`@RequestBody CustomerSession customerSession, HttpServletResponse response`)
- `POST /order` (`@RequestBody OrderRequest order, HttpServletRequest request`)
- `POST /cancel` (`@RequestBody OrderRequest order, HttpServletRequest request`)

### aop
- `ApiLoggingAspect` (`@Aspect @Component`): `@Around("execution(* com.skala.shopapi.controller..*(..))")`로
  메서드명, 파라미터, 반환값, 소요시간(ms)을 로깅
- 의존성 주의: Spring Boot 4.x부터는 `spring-boot-starter-aop`가 더 이상 게시되지 않는다(Maven Central 확인 결과
  최신 버전이 4.0.0-M2에서 끊김). 대신 `implementation 'org.aspectj:aspectjweaver'`를 직접 추가한다
  (`spring-aop`는 webmvc 스타터에 이미 포함됨).

### config / 문서화
- `build.gradle`의 `springdoc-openapi-starter-webmvc-ui:2.3.0`이 Spring Boot 4.1.0에서 정상 기동하는지
  `bootRun` 후 `/swagger-ui/index.html` 접속으로 확인. 기동 실패 시 Boot4 호환 버전으로 조정.
- `config/OpenApiConfig` (`@Configuration`): `@Bean OpenAPI customOpenAPI()`로 제목/설명 커스터마이즈
- 각 컨트롤러 클래스에 `@Tag(name=...)`, 각 메서드에 `@Operation(summary=...)` 추가

---

## 3순위 — 그 외 새로운 기능 (하나씩, 승인 게이트)

공통: Cart/Wishlist는 `SessionHandler.getCurrentCustomerId(request)`로 로그인 고객을 식별한다(2순위 임시 구현
그대로 사용, 4순위에서 JWT로 교체돼도 이 코드는 그대로 유지).

### 3-1 장바구니
- `entity/CartItem` (`@Entity @Table(name="cart_item")`): `Long id`(IDENTITY),
  `@ManyToOne(LAZY) Customer customer`, `@ManyToOne(LAZY) Product product`, `Integer quantity`
- `repository/CartItemRepository`: `findByCustomer_CustomerId(String)`, `findByCustomerAndProduct(Customer, Product)`
- `service/CartService` (`@Service @RequiredArgsConstructor`):
  `Response<Void> addToCart(OrderRequest req, HttpServletRequest request)` (기존 항목 있으면 수량 누적),
  `Response<Void> removeFromCart(OrderRequest req, HttpServletRequest request)`,
  `Response<List<CartItemDto>> getCart(HttpServletRequest request)`,
  `@Transactional Response<Customer> checkout(HttpServletRequest request)` — 장바구니 항목을 순회하며
  `CustomerService.placeOrder`를 그대로 호출해 재사용(중복 로직 없이) 후 장바구니 비움
- `controller/CartController` (`/api/customers/cart`): `GET`, `POST`, `DELETE`, `POST /checkout`
- `dto/CartItemDto`: `Long productId`, `String productName`, `Double productPrice`, `Integer quantity`
- **버그 및 수정 (Hibernate 프록시 직렬화 노출)**: `checkout()`처럼 같은 트랜잭션 안에서 지연 로딩 연관관계를
  먼저 건드린 뒤 같은 ID로 `repository.findById()`를 다시 호출하면, 영속성 컨텍스트가 이전에 만든 Hibernate
  프록시(ByteBuddy 서브클래스) 인스턴스를 그대로 반환한다. 이 프록시를 Jackson으로 직렬화하면
  `hibernateLazyInitializer` 같은 내부 필드가 JSON에 그대로 노출된다. `jackson-datatype-hibernate6`로 고치려
  했으나 Hibernate 7(이 프로젝트가 쓰는 버전)과 호환되지 않아 사용 불가 — 대신 `config/JacksonConfig`에서
  `Object.class`에 `@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})` 믹스인을 전역 등록해 해결.
  (참고: Boot 4는 Jackson 3 기반이라 커스터마이저 인터페이스도
  `org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer`이고 빌더 타입은
  `tools.jackson.databind.json.JsonMapper.Builder`다 — 예전 `Jackson2ObjectMapperBuilderCustomizer`는 없음)
- **버그 및 수정 (고객 존재 확인 누락)**: `CartService.getCart()`가 `addToCart`/`removeFromCart`/`checkout`과
  달리 `customerRepository.findById()`로 고객 존재를 확인하지 않고 바로 `cartItemRepository.findByCustomer_CustomerId`만
  호출했다. H2가 인메모리라 서버 재시작 시 고객 데이터가 사라지는데, 브라우저에 만료 없는 예전 `bff-access` 쿠키가
  남아있으면 그 쿠키가 가리키는 고객이 더 이상 존재하지 않아도 `getCart`는 빈 배열과 함께 200을 반환해버리는 반면
  담기/제거/체크아웃은 정상적으로 404를 반환하는 불일치가 있었다. `getCart`에도 동일한 고객 존재 검증을 추가해 해결.
  (근본 원인인 "쿠키에 만료·서명 검증이 없어 예전 로그인 세션이 영구적으로 유효한 것처럼 보이는 문제" 자체는
  4순위 JWT에서 정식으로 해결 예정 — 지금은 임시 SessionHandler의 알려진 한계로 남겨둠)

### 3-2 주문 상세 및 내역 관리
- `entity/OrderHistory` (`@Entity @Table(name="order_history")`): `Long id`(IDENTITY),
  `@ManyToOne(LAZY) Customer customer`, `@ManyToOne(LAZY) Product product`, `Integer quantity`,
  `Double amount`, `String type` (`"ORDER"`/`"CANCEL"`), `LocalDateTime orderedAt`
- `CustomerService.placeOrder`/`cancelOrder` 마지막에 `orderHistoryRepository.save(new OrderHistory(...))` 추가
- `repository/OrderHistoryRepository`: `findByCustomer_CustomerIdOrderByOrderedAtDesc(String)`
- `CustomerController` 확장: `GET /{customerId}/orders`(목록), `GET /{customerId}/orders/{orderId}`(상세,
  대상 없으면 `DATA_NOT_FOUND`)
- `dto/OrderHistoryDto`: `Long id`, `String productName`, `Integer quantity`, `Double amount`, `String type`,
  `LocalDateTime orderedAt`

### 3-3 상품 리뷰 및 평점
- `entity/Review` (`@Entity @Table(name="review")`): `Long id`(IDENTITY),
  `@ManyToOne(LAZY) Customer customer`, `@ManyToOne(LAZY) Product product`, `Integer rating`, `String comment`,
  `LocalDateTime createdAt`
- `repository/ReviewRepository`: `findByProduct_Id(Long productId)`,
  `@Query("select avg(r.rating) from Review r where r.product.id = :productId") Double findAverageRatingByProductId(Long productId)`
- `service/ReviewService`: `createReview(Long productId, ReviewRequest req, HttpServletRequest request)` —
  `rating`이 1~5 밖이면 `ParameterException("rating")`; **`orderHistoryRepository.existsByCustomer_CustomerIdAndProduct_IdAndType(customerId, productId, OrderHistory.TYPE_ORDER)`로 구매 이력 확인, 없으면 `ResponseException(Error.PURCHASE_REQUIRED)`(403)** —
  주문을 취소했더라도 `OrderHistory`의 `ORDER` 레코드 자체는 남으므로 리뷰 작성은 계속 허용됨;
  `getReviewsByProduct(Long productId)` — 평균 평점 포함;
  `deleteReview(Long reviewId, HttpServletRequest request)` — 작성자 customerId 불일치 시 `NOT_AUTHENTICATED`
- `controller/ReviewController`: `GET /api/products/{productId}/reviews`, `POST /api/products/{productId}/reviews`,
  `DELETE /api/reviews/{reviewId}`
- `dto/ReviewRequest`: `Integer rating`, `String comment`

### 3-4 위시리스트/찜하기
- `entity/Wishlist` (`@Entity @Table(name="wishlist", uniqueConstraints=@UniqueConstraint(columnNames={"customer_id","product_id"}))`):
  `Long id`(IDENTITY), `@ManyToOne(LAZY) Customer customer`, `@ManyToOne(LAZY) Product product`
- `repository/WishlistRepository`: `findByCustomer_CustomerId(String)`, `existsByCustomerAndProduct(Customer, Product)`
- `service/WishlistService`: `addWishlist(Long productId, request)` — 이미 존재하면 `DATA_DUPLICATED`;
  `removeWishlist(Long productId, request)`; `getWishlist(request)`
- `controller/WishlistController` (`/api/customers/wishlist`): `GET`, `POST`, `DELETE`

### 3-5 카테고리 및 검색
- `entity/Category` (`@Entity @Table(name="category")`): `Long id`(IDENTITY), `String categoryName`
- `entity/Product` 확장: `@ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id") Category category` (nullable)
- `repository/CategoryRepository extends JpaRepository<Category, Long>`
- `ProductRepository` 추가: `findByProductNameContainingAndCategory_Id(String keyword, Long categoryId)`,
  `findByProductNameContaining(String keyword)`, `findByCategory_Id(Long categoryId)`
- `controller/CategoryController` (`/api/categories`): 기본 CRUD (Product 패턴과 동일)
- `ProductController` 확장: `GET /api/products/search?keyword=&categoryId=` — 파라미터 조합에 따라 적절한
  repository 메서드 분기 호출

---

## 4순위 — JWT 구현 (보류)

- `build.gradle`에 `io.jsonwebtoken:jjwt-api`, `jjwt-impl`(runtimeOnly), `jjwt-jackson`(runtimeOnly) 추가
- `tools/JwtTokenProvider` (`@Component`): `application.yml`의 `jwt.secret`/`jwt.expiration` 주입,
  `String generateToken(String customerId)`, `String parseCustomerId(String token)` (서명/만료 검증 실패 시
  `ResponseException(Error.NOT_AUTHENTICATED)`)
- `common/SessionHandler` 내부를 `JwtTokenProvider` 기반으로 교체: `storeAccessToken`은 JWT를 생성해 쿠키에 저장,
  `getCurrentCustomerId`는 쿠키의 JWT를 파싱해 customerId 반환 — **메서드 시그니처는 2·3순위와 동일하게 유지**
- `config/AuthInterceptor` (`HandlerInterceptor`) + `WebMvcConfigurer`에 등록: `/api/customers/order`,
  `/api/customers/cancel`, `/api/customers/cart/**`, `/api/customers/wishlist/**`에 적용
- `application.yml`에 `jwt.secret`, `jwt.expiration` 추가

## 5순위 — Docker 배포 (보류)

- `Dockerfile` (멀티스테이지): `FROM gradle:...-jdk21 AS build` → `./gradlew build -x test` →
  `FROM eclipse-temurin:21-jre` → `COPY --from=build build/libs/*.jar app.jar` → `EXPOSE 8080` →
  `ENTRYPOINT ["java","-jar","/app.jar"]`
- `.dockerignore`: `.gradle`, `build/`, `.git`, `docs/`
- 빌드/실행: `./gradlew build` → `docker build -t shop-api:1.0 .` → `docker run -p 8080:8080 shop-api:1.0`
