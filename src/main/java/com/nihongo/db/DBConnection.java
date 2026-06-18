package com.nihongo.db;

// Java에서 DB와 통신할 때 쓰는 표준 인터페이스들
import java.sql.Connection;       // DB 연결 객체
import java.sql.DriverManager;    // JDBC 드라이버를 통해 연결을 만들어주는 클래스
import java.sql.SQLException;     // DB 관련 오류를 나타내는 예외 클래스

/**
 * DBConnection: MySQL 데이터베이스 연결을 관리하는 유틸리티 클래스
 *
 * 모든 서블릿(SignupServlet, LoginServlet 등)에서
 * DBConnection.getConnection()을 호출해 DB 연결 객체를 얻음
 *
 * 사용 예시 (서블릿 내부):
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // DB 작업
 *   }
 *   // try-with-resources 문법: 블록이 끝나면 conn.close() 자동 호출
 */
public class DBConnection {

    /*
      JDBC URL 형식:
      jdbc:mysql://[호스트]:[포트]/[데이터베이스명]?[옵션1]&[옵션2]...

      - localhost:3306  : MySQL이 내 컴퓨터(로컬)의 3306 포트에서 실행 중
      - nihongo_db     : 01_schema.sql에서 만든 데이터베이스 이름
      - useSSL=false    : 로컬 개발 환경이므로 SSL 암호화 사용 안 함
      - serverTimezone  : MySQL 서버의 시간대를 서울로 설정 (시간 관련 오류 방지)
      - characterEncoding: 한글·일본어가 깨지지 않도록 UTF-8 인코딩 강제 적용
    */
    // 환경변수에서 DB 접속 정보를 읽음 (없으면 로컬 개발용 기본값 사용)
    private static final String HOST     = getEnv("MYSQLHOST",     "localhost");
    private static final String PORT     = getEnv("MYSQLPORT",     "3306");
    private static final String DATABASE = getEnv("MYSQLDATABASE", "neko_nihongo");
    private static final String USER     = getEnv("MYSQLUSER",     "root");
    private static final String PASSWORD = getEnv("MYSQLPASSWORD", "hyo103525");

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
            "?useSSL=false" +
            "&serverTimezone=Asia/Seoul" +
            "&characterEncoding=UTF-8";

    // 환경변수를 읽고 없으면 기본값을 반환하는 헬퍼 메서드
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * getConnection(): DB 연결 객체(Connection)를 반환하는 정적(static) 메서드
     *
     * static이므로 객체 생성 없이 DBConnection.getConnection()으로 바로 호출 가능
     *
     * @return Connection  — DB와 통신할 수 있는 연결 객체
     *                       사용 후 반드시 close() 해야 메모리 누수가 없음
     *                       (try-with-resources 사용 권장)
     * @throws SQLException — DB 연결에 실패하면 예외 발생 (비밀번호 오류, DB 꺼짐 등)
     */
    public static Connection getConnection() throws SQLException {
        /* Tomcat 환경에서 JDBC 드라이버를 명시적으로 로드 (모듈 클래스로더 문제 방지) */
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
