package com.nihongo.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihongo.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * SignupServlet — 회원가입 처리
 *
 * 요청 형식: POST /api/signup
 * Body JSON: { username, nickname, email, password }
 *
 * 처리 순서:
 *   1. 필드 유효성 검사
 *   2. 아이디·이메일 중복 확인
 *   3. 비밀번호 BCrypt 해시
 *   4. DB INSERT
 *   5. 자동 로그인 (세션 생성)
 *   6. 성공 응답: { message, username, nickname }
 */
@WebServlet("/api/signup")
public class SignupServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(req, resp);
        resp.setContentType("application/json; charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            JsonNode body = mapper.readTree(req.getInputStream());

            String username = body.path("username").asText().trim();
            String nickname = body.path("nickname").asText().trim();
            String email    = body.path("email").asText().trim();
            String password = body.path("password").asText();

            /* 필드 유효성 검사 */
            if (username.isEmpty() || nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                sendError(resp, out, 400, "모든 필드를 입력해주세요.");
                return;
            }
            if (!username.matches("[a-zA-Z][a-zA-Z0-9_]{1,11}")) {
                sendError(resp, out, 400, "아이디는 영문으로 시작, 2~12자로 입력해주세요.");
                return;
            }
            if (password.length() < 6) {
                sendError(resp, out, 400, "비밀번호는 6자 이상이어야 합니다.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {

                /* 아이디 중복 확인 */
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE username = ?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            sendError(resp, out, 409, "이미 사용 중인 아이디입니다.");
                            return;
                        }
                    }
                }

                /* 이메일 중복 확인 */
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE email = ?")) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            sendError(resp, out, 409, "이미 사용 중인 이메일입니다.");
                            return;
                        }
                    }
                }

                /* 비밀번호 해시 + INSERT */
                String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
                int newUserId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (username, nickname, email, password_hash) VALUES (?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, nickname);
                    ps.setString(3, email);
                    ps.setString(4, passwordHash);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newUserId = keys.getInt(1);
                    }
                }

                /* 자동 로그인 — 가입 직후 세션 생성 */
                HttpSession session = req.getSession(true);
                session.setAttribute("userId",   newUserId);
                session.setAttribute("username", username);
                session.setMaxInactiveInterval(60 * 60);
            }

            /* 성공 응답: username + nickname 반환 */
            resp.setStatus(HttpServletResponse.SC_CREATED); // 201
            Map<String, String> result = new HashMap<>();
            result.put("message",  "회원가입이 완료됐어요!");
            result.put("username", username);
            result.put("nickname", nickname);
            out.print(mapper.writeValueAsString(result));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, out, 500, "서버 오류가 발생했습니다.");
        }
    }

    private void setCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && (origin.startsWith("http://localhost")
                || origin.endsWith(".web.app")
                || origin.endsWith(".firebaseapp.com"))) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }
        resp.setHeader("Access-Control-Allow-Methods",     "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers",     "Content-Type");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    private void sendError(HttpServletResponse resp, PrintWriter out, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        out.print(mapper.writeValueAsString(error));
    }
}
