document.addEventListener("DOMContentLoaded", () => {
    // ---------------------------------------------------------
    // 1. 요소 선택
    // ---------------------------------------------------------
    const steps = document.querySelectorAll(".step");
    const dots = document.querySelectorAll(".step-dot");

    const btnNext1 = document.getElementById("next1");
    const btnPrev2 = document.getElementById("prev2");
    const btnSubmit = document.getElementById("next2"); // '생성하기' 버튼
    const btnGoList = document.getElementById("goListBtn"); // '목록으로' 버튼

    const confirmChk = document.getElementById("confirmChk");

    // ---------------------------------------------------------
    // 2. 단계 이동 함수
    // ---------------------------------------------------------
    const showStep = (idx) => {
        steps.forEach((s, i) => {
            s.classList.toggle("active", i === idx);
            if(dots[i]) dots[i].classList.toggle("active", i <= idx);
        });
    };

    // [STEP 1 -> STEP 2] 이동
    btnNext1.addEventListener("click", () => {
        // 간단 유효성 검사
        const name = document.getElementById("petName").value.trim();
        const start = document.getElementById("dateStart").value;
        const end = document.getElementById("dateEnd").value;
        const msg = document.getElementById("message").value.trim();

        if (!name || !start || !end || !msg) {
            alert("모든 정보를 입력해주세요.");
            return;
        }

        renderSummary(); // 요약 정보 업데이트
        showStep(1);     // 다음 단계로
    });

    // [STEP 2 -> STEP 1] 이동
    btnPrev2.addEventListener("click", () => showStep(0));

    // 체크박스 동의 시 [생성하기] 버튼 활성화
    confirmChk.addEventListener("change", (e) => {
        btnSubmit.disabled = !e.target.checked;
    });

    // ---------------------------------------------------------
    // 3. 요약 정보 렌더링 (STEP 2 화면)
    // ---------------------------------------------------------
    function renderSummary() {
        const name = document.getElementById("petName").value;
        const start = document.getElementById("dateStart").value;
        const end = document.getElementById("dateEnd").value;
        const msg = document.getElementById("message").value;

        const fileInput = document.getElementById("petPhoto");
        const hasFile = fileInput.files.length > 0;

        document.getElementById("summary").innerHTML = `
            <div style="text-align: left; display: inline-block; line-height: 1.6;">
                <p><strong>이름:</strong> ${name}</p>
                <p><strong>함께한 날:</strong> ${start} ~ ${end}</p>
                <p><strong>한마디:</strong> "${msg}"</p>
                <p class="muted" style="font-size:0.9em; margin-top:5px; color:#666;">
                    ${hasFile ? "※ 사진 파일이 선택되었습니다." : "※ 사진 파일이 없습니다 (기본 이미지 사용)."}
                </p>
            </div>
        `;
    }

    // ---------------------------------------------------------
    // 4. 서버 전송 (파일 업로드 포함)
    // ---------------------------------------------------------
    btnSubmit.addEventListener("click", async () => {
        // 중복 클릭 방지
        btnSubmit.disabled = true;
        btnSubmit.innerText = "저장 중...";

        const formData = new FormData();
        formData.append("petName", document.getElementById("petName").value);
        formData.append("dateStart", document.getElementById("dateStart").value);
        formData.append("dateEnd", document.getElementById("dateEnd").value);
        formData.append("message", document.getElementById("message").value);

        const slotIdx = document.getElementById("slotIndex").value;
        formData.append("slotIndex", slotIdx);

        // 파일이 있으면 추가
        const fileInput = document.getElementById("petPhoto");
        if(fileInput.files[0]) {
            formData.append("petPhoto", fileInput.files[0]);
        }

        try {
            // [중요] ReserveController의 @PostMapping("/save4") 경로로 전송
            const response = await fetch("/reserve/save4", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                // 성공 시 STEP 3(완료 화면)으로 이동
                showStep(2);
            } else {
                const errorText = await response.text();
                if (response.status === 401) {
                    alert("로그인이 필요합니다.");
                    window.location.href = "/signin"; // 로그인 페이지로 이동
                } else {
                    alert("저장에 실패했습니다: " + errorText);
                    // 실패 시 버튼 복구
                    btnSubmit.disabled = false;
                    btnSubmit.innerText = "생성하기";
                }
            }
        } catch (err) {
            console.error(err);
            alert("서버 통신 중 오류가 발생했습니다.");
            btnSubmit.disabled = false;
            btnSubmit.innerText = "생성하기";
        }
    });

    // ---------------------------------------------------------
    // 5. 목록으로 가기 (완료 후)
    // ---------------------------------------------------------
    btnGoList.addEventListener("click", () => {
        // [중요] ReserveController의 @GetMapping("/ourpage/main") 경로로 이동
        // URL 매핑: /reserve/ourpage/main
        window.location.href = "/reserve/ourpage/main";
    });
});