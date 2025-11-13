document.addEventListener("DOMContentLoaded", () => {


   /* ===== 결제 수단 선택 ===== */
   const methodButtons = document.querySelectorAll(".method-btn");
   const sections = document.querySelectorAll(".payment-section");

   methodButtons.forEach((btn) => {
     btn.addEventListener("click", () => {
       methodButtons.forEach((b) => b.classList.remove("active"));
       sections.forEach((sec) => sec.classList.remove("active"));
       btn.classList.add("active");
       document.getElementById(btn.dataset.target).classList.add("active");
     });
   });

   /* ===== 결제 버튼 클릭 ===== */
   document.getElementById("payBtn").addEventListener("click", async () => {
     try {
       const activeMethod = document.querySelector(".method-btn.active").dataset.target;
       const amount = 35000; // th:text 바인딩 or JS 변수로 전달 가능
       const orderId = "ORDER_" + Date.now();

       // 백엔드로 결제 요청
       const res = await fetch("/api/payments/create", {
         method: "POST",
         headers: { "Content-Type": "application/json" },
         body: JSON.stringify({
           method: activeMethod.toUpperCase(), // CARD / KAKAO / NAVER
           amount,
           orderId
         })
       });

       if (!res.ok) {
         alert(await res.text());
         return;
       }

       const data = await res.json();

       // 결제 수단별 후속 처리
       switch (activeMethod) {
         case "card":
           alert("카드 결제가 완료되었습니다.");
           window.location.href = "/pay/success";
           break;

         case "kakao":
           if (data.next_redirect_pc_url) {
             window.location.href = data.next_redirect_pc_url;
           } else {
             alert("카카오페이 결제창 연결 실패");
           }
           break;

         case "naver":
           if (data.redirect_url) {
             window.location.href = data.redirect_url;
           } else {
             alert("네이버페이 결제창 연결 실패");
           }
           break;

         default:
           alert("지원하지 않는 결제 방식입니다.");
       }
     } catch (err) {
       console.error("결제 중 오류:", err);
       alert("결제 요청 중 오류가 발생했습니다.");
     }
   });
 });