function findId() {

    const name = document.getElementById("name").value.trim();
    const emailId = document.getElementById("emailId").value.trim();
    const emailDomain = document.getElementById("emailDomain").value.trim();

    const resultEl = document.getElementById("result");

    if (!name || !emailId || !emailDomain) {
        resultEl.innerHTML = `<span class="error">모든 정보를 입력해주세요.</span>`;
        return;
    }

    fetch("/api/find/id", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            name: name,
            emailId: emailId,
            emailDomain: emailDomain
        })
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === "ok") {
                resultEl.innerHTML =
                    `<span class="highlight">가입된 아이디: ${data.id}</span>`;
            } else {
                resultEl.innerHTML =
                    `<span class="error">${data.message || "일치하는 회원이 없습니다."}</span>`;
            }
        })
        .catch(() => {
            resultEl.innerHTML =
                `<span class="error">서버 오류가 발생했습니다.</span>`;
        });
}
