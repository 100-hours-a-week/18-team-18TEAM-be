# 리뷰 API

## 개요

사용자 간 리뷰(별점 + 태그 1~3개 + 후기)를 작성·수정·삭제하고,
집계된 리뷰 정보(베이지안 기반 점수, 평균 별점, 상위 태그 3개)를 조회하는 기능.
개별 리뷰 내용은 비공개이며, 집계 결과만 공개된다.
상대방 리뷰는 내 지갑에 그 사람의 명함이 있어야 조회 가능하다.

---

## API 목록

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/reviews?revieweeId={id}` | 내가 특정 상대에게 쓴 리뷰 단일 조회 |
| POST | `/api/reviews` | 리뷰 작성 |
| PATCH | `/api/reviews` | 리뷰 수정 |
| DELETE | `/api/reviews?revieweeId={id}` | 내가 쓴 리뷰 삭제 |
| GET | `/api/reviews/me` | 내 집계 리뷰 조회 |
| GET | `/api/reviews/users/{userId}` | 남의 집계 리뷰 조회 |
| GET | `/api/reviews/tags` | 태그 전체 조회 |

---

## 요구사항

- 리뷰는 reviewer → reviewee 방향으로 작성 (1:1, 중복 불가)
- 태그는 1~3개 선택
- 별점은 1~5 정수
- 후기(comment)는 선택 입력 (최대 500자)
- 리뷰 개별 내용은 비공개, 집계 결과만 공개
- 남의 집계 조회: `existsCollectedCardByOwner(requesterId, targetUserId)` 로 명함 보유 여부 확인
- 본인 리뷰 작성 불가

---

## 점수 산정 공식

### 목적
- 기준 30점에서 시작, 리뷰가 많을수록 항상 우상향
- 리뷰 적을 때 점수 튐 방지 (베이지안 평균), 많아도 폭주 방지 (clamp)

### 변수
- `n` : 리뷰 개수
- `Σr_i` : 별점 합계 (각 r_i = 1~5)

### 고정 파라미터
| 파라미터 | 값 | 설명 |
|---|---|---|
| `μ₀` | 3 | 기준(중립) 평균 점수 |
| `m` | 20 | 안정화용 기준 리뷰 개수 |
| `A` | 4 | 로그 보너스 계수 |
| `B` | 0.03 | 선형 보너스 계수 |

### 수식

```
bayesAvg  = (20 * 3 + Σr_i) / (20 + n)
          = (60 + Σr_i) / (20 + n)

baseScore = 30 + 15 * (bayesAvg - 3)

countBonus = 4 * ln(1 + n) + 0.03 * n

