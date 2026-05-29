# Neko-Nihongo
# 🇯🇵 일본어 N3 학습 사이트 — DB 구축 가이드

> **프로젝트 스택:** React + Java Servlet + Tomcat + MySQL

---

## 📁 파일 구성

| 파일 | 설명 |
|------|------|
| `01_schema.sql` | MySQL 테이블 스키마 (전체 DB 구조) |
| `02_words_data.sql` | 단어 1,770개 INSERT SQL (바로 import 가능) |
| `02_excel_to_sql.py` | 엑셀 → SQL 재변환 스크립트 (수정 필요 시) |
| `03_sentences_data.sql` | N3 예시 문장 100개 + 퀴즈 30개 INSERT SQL |
| `japanese_n3_db_setup.ipynb` | 구글 코랩 노트북 (SQL 생성 자동화) |
| `VocabList_N3.xlsx` | 원본 단어 엑셀 파일 |

---

## 🗄️ DB 구조 (ERD 요약)

```
users
 └─ word_learning       (단어 학습 현황 + 복습 목록)
 └─ word_quiz_results   (OX / 타이핑 퀴즈 결과)
 └─ sentence_results    (문장 퀴즈 결과)

words ──── word_learning
      └─── word_quiz_results
      └─── sentences (source_word_id)

sentences ──── sentence_quizzes
          └─── sentence_results
```

### 테이블 설명

| 테이블 | 역할 |
|--------|------|
| `words` | N3 단어 1,770개 (한자 / 히라가나 / 뜻 / 한자설명) |
| `users` | 사용자 계정 |
| `word_learning` | 학습한 단어 자동 저장 + 복습 횟수 추적 |
| `word_quiz_results` | OX퀴즈 / 타이핑퀴즈 결과 저장 |
| `sentences` | N3 예시 문장 100개 |
| `sentence_quizzes` | 문장 퀴즈 3종 (단어배열 / 발음맞추기 / 한자읽기) |
| `sentence_results` | 문장 퀴즈 결과 + 오답 저장 |

---

## 🚀 MySQL import 순서

> MySQL Workbench 또는 터미널에서 아래 순서대로 실행하세요.

### 1단계 — 스키마 생성 (테이블 만들기)
```bash
mysql -u root -p < 01_schema.sql
```

### 2단계 — 단어 데이터 import
```bash
mysql -u root -p japanese_n3 < 02_words_data.sql
```

### 3단계 — 예시 문장 + 퀴즈 import
```bash
mysql -u root -p japanese_n3 < 03_sentences_data.sql
```

> ✅ 3단계까지 완료하면 DB 구축 끝!

---

## 🧪 구글 코랩 사용법 (엑셀 재변환 필요 시)

> 엑셀 파일을 수정했거나 SQL을 다시 생성하고 싶을 때 사용

1. [colab.research.google.com](https://colab.research.google.com) 접속
2. `japanese_n3_db_setup.ipynb` 업로드 후 열기
3. 왼쪽 파일 패널에서 `VocabList_N3.xlsx` 업로드
4. 셀 순서대로 실행
5. 생성된 SQL 파일 우클릭 → 다운로드

> ⚠️ 코랩에서 파일 업로드 시 경로는 `/content/sample_data/VocabList_N3.xlsx` 또는 `/content/VocabList_N3.xlsx` 확인 필요

---

## ⚙️ words 테이블 컬럼 설명

| 컬럼 | 타입 | 예시 |
|------|------|------|
| `word_id` | INT (PK) | 자동 증가 |
| `kanji` | VARCHAR(100) | `挨拶` (없으면 NULL) |
| `hiragana` | VARCHAR(100) | `あいさつ` |
| `meaning` | TEXT | `1.인사.\n2.서로 만나거나...` |
| `kanji_info1` | VARCHAR(100) | `挨 미칠 애` |
| `kanji_info2` | VARCHAR(100) | `拶 짓누를 찰` |
| `syllable_group` | CHAR(1) | `あ` |

## ⚙️ sentence_quizzes 테이블 컬럼 설명

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `quiz_type` | ENUM | `word_order` / `pronunciation` / `kanji_reading` |
| `question_data` | JSON | 문제 데이터 (단어 배열 또는 대상 단어 포함) |
| `answer` | VARCHAR(300) | 정답 문자열 |
| `underline_word` | VARCHAR(100) | 발음/한자 문제에서 밑줄 칠 단어 |

---

## 📊 데이터 현황

| 항목 | 수량 |
|------|------|
| 총 단어 수 | 1,770개 |
| 시트 구성 | あ ~ わ 43개 (오십음도 순) |
| 한자 있는 단어 | 1,557개 |
| 히라가나 전용 단어 | 213개 |
| 예시 문장 | 100개 |
| 단어배열 퀴즈 | 10개 |
| 발음맞추기 퀴즈 | 10개 |
| 한자읽기 퀴즈 | 10개 |

---

## 💡 React + Tomcat 연동 쿼리 예시

### 플래시카드 — 단어 불러오기
```java
// GET /api/words?group=あ&limit=10
// SELECT * FROM words WHERE syllable_group = ? ORDER BY RAND() LIMIT ?
```

### 단어 학습 완료 시 복습 목록 저장
```java
// POST /api/learning  { userId, wordId }
// INSERT INTO word_learning (user_id, word_id) VALUES (?, ?)
// ON DUPLICATE KEY UPDATE
//   review_count = review_count + 1,
//   last_reviewed = NOW()
```

### OX / 타이핑 퀴즈 결과 저장
```java
// POST /api/quiz/word  { userId, wordId, quizType, isCorrect, userAnswer }
// INSERT INTO word_quiz_results (user_id, word_id, quiz_type, is_correct, user_answer)
// VALUES (?, ?, ?, ?, ?)
```

### 오답 목록 조회 (재학습용)
```java
// GET /api/wrong-answers?userId=1
// SELECT DISTINCT w.* FROM word_quiz_results r
// JOIN words w ON r.word_id = w.word_id
// WHERE r.user_id = ? AND r.is_correct = 0
// ORDER BY r.answered_at DESC
```

### 문장 퀴즈 불러오기
```java
// GET /api/quiz/sentence?type=word_order&limit=5
// SELECT sq.*, s.japanese, s.korean
// FROM sentence_quizzes sq
// JOIN sentences s ON sq.sentence_id = s.sentence_id
// WHERE sq.quiz_type = ?
// ORDER BY RAND() LIMIT ?
```

### 정답률 조회
```java
// GET /api/stats?userId=1
// SELECT
//   COUNT(*) AS total,
//   SUM(is_correct) AS correct,
//   ROUND(SUM(is_correct) / COUNT(*) * 100, 1) AS rate
// FROM word_quiz_results
// WHERE user_id = ?
```

---

## ⚠️ 주의사항

- 단어 뜻은 파파고로 번역된 항목이 일부 포함되어 있어 어색할 수 있으니 검수 권장
- `meaning` 컬럼은 `\n`으로 여러 뜻이 구분됨 → 프론트에서 `split('\n')` 처리 필요
- 예시 문장 퀴즈는 현재 30개이므로 필요 시 `03_sentences_data.sql`에 직접 추가 가능
- `sentence_quizzes`의 `question_data`는 JSON 타입 → Java에서 `org.json` 또는 `Gson` 라이브러리로 파싱
