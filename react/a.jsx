import React from 'react';

// 0. 이미지 불러오기 (Assets 폴더에서 가져옵니다)
import niLogo from '../assets/ni.png';

// 1. 헤더 (상단 메뉴바)
const Header = () => (
  <header style={{ display: 'flex', justifyContent: 'space-between', padding: '20px', borderBottom: '1px solid #eaeaea' }}>
    <h1 style={{ margin: 0, fontSize: '24px', color: '#5D4037' }}> Neko Nihongo</h1>
    <nav style={{ alignSelf: 'center' }}>
      <span style={{ margin: '0 15px', cursor: 'pointer', fontWeight: 'bold' }}>홈</span>
      <span style={{ margin: '0 15px', cursor: 'pointer', color: '#666' }}>레슨</span>
      <span style={{ margin: '0 15px', cursor: 'pointer', color: '#666' }}>문법</span>
      <span style={{ margin: '0 15px', cursor: 'pointer', color: '#666' }}>단어</span>
    </nav>
    <div style={{ alignSelf: 'center' }}>
      <button style={{ marginRight: '10px', padding: '8px 16px', border: 'none', background: 'transparent', cursor: 'pointer' }}>로그인</button>
      <button style={{ padding: '8px 20px', border: 'none', backgroundColor: '#D4B89F', color: 'white', borderRadius: '20px', cursor: 'pointer', fontWeight: 'bold' }}>회원가입</button>
    </div>
  </header>
);

// 2. 메인 배너 (Hero 섹션: 왼쪽 글씨 - 중앙 이미지 - 오른쪽 카드)
const Hero = () => (
  <section style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '50px 40px', backgroundColor: '#FDFBF9', borderRadius: '20px', marginTop: '30px' }}>
    
    {/* 1. 왼쪽 구역: 인사말과 버튼 */}
    <div style={{ flex: 1 }}>
      <h2 style={{ fontSize: '42px', color: '#333', marginBottom: '15px' }}>일본어를, <br/>더욱 즐겁게.</h2>
      <p style={{ color: '#666', lineHeight: '1.6', fontSize: '16px' }}>초급자부터 상급자까지, 당신의 페이스로<br/>일본어를 학습할 수 있습니다.</p>
  
      <button style={{ marginTop: '25px', padding: '14px 28px', backgroundColor: '#C8A98B', color: 'white', border: 'none', borderRadius: '25px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}>
        무료로 시작하기 →
      </button>
    </div>

    {/* 2. 중앙 구역: 이미지 (원하시던 '여기' 위치!) */}
    <div style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>
      <img style={{ width: '350px', height: 'auto' }} src={niLogo} alt="니고 라보 로고 아이콘" /> 
    </div>
    
    {/* 3. 오른쪽 구역: 진도율 카드 */}
    <div style={{ width: '250px', padding: '25px', background: 'white', borderRadius: '20px', boxShadow: '0 10px 25px rgba(0,0,0,0.05)' }}>
      <p style={{ fontSize: '13px', color: '#888', margin: '0 0 10px 0' }}>나의 레벨</p>
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '20px' }}>
        <h3 style={{ margin: 0, fontSize: '24px', color: '#333' }}>초급</h3>
        <span style={{ fontSize: '12px', backgroundColor: '#FDECE8', color: '#E57373', padding: '4px 10px', borderRadius: '12px', marginLeft: '10px', fontWeight: 'bold' }}>레벨 2</span>
      </div>
      {/* 게이지 바 */}
      <div style={{ height: '8px', backgroundColor: '#F0F0F0', borderRadius: '4px', overflow: 'hidden' }}>
        <div style={{ width: '40%', height: '100%', backgroundColor: '#D4B89F', borderRadius: '4px' }}></div>
      </div>
      <p style={{ textAlign: 'right', fontSize: '14px', marginTop: '8px', color: '#333', fontWeight: 'bold' }}>40%</p>
    </div>
  </section>
);

// 3. 메인 컴포넌트 전체 조립
function A() {
  return (
    <div style={{ fontFamily: '"Noto Sans KR", sans-serif', maxWidth: '1100px', margin: '0 auto', padding: '20px' }}>
      
      <Header />
      <Hero />
      
      {/* 아래 하단 코스 선택 리스트 */}
      <div style={{ marginTop: '60px' }}>
        <h3 style={{ borderBottom: '3px solid #333', display: 'inline-block', paddingBottom: '8px', fontSize: '22px' }}>학습 코스</h3>
        
        <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
          
          <div style={{ flex: 1, padding: '35px 20px', border: '1px solid #F0F0F0', borderRadius: '15px', textAlign: 'center', boxShadow: '0 4px 15px rgba(0,0,0,0.02)' }}>
            <div style={{ fontSize: '45px', marginBottom: '15px' }}>📖</div>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>히라가나·가타카나</h4>
            <p style={{ fontSize: '13px', color: '#888' }}>기초부터 탄탄히 배우기</p>
          </div>
          
          <div style={{ flex: 1, padding: '35px 20px', border: '1px solid #F0F0F0', borderRadius: '15px', textAlign: 'center', boxShadow: '0 4px 15px rgba(0,0,0,0.02)' }}>
            <div style={{ fontSize: '45px', marginBottom: '15px' }}>💬</div>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>일상회화</h4>
            <p style={{ fontSize: '13px', color: '#888' }}>회화에서 쓰는 표현 배우기</p>
          </div>
          
          <div style={{ flex: 1, padding: '35px 20px', border: '1px solid #F0F0F0', borderRadius: '15px', textAlign: 'center', boxShadow: '0 4px 15px rgba(0,0,0,0.02)' }}>
            <div style={{ fontSize: '45px', marginBottom: '15px' }}>⛩️</div>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>일본문화</h4>
            <p style={{ fontSize: '13px', color: '#888' }}>일본의 문화와 습관 알기</p>
          </div>
          
        </div>
      </div>
    </div>
  );
}

export default A;