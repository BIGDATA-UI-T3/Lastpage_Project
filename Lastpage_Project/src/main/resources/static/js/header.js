document.addEventListener("DOMContentLoaded", async () => {
  const headerContainer = document.querySelector("#header-container");
  let loginLink, signupLink;

  /** ------------------------------
   *  헤더를 서버에서 fetch해서 렌더링
   * ------------------------------ */
  async function loadHeader() {
    try {
      const res = await fetch("/html/header.html", {
        credentials: "same-origin", // 세션 유지
        cache: "no-store" // 항상 최신 헤더 가져오기
      });

      if (!res.ok) throw new Error("헤더 파일 로드 실패");

      const html = await res.text();
      headerContainer.innerHTML = html;

      //  로그인 상태 반영
      const authDiv = headerContainer.querySelector(".auth");
      if (authDiv) {
        const isLoggedIn = authDiv.dataset.login === "true";
        const username = authDiv.dataset.username;
        loginLink = authDiv.querySelector(".login-link");
        signupLink = authDiv.querySelector(".signup-link");

        if (isLoggedIn && loginLink && signupLink) {
          loginLink.textContent = `${username || "회원"}님 반갑습니다.`;
          signupLink.textContent = "로그아웃";
          loginLink.href = "/mypage/Mypage";
          signupLink.href = "/logout";
        }
      }

      //  햄버거 메뉴 토글
      const hamburger = headerContainer.querySelector("#hamburger");
      const sideMenu = headerContainer.querySelector("#sideMenu");
      if (hamburger && sideMenu) {
        hamburger.addEventListener("click", () => {
          sideMenu.classList.toggle("active");
        });
      }

    } catch (err) {
      console.error("헤더 로딩 중 오류:", err);
    }
  }

  /** ------------------------------
   *  초기 실행
   * ------------------------------ */
  if (headerContainer) {
    await loadHeader();
  }

  /** ------------------------------
   *  회원정보 수정 후 이름 갱신
   * ------------------------------ */
  const updatedName = localStorage.getItem("updatedUserName");
  if (updatedName && loginLink) {
    loginLink.textContent = `${updatedName}님 반갑습니다.`;
    localStorage.removeItem("updatedUserName");
  }

  /** ------------------------------
   *  로그인 성공 시 헤더 즉시 갱신 (페이지 새로고침 없이)
   *  - login.js에서 dispatchEvent("lp:login-success") 발생
   * ------------------------------ */
  window.addEventListener("lp:login-success", (e) => {
    const { name } = e.detail;
    if (loginLink && signupLink) {
      loginLink.textContent = `${name || "회원"}님 반갑습니다.`;
      signupLink.textContent = "로그아웃";
      loginLink.href = "/mypage/Mypage";
      signupLink.href = "/logout";
    }
  });

  /** ------------------------------
   *  외부에서 강제로 헤더 새로고침 요청 시
   *  - window.dispatchEvent(new Event("lp:update-header"))
   * ------------------------------ */
  window.addEventListener("lp:update-header", async () => {
    await loadHeader();
  });
});
