package com.nihongo.filter;

import jakarta.servlet.*;          // 서블릿 필터 관련 인터페이스
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * EncodingFilter: 모든 HTTP 요청·응답에 UTF-8 인코딩을 적용하는 필터
 *
 * 필터(Filter)란?
 *   서블릿이 요청을 처리하기 전·후에 공통 작업을 수행하는 구성 요소
 *   여기서는 한글·일본어가 깨지지 않도록 인코딩을 UTF-8로 강제 설정함
 *
 * web.xml에서 /* (모든 URL)에 매핑했으므로
 * 모든 요청이 서블릿에 도달하기 전에 이 필터를 먼저 거침
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    /**
     * doFilter: 실제 필터 로직
     * 요청이 들어올 때마다 실행됨
     *
     * @param request  — 들어온 HTTP 요청 객체
     * @param response — 내보낼 HTTP 응답 객체
     * @param chain    — 다음 필터 또는 서블릿으로 요청을 넘기는 객체
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 요청 인코딩: 클라이언트(React)에서 보낸 데이터를 UTF-8로 읽음
        request.setCharacterEncoding("UTF-8");

        // 응답 인코딩: 서버에서 클라이언트로 보내는 데이터를 UTF-8로 전송
        response.setCharacterEncoding("UTF-8");

        // CORS 헤더: Firebase 등 외부 도메인에서의 요청을 허용
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // OPTIONS 프리플라이트 요청은 바로 200 응답
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        /*
          chain.doFilter(): 이 줄이 있어야 다음 서블릿(SignupServlet 등)으로 요청이 전달됨
          이 줄 없으면 요청이 여기서 멈춰버림
        */
        chain.doFilter(request, response);
    }

    // init(), destroy(): 필터 초기화·종료 시 실행
    // 여기서는 특별한 초기화·정리 작업이 없으므로 기본 구현(빈 메서드) 사용
    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