score = clamp(baseScore + countBonus, 0, 100)
```

### 최종 한 줄 수식
```
score = clamp(
  30 + 15 * ((60 + Σr_i) / (20 + n) - 3)
     + 4 * ln(1 + n)
     + 0.03 * n,
  0, 100
)
```

### Java 구현
```java
private double calculateScore(int reviewCount, int starScoreSum) {
    if (reviewCount == 0) return 30.0;

    double n = reviewCount;
    double sum = starScoreSum;

    double bayesAvg = (60 + sum) / (20 + n);
    double baseScore = 30 + 15 * (bayesAvg - 3);
    double countBonus = 4 * Math.log(1 + n) + 0.03 * n;

    return Math.clamp(baseScore + countBonus, 0.0, 100.0);
}
```

### 예시
| 리뷰 수 | 평균 별점 | 산출 점수 |
|---------|----------|----------|
| 0 | - | 30.0 |
| 5 | 3.0 | 36.4 |
| 20 | 4.0 | 49.7 |
| 100 | 4.5 | 78.1 |

> `Tag.score` 필드는 이 계산식과 무관

---

## 상세 스펙

### GET /api/reviews?revieweeId={id} — 내가 쓴 리뷰 단일 조회
내가 revieweeId 사용자에게 작성한 리뷰 1건 반환. 작성 이력 없으면 data: null.

**Response 200:**
```json
{
  "message": "내가 쓴 리뷰 조회 성공",
  "data": {
    "review_id": 5,
    "score": 4,
    "comment": "덕분에 프로젝트를 무사히 마쳤습니다!",
    "tags": [
      { "id": 1, "keyword": "소통 잘됨" },
      { "id": 2, "keyword": "책임감 있음" },
      { "id": 4, "keyword": "전문성 높음" }
    ]
  }
}
```

---

### POST /api/reviews — 리뷰 작성

**에러 케이스:**
- 본인 작성 → 400
- 중복 작성 → 409
- 태그 개수 < 1 또는 > 3 → 400
- 별점 범위 1~5 벗어남 → 400

**Request:**
```json
{
  "reviewee_id": 27,
  "tag_id_list": [1, 2, 4],
  "comment": "덕분에 프로젝트를 무사히 마쳤습니다!",
  "score": 4
}
```

**Response 201:**
```json
{
  "message": "리뷰 작성 성공",
  "data": { "review_id": 10 }
}
```

---

### PATCH /api/reviews — 리뷰 수정

- 본인 리뷰만 수정 가능 → 403
- null 필드는 수정하지 않음 (부분 수정)
- tag_id_list 변경 시 기존 ReviewTag 전체 삭제 후 재생성

**Request:**
```json
{
  "reviewee_id": 27,
  "tag_id_list": [1, 2, 4],
  "comment": "수정된 후기입니다.",
  "score": 5
}
```

**Response 200:**
```json
{
  "message": "리뷰 수정 성공",
  "data": {
    "review_id": 5,
    "score": 5,
    "comment": "수정된 후기입니다.",
    "tags": [
      { "id": 1, "keyword": "소통 잘됨" },
      { "id": 2, "keyword": "책임감 있음" },
      { "id": 4, "keyword": "전문성 높음" }
    ]
  }
}
```

---

### DELETE /api/reviews?revieweeId={id} — 리뷰 삭제

- 내가 revieweeId에게 쓴 리뷰 soft delete
- 본인 리뷰만 삭제 가능 → 403
- 리뷰 없음 → 404

**Response 200:**
```json
{ "message": "리뷰 삭제 성공", "data": null }
```

---

### GET /api/reviews/me — 내 집계 리뷰 조회

**Response 200:**
```json
{
  "message": "내 리뷰 조회 성공",
  "data": {
    "review_count": 12,
    "average_score": 4.3,
    "calculated_score": 62.7,
    "top_tags": [
      { "id": 1, "keyword": "소통 잘됨", "count": 8 },
      { "id": 2, "keyword": "책임감 있음", "count": 6 },
      { "id": 4, "keyword": "전문성 높음", "count": 5 }
    ]
  }
}
```

> `top_tags`: count 내림차순 상위 3개. 리뷰가 없으면 빈 배열.

---

### GET /api/reviews/users/{userId} — 남의 집계 리뷰 조회

- 내 지갑에 userId의 명함 없으면 → 403
- 응답 구조는 `/api/reviews/me` 와 동일

---

### GET /api/reviews/tags — 태그 전체 조회

**Response 200:**
```json
{
  "message": "태그 조회 성공",
  "data": [
    { "id": 1, "keyword": "소통 잘됨" },
    { "id": 2, "keyword": "책임감 있음" }
  ]
}
```

---

## 처리 흐름

### 리뷰 작성
```
[인증] reviewer 확인
  → 본인 여부 체크 (reviewer == reviewee → 400)
  → 중복 체크 (ReviewRepository.existsByReviewerIdAndRevieweeId → 409)
  → 태그 개수 검증 (1 ≤ size ≤ 3)
  → Review 저장
  → ReviewTag 저장 (Tag는 마스터 테이블 — getReferenceById 사용)
  → 201
```

### 남의 집계 리뷰 조회
```
[인증] requester 확인
  → existsCollectedCardByOwner(requesterId, targetUserId) → 403
  → reviewee_id = userId 기준 Review 집계 쿼리
  → review_count, Σr_i 계산 → average_score, calculated_score 산출
  → ReviewTag 기준 태그별 count → 상위 3개 추출
  → 200
```

---

## 변경 파일 목록

### 신규
| 파일 | 내용 |
|------|------|
| `domain/review/controller/ReviewController.java` | 7개 엔드포인트 |
| `domain/review/service/ReviewService.java` | 비즈니스 로직 + 점수 계산 |
| `domain/review/dto/request/ReviewCreateRequest.java` | 작성 요청 DTO |
| `domain/review/dto/request/ReviewUpdateRequest.java` | 수정 요청 DTO |
| `domain/review/dto/response/ReviewDetailResponse.java` | 단일 리뷰 응답 |
| `domain/review/dto/response/ReviewSummaryResponse.java` | 집계 응답 |
| `domain/review/dto/response/TagResponse.java` | 태그 응답 |
| `domain/review/dto/response/TagCountResponse.java` | 집계용 태그+count 응답 |

### 수정
| 파일 | 내용 |
|------|------|
| `domain/review/repository/ReviewRepository.java` | 집계/조회 쿼리 추가 |
| `domain/review/repository/ReviewTagRepository.java` | 태그별 count, 삭제 쿼리 추가 |

---

## 커밋 단위

1. `feat(review): 태그 전체 조회 API`
2. `feat(review): 리뷰 작성 API`
3. `feat(review): 내가 쓴 리뷰 단일 조회 API`
4. `feat(review): 리뷰 수정/삭제 API`
5. `feat(review): 내 집계 리뷰 조회 API (베이지안 점수 계산 포함)`
6. `feat(review): 남의 집계 리뷰 조회 API (명함 보유 권한 확인)`

---

## 검증 방법

- 리뷰 작성 후 `GET /api/reviews?revieweeId={id}` → 작성 내용 일치 확인
- 동일 대상 재작성 시 409 반환 확인
- 명함 미보유 상태에서 `GET /api/reviews/users/{id}` → 403 반환 확인
- soft delete 후 집계에서 제외 확인 (review_count 감소)
- 리뷰 0건일 때 calculated_score = 30.0 확인
