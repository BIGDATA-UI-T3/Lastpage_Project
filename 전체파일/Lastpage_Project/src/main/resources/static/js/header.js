document.addEventListener("DOMContentLoaded", async () => {
  const headerContainer = document.querySelector("#header-container");
  let loginLink, signupLink;

  /** ------------------------------
   *  헤더를 서버에서 fetch해서 렌더링
   * ------------------------------ */
  async function loadHeader() {
    try {
      const res = await fetch("/html/header.html", {
        credentials: "same-origin",
        cache: "no-store"
      });

      if (!res.ok) throw new Error("헤더 파일 로드 실패");

      const html = await res.text();
      headerContainer.innerHTML = html;

      // 로그인 상태 반영
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

      /** ------------------------------
       *  햄버거 메뉴 슬라이드 토글
       * ------------------------------ */
      const hamburger = headerContainer.querySelector("#hamburger");
      const sideMenu = headerContainer.querySelector("#sideMenu");
      if (hamburger && sideMenu) {
        hamburger.addEventListener("click", () => {
          sideMenu.classList.toggle("active");
          document.body.classList.toggle("side-open");
        });
      }

    } catch (err) {
      console.error("헤더 로딩 중 오류:", err);
    }
  }

  /** ------------------------------
   * 초기 실행
   * ------------------------------ */
  if (headerContainer) {
    await loadHeader();
  }

  /** ------------------------------
   * 회원정보 수정 후 이름 갱신
   * ------------------------------ */
  const updatedName = localStorage.getItem("updatedUserName");
  if (updatedName && loginLink) {
    loginLink.textContent = `${updatedName}님 반갑습니다.`;
    localStorage.removeItem("updatedUserName");
  }

  /** ------------------------------
   * 로그인 성공 시 헤더 갱신
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
   * 외부에서 헤더 새로고침 요청
   * ------------------------------ */
  window.addEventListener("lp:update-header", async () => {
    await loadHeader();
  });

  /** ------------------------------
   * 로그인 환영 메시지
   * ------------------------------ */
  const params = new URLSearchParams(window.location.search);
  const welcomeName = params.get("welcome");
  if (welcomeName) {
    setTimeout(() => {
      alert(`${welcomeName}님 환영합니다!`);
    }, 300);
  }
});
