# API 명세서 (순위별 누적)

각 순위가 끝났을 때 존재해야 하는 API 목록을 누적 형태로 정리한다. 공통 응답 포맷:

```json
{ "code": 200, "message": "OK", "data": { } }
```

에러 응답 예시:
```json
{ "code": 404, "message": "Data not found", "data": null }
```

---

## 1순위 완료 시점 — Product/Customer CRUD

### 상품

| Method | URI | 설명 | 요청 예시 | 에러 |
|---|---|---|---|---|
| GET | `/api/products/list?offset=0&count=10` | 상품 목록(페이징) | - | - |
| GET | `/api/products/{id}` | 상품 상세 | - | `DATA_NOT_FOUND`(404) |
| POST | `/api/products` | 상품 등록 | `{"productName":"무선마우스","productPrice":15000}` | `ParameterException`(400), `DATA_DUPLICATED`(409) |
| PUT | `/api/products` | 상품 수정 | `{"id":1,"productName":"무선마우스2","productPrice":16000}` | `ParameterException`(400), `DATA_NOT_FOUND`(404) |
| DELETE | `/api/products` | 상품 삭제 | `{"id":1}` | `DATA_NOT_FOUND`(404) |

응답 예시 (`GET /api/products/list`):
```json
{ "code":200, "message":"OK", "data": {
  "content":[{"id":1,"productName":"무선마우스","productPrice":15000}],
  "totalPages":1, "totalElements":3, "offset":0, "count":10 } }
```

### 고객

| Method | URI | 설명 | 요청 예시 | 에러 |
|---|---|---|---|---|
| GET | `/api/customers/list?offset=0&count=10` | 고객 목록(페이징) | - | - |
| GET | `/api/customers/{customerId}` | 고객 상세 + 보유 상품목록 | - | `DATA_NOT_FOUND`(404) |
| POST | `/api/customers` | 회원가입(초기 포인트 지급) | `{"customerId":"skala01","customerPassword":"pw1234"}` | `ParameterException`(400), `DATA_DUPLICATED`(409) |
| PUT | `/api/customers` | 고객 정보 수정(포인트 등) | `{"customerId":"skala01","customerPoint":100000}` | `DATA_NOT_FOUND`(404) |
| DELETE | `/api/customers` | 고객 삭제 | `{"customerId":"skala01"}` | `DATA_NOT_FOUND`(404) |

응답 예시 (`GET /api/customers/skala01`):
```json
{ "code":200, "message":"OK", "data": {
  "customerId":"skala01", "customerPoint":970000,
  "products":[{"productId":1,"productName":"무선마우스","productPrice":15000,"quantity":2}] } }
```

---

## 2순위 완료 시점 — 위 API + 로그인/주문/취소 + Swagger

### 고객 (추가)

| Method | URI | 설명 | 요청 예시 | 에러 |
|---|---|---|---|---|
| POST | `/api/customers/login` | 로그인 → 세션/토큰 쿠키 발급(`bff-access`) | `{"customerId":"skala01","customerPassword":"pw1234"}` | `ParameterException`(400), `DATA_NOT_FOUND`(404), `NOT_AUTHENTICATED`(401) |
| POST | `/api/customers/order` | 상품 주문(로그인 필요, 포인트 차감) | `{"productId":1,"quantity":2}` | `DATA_NOT_FOUND`(404), `INSUFFICIENT_FUNDS`(400) |
| POST | `/api/customers/cancel` | 주문 취소(포인트 환급) | `{"productId":1,"quantity":1}` | `DATA_NOT_FOUND`(404), `INSUFFICIENT_QUANTITY`(400) |

응답 예시 (`POST /api/customers/order`):
```json
{ "code":200, "message":"OK", "data": {"customerId":"skala01","customerPoint":970000} }
```

### 문서화

| 항목 | 경로 |
|---|---|
| Swagger UI | `/swagger-ui/index.html` |
| OpenAPI JSON | `/v3/api-docs` |

---

## 3순위 — 하위 단계별 API (사용자 승인 게이트에 따라 순차 추가)

### 3-1 장바구니 완료 시

| Method | URI | 설명 | 요청 예시 |
|---|---|---|---|
| GET | `/api/customers/cart` | 내 장바구니 조회 | - |
| POST | `/api/customers/cart` | 장바구니 담기 | `{"productId":1,"quantity":2}` |
| DELETE | `/api/customers/cart` | 장바구니 항목 제거 | `{"productId":1,"quantity":1}` |
| POST | `/api/customers/cart/checkout` | 장바구니 전체 주문 확정 | - |

### 3-2 주문 상세/내역 완료 시

| Method | URI | 설명 |
|---|---|---|
| GET | `/api/customers/{customerId}/orders` | 주문/취소 이력 목록(시간 역순) |
| GET | `/api/customers/{customerId}/orders/{orderId}` | 이력 상세 |

### 3-3 리뷰/평점 완료 시

| Method | URI | 설명 | 요청 예시 | 에러 |
|---|---|---|---|---|
| GET | `/api/products/{productId}/reviews` | 리뷰 목록 + 평균 평점 | - | - |
| POST | `/api/products/{productId}/reviews` | 리뷰 작성 (해당 상품 구매 이력 필요) | `{"rating":5,"comment":"좋아요"}` | `ParameterException`(rating 1~5), `PURCHASE_REQUIRED`(403, 구매 이력 없음) |
| DELETE | `/api/reviews/{reviewId}` | 리뷰 삭제(작성자만) | - | `NOT_AUTHENTICATED`(401) |

### 3-4 위시리스트 완료 시

| Method | URI | 설명 | 요청 예시 | 에러 |
|---|---|---|---|---|
| GET | `/api/customers/wishlist` | 위시리스트 조회 | - | - |
| POST | `/api/customers/wishlist` | 찜하기 | `{"productId":1}` | `DATA_DUPLICATED`(409) |
| DELETE | `/api/customers/wishlist` | 찜 삭제 | `{"productId":1}` | `DATA_NOT_FOUND`(404) |

### 3-5 카테고리/검색 완료 시

| Method | URI | 설명 | 요청 예시 |
|---|---|---|---|
| GET | `/api/categories` | 카테고리 목록 | - |
| POST | `/api/categories` | 카테고리 등록 | `{"categoryName":"주변기기"}` |
| PUT | `/api/categories` | 카테고리 수정 | `{"id":1,"categoryName":"PC주변기기"}` |
| DELETE | `/api/categories` | 카테고리 삭제 | `{"id":1}` |
| GET | `/api/products/search?keyword=마우스&categoryId=1` | 상품 검색(키워드+카테고리) | - |

---

## 4순위 완료 시점 — JWT 적용 후 달라지는 부분 (보류)

기존 API 목록은 동일하되, 아래 API들은 **유효한 JWT 쿠키가 없으면 401 `NOT_AUTHENTICATED`**로 거부된다:
`POST /api/customers/order`, `POST /api/customers/cancel`, `/api/customers/cart/**`, `/api/customers/wishlist/**`.
`POST /api/customers/login` 응답 헤더에 `Set-Cookie: bff-access=<JWT>; HttpOnly`가 실제 서명된 토큰으로 발급된다.

## 5순위 완료 시점 — 배포 후 접근 경로 (보류)

API 경로/스펙 자체는 변경 없음. `docker run -p 8080:8080 shop-api:1.0` 실행 후 동일한 API를
`http://localhost:8080/api/...`로 호출 가능해야 한다.
