document.addEventListener("DOMContentLoaded", () => {

    const kakaoBtn = document.getElementById("kakaoPayBtn");
    const naverBtn = document.getElementById("naverPayBtn");
    const confirmCheck = document.getElementById("confirmCheck");
    const amountInput = document.getElementById("paymentAmount");

    if (!kakaoBtn || !naverBtn) {
        console.error("[ERROR] 결제 버튼 요소를 찾을 수 없습니다.");
        return;
    }
    if (!amountInput) {
        console.error("[ERROR] 결제 금액이 존재하지 않습니다.");
        return;
    }

    //  클라이언트 전용 UUID 주문번호 생성
    const orderId = "ORDER_" + Date.now() + "_" + crypto.randomUUID();
    const amount = Number(amountInput.value);

    console.log("[생성된 OrderId] ", orderId);

    // ---------------------------
    // 공통 결제 함수
    // ---------------------------
    async function requestPayment(method) {
        try {
            if (!confirmCheck.checked) {
                alert("결제 내용을 확인해 주세요.");
                return;
            }

            kakaoBtn.disabled = true;
            naverBtn.disabled = true;

            console.log("[결제 요청] method=", method, "orderId=", orderId);

            const res = await fetch("/api/payments/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ method, amount, orderId })
            });

            if (!res.ok) {
                alert(await res.text());
                return;
            }

            const data = await res.json();

            // 카카오페이
            if (method === "KAKAO") {
                if (data.next_redirect_pc_url) {
                    window.location.href = data.next_redirect_pc_url;
                } else {
                    alert("카카오페이 결제창 연결 실패");
                }
            }

            // 네이버페이
            if (method === "NAVER") {
                if (data.redirect_url) {
                    window.location.href = data.redirect_url;
                } else {
                    alert("네이버페이 결제창 연결 실패");
                }
            }

        } catch (err) {
            console.error("[결제 오류]", err);
            alert("결제 요청 중 오류가 발생했습니다.");
        } finally {
            kakaoBtn.disabled = false;
            naverBtn.disabled = false;
        }
    }

    // 이벤트 연결
    kakaoBtn.addEventListener("click", () => requestPayment("KAKAO"));
    naverBtn.addEventListener("click", () => requestPayment("NAVER"));
});
