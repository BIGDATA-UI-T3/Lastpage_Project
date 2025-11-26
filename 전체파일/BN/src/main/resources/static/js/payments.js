document.addEventListener("DOMContentLoaded", () => {

    const kakaoBtn = document.getElementById("kakaoPayBtn");
    const naverBtn = document.getElementById("naverPayBtn");
    const confirmCheck = document.getElementById("confirmCheck");

    const reserveIdInput = document.getElementById("reserveId");
    const amountInput = document.getElementById("amount");

    if (!kakaoBtn || !naverBtn) {
        console.error("[ERROR] 결제 버튼 요소를 찾을 수 없습니다.");
        return;
    }
    if (!reserveIdInput || !amountInput) {
        console.error("[ERROR] 결제 정보(reserveId / amount)가 없습니다.");
        return;
    }

    const reserveId = reserveIdInput.value;
    const amount = Number(amountInput.value);

    // 주문번호 생성
    const orderId = "ORDER_" + Date.now() + "_" + crypto.randomUUID();
    console.log("[생성된 orderId] ", orderId);

    // =====================================
    // 결제 실행 함수
    // =====================================
    async function requestPayment(method) {
        try {
            if (!confirmCheck.checked) {
                alert("결제 내용을 확인해주세요.");
                return;
            }

            kakaoBtn.disabled = true;
            naverBtn.disabled = true;

            console.log(`[결제 요청] method=${method}, orderId=${orderId}, reserveId=${reserveId}`);

            const dto = {
                method: method,        // "KAKAO" 또는 "NAVER"
                orderId: orderId,
                reserveId: Number(reserveId),
                amount: amount
            };

            const res = await fetch("/api/payments/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const err = await res.text();
                alert("결제 요청 중 오류 발생: " + err);
                return;
            }

            const data = await res.json();
            console.log("[PG 응답]", data);

            // =============================
            // 카카오페이 결제창 이동
            // =============================
            if (method === "KAKAO") {
                if (data.next_redirect_pc_url) {
                    window.location.href = data.next_redirect_pc_url;
                } else {
                    alert("카카오페이 결제창 연결 실패");
                }
            }

            // =============================
            // 네이버페이 결제창 이동
            // =============================
            if (method === "NAVER") {
                if (data.redirect_url) {
                    window.location.href = data.redirect_url;
                } else {
                    alert("네이버페이 결제창 연결 실패");
                }
            }

        } catch (e) {
            console.error("[결제 오류]", e);
            alert("결제 요청 중 오류가 발생했습니다.");
        } finally {
            kakaoBtn.disabled = false;
            naverBtn.disabled = false;
        }
    }

    // 버튼 이벤트
    kakaoBtn.addEventListener("click", () => requestPayment("KAKAO"));
    naverBtn.addEventListener("click", () => requestPayment("NAVER"));

});
