package com.nihongo.servlet;

// Jackson 라이브러리: JSON 파싱·생성에 사용
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// 우리가 만든 DB 연결 클래스
import com.nihongo.db.DBConnection;

// BCrypt: 비밀번호 해시 검증 라이브러리
import org.mindrot.jbcrypt.BCrypt;

// 서블릿 관련 표준 클래스들
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // 세션: 로그인 상태를 서버에 저장하는 객체

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginServlet: 로그인 요청을 처리하는 서블릿
 *
 * @WebServlet("/api/login")
 *   → React에서 POST http://localhost:8080/api/login 으로 요청하면
 *     이 클래스의 doPost() 메서드가 자동으로 실행됨
 *
 * 처리 흐름:
 *   React 로그인 폼 제출
 *     → fetch POST /api/login (JSON 본문: username, password)
 *     → doPost() 실행
 *     → DB에서 username으로 사용자 조회 (SELECT)
 *     → BCrypt.checkpw()로 비밀번호 일치 확인
 *     → 성공: 세션에 사용자 정보 저장 + 200 응답
 *     → 실패: 401 응답 (아이디 없음 or 비밀번호 불일치)
 *
 * BCrypt 비밀번호 검증 방식:
 *   DB에는 암호화된 해시값만 저장되어 있음 (원본 비밀번호 없음)
 *   → 입력한 비밀번호와 저장된 해시를 직접 비교하는 게 아니라
 *     BCrypt.checkpw(입력한 비밀번호, DB의 해시값) 로 일치 여부만 확인
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    /*
      ObjectMapper: JSON ↔ Java 객체 변환기 (Jackson)
      한 번만 생성해서 재사용 — ObjectMapper는 스레드 안전(thread-safe)하므로 괜찮음
    */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * doOptions: CORS Preflight 요청 처리
     *
     * 브라우저는 POST 요청 전에 OPTIONS 요청을 먼저 보내
     * 서버가 이 요청을 허용하는지 확인함 (CORS 사전 확인)
     * 이 메서드에서 200 OK + 허용 헤더를 응답해야 실제 POST 요청이 진행됨
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
    }

    /**
     * doPost: 실제 로그인 처리 메서드
     * HTTP POST /api/login 요청이 오면 자동으로 이 메서드가 호출됨
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // 모든 응답에 CORS 헤더 추가
        setCorsHeaders(req, resp);

        // 응답 형식: JSON, 인코딩: UTF-8
        resp.setContentType("application/json; charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        try {
            /*
              1단계: 요청 본문(body) JSON 파싱
              React의 fetch()에서 body: JSON.stringify({username, password}) 로 보낸 데이터를 읽음
            */
            JsonNode body = mapper.readTree(req.getInputStream());

            String username = body.path("username").asText().trim();
            String password = body.path("password").asText();
            // 비밀번호는 trim() 하지 않음 — 앞뒤 공백도 비밀번호의 일부

            // 2단계: 기본 유효성 검사 — 빈 값이면 바로 거절
            if (username.isEmpty() || password.isEmpty()) {
                sendError(resp, out, 400, "아이디와 비밀번호를 모두 입력해주세요.");
                return;
            }

            /*
              3단계: DB에서 username으로 사용자 조회
              try-with-resources: conn, ps, rs 모두 블록이 끝나면 자동으로 close()됨
            */
            try (Connection conn = DBConnection.getConnection()) {

                /*
                  username으로 사용자를 찾아서 user_id, username, password_hash 조회
                  비밀번호는 DB에서 비교하지 않고 Java 코드에서 BCrypt로 비교함
                  이유: BCrypt는 같은 비밀번호라도 솔트가 달라 SQL WHERE로 직접 비교 불가
                */
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT user_id, username, nickname, password_hash FROM users WHERE username = ?")) {

                    ps.setString(1, username);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            sendError(resp, out, 401, "아이디 또는 비밀번호가 올바르지 않습니다.");
                            return;
                        }

                        int    userId       = rs.getInt("user_id");
                        String dbUsername   = rs.getString("username");
                        String dbNickname   = rs.getString("nickname"); /* NULL 가능 — 구버전 계정 */
                        String passwordHash = rs.getString("password_hash");

                        if (!BCrypt.checkpw(password, passwordHash)) {
                            sendError(resp, out, 401, "아이디 또는 비밀번호가 올바르지 않습니다.");
                            return;
                        }

                        /* 로그인 성공 — 세션 생성 */
                        HttpSession session = req.getSession(true);
                        session.setAttribute("userId",   userId);
                        session.setAttribute("username", dbUsername);
                        session.setMaxInactiveInterval(60 * 60);

                        /* 성공 응답: username + nickname 포함 */
                        resp.setStatus(HttpServletResponse.SC_OK);
                        Map<String, String> result = new HashMap<>();
                        result.put("message",  "로그인 성공");
                        result.put("username", dbUsername);
                        result.put("nickname", dbNickname != null ? dbNickname : dbUsername);
                        out.print(mapper.writeValueAsString(result));
                        return;
                    }
                }
            }

        } catch (Exception e) {
            // 예상치 못한 서버 오류 (DB 연결 실패 등)
            e.printStackTrace(); // 서버 콘솔에 오류 스택 출력
            sendError(resp, out, 500, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * setCorsHeaders: CORS 허용 헤더를 응답에 추가하는 공통 메서드
     *
     * Vite 개발 서버는 포트 3000이 사용 중이면 3001, 3002... 로 자동 변경됨
     * 포트를 고정하지 않고 요청의 Origin 헤더를 읽어서 localhost면 그대로 허용
     *
     * @param req  — Origin 헤더를 읽기 위한 요청 객체
     * @param resp — CORS 헤더를 추가할 응답 객체
     */
    private void setCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        // localhost 에서 온 요청이면 해당 Origin 그대로 허용
        if (origin != null && origin.startsWith("http://localhost")) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }
        resp.setHeader("Access-Control-Allow-Methods",     "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers",     "Content-Type");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    /**
     * sendError: 오류 JSON 응답을 보내는 공통 메서드
     *
     * @param resp    — HTTP 응답 객체
     * @param out     — 응답 본문 출력 스트림
     * @param status  — HTTP 상태 코드 (400: 잘못된 요청, 401: 인증 실패, 500: 서버 오류)
     * @param message — React 화면에 표시할 오류 메시지
     */
    private void sendError(HttpServletResponse resp, PrintWriter out, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        out.print(mapper.writeValueAsString(error));
    }
}
