AOS.init({
      once: false, // 스크롤할 때마다 애니메이션 반복
      duration: 800, // 애니메이션 지속 시간
      easing: 'ease-out-cubic', // 부드러운 효과
      threshold: 0.1 // 요소가 10% 보였을 때 애니메이션 시작
    });