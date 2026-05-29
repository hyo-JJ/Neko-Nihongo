# Neko-Nihongo
# 🇯🇵 일본어 N3 학습 사이트 — DB 구축 가이드

> **프로젝트 스택:** React + Java Servlet + Tomcat + MySQL

---

## 📁 파일 구성

| 파일 | 설명 |
|------|------|
| `01_schema.sql` | MySQL 테이블 스키마 (전체 DB 구조) |
| `02_words_data.sql` | 단어 1,770개 INSERT SQL (바로 import 가능) |
| `03_sentences_data.sql` | N3 예시 문장 100개 + 퀴즈 30개 INSERT SQL |
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
