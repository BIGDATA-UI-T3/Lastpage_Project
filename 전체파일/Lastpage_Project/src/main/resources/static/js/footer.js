document.addEventListener("DOMContentLoaded", async () => {
  const footerContainer = document.querySelector("#footer-container");
  if (!footerContainer) return;

  try {
    const res = await fetch("/html/footer.html", {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!res.ok) throw new Error("푸터 로드 실패");

    // 불러온 HTML 삽입
    const html = await res.text();
    footerContainer.innerHTML = html;

    // 푸터가 로드된 후 개발자 모달 기능 초기화
    initDevModal();

  } catch (err) {
    console.error("푸터 로딩 중 오류:", err);
  }
});


// ================================
//  개발자 소개 모달 기능
// ================================
function initDevModal() {
  const modals = {
    bogeum: document.getElementById("devModal-bogeum"),
    jun: document.getElementById("devModal-jun"),
    yejin: document.getElementById("devModal-yejin")
  };

  document.querySelectorAll(".dev-link").forEach(link => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      const who = link.dataset.dev;

      if (modals[who]) {
        modals[who].classList.add("open");
      }
    });
  });

  // 닫기 버튼들 처리
  document.querySelectorAll(".devCloseBtn").forEach(btn => {
    btn.addEventListener("click", () => {
      const key = btn.dataset.close;
      modals[key]?.classList.remove("open");
    });
  });

  // 오버레이 클릭 시 닫기
  Object.values(modals).forEach(modal => {
    modal.addEventListener("click", (e) => {
      if (e.target.classList.contains("dev-overlay")) {
        modal.classList.remove("open");
      }
    });
  });
}

