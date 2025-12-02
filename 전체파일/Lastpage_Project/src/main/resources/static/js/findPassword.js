document.getElementById("findPwBtn").addEventListener("click", async () => {

    const id = document.getElementById("userId").value.trim();
    const emailId = document.getElementById("emailId").value.trim();
    const emailDomain = document.getElementById("emailDomain").value.trim();

    if (!id || !emailId || !emailDomain) {
        alert("아이디와 이메일을 모두 입력해주세요.");
        return;
    }

    try {
        const res = await fetch("/api/find/password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id: id,
                emailId: emailId,
                emailDomain: emailDomain
            })
        });

        const data = await res.json();

        if (data.status === "ok") {
            // 성공 UI 표시
            document.getElementById("successBox").style.display = "flex";
        } else {
            alert(data.message || "일치하는 회원 정보를 찾을 수 없습니다.");
        }

    } catch (err) {
        console.error(err);
        alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
});
