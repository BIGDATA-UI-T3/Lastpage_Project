document.addEventListener("DOMContentLoaded", async () => {
  const footerContainer = document.querySelector("#footer-container");
  if (!footerContainer) return;

  try {
    const res = await fetch("/html/footer.html", {
      credentials: "same-origin", // 세션 유지
      cache: "no-store" // 최신화
    });

    if (!res.ok) throw new Error("푸터 로드 실패");
    const html = await res.text();
    footerContainer.innerHTML = html;
  } catch (err) {
    console.error("푸터 로딩 중 오류:", err);
  }
});
