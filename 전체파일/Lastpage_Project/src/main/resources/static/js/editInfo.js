
document.addEventListener("DOMContentLoaded", () => {

  /* =======================================================
       공통 요소
  ======================================================= */
  const form = document.getElementById("editForm");
  if (!form) return;

  const userType = form.dataset.usertype;   // native | social | admin
  const oauthEmail = form.dataset.oauthemail;
  const targetUserSeq = form.dataset.userseq; // admin 모드용

  const name = document.getElementById("name");
  const emailId = document.getElementById("emailId");
  const emailDomain = document.getElementById("emailDomain");
  const password = document.getElementById("password");
  const pwConfirm = document.getElementById("password-confirm");
  const deleteBtn = document.getElementById("deleteBtn");
  const saveBtn = document.getElementById("saveBtn");

  const pwBar = document.getElementById("pwStrengthBar");
  const pwText = document.getElementById("pwStrengthText");

  /* =======================================================
       1) 관리자 모드 처리
  ======================================================= */
  if (userType === "admin") {

    console.log("[ADMIN MODE] 관리자 권한으로 회원정보 수정 페이지 접근");

    [name, emailId, emailDomain].forEach(el => el.disabled = false);

    if (password) password.disabled = true;
    if (pwConfirm) pwConfirm.disabled = true;

    if (pwBar) pwBar.style.display = "none";
    if (pwText) pwText.style.display = "none";

    if (deleteBtn) deleteBtn.style.display = "none";

    /* 관리자 저장 */
    if (saveBtn) {
      saveBtn.addEventListener("click", async () => {

        const birthValue = document.getElementById("birth").value;
        let year = null, month = null, day = null;

        if (birthValue) {
          const date = new Date(birthValue);
          year = date.getFullYear();
          month = date.getMonth() + 1;
          day = date.getDate();
        }

        const updated = {
          name: name.value.trim(),
          emailId: emailId.value.trim(),
          emailDomain: emailDomain.value.trim(),
          phone_num: document.getElementById("phone").value.trim(),
          year,
          month,
          day,
          gender: document.getElementById("gender").value
        };

        // (1) 관리자용 업데이트
        const res = await fetch(`/admin/user/update/${targetUserSeq}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(updated)
        });

        if (!res.ok) {
          alert(await res.text());
          return;
        }

        alert("관리자가 회원 정보를 수정했습니다.");

        // -------------------------------------------
        // (2) 관리자 자신인지 확인 → 세션 리프레시
        // -------------------------------------------
        try {
          await fetch("/admin/user/session/refresh", { method: "POST" });

          // 헤더 새로고침 이벤트
          window.dispatchEvent(new CustomEvent("lp:update-header"));
        } catch (e) {
          console.error("관리자 세션 갱신 실패:", e);
        }

        // -------------------------------------------
        // (3) 관리자 목록으로 이동
        // -------------------------------------------
        window.location.href = "/admin/users";
      });
    }

    return; // 관리자 모드 종료
  }

  /* =======================================================
       2) 소셜 로그인 사용자
  ======================================================= */
  if (userType === "social") {
    if (oauthEmail) {
      const [idPart, domainPart] = oauthEmail.split("@");
      if (emailId) emailId.value = idPart;
      if (emailDomain) emailDomain.value = domainPart;
    }

    [name, emailId, emailDomain, password, pwConfirm].forEach(el => el.disabled = true);
  }

  /* =======================================================
       3) 자체 로그인 사용자 (일반)
  ======================================================= */
  if (userType === "native") {
    [name, emailId, emailDomain, password, pwConfirm].forEach(el => el.disabled = false);
  }

  /* =======================================================
       4) 비밀번호 강도 검사 + 재사용 검사
  ======================================================= */
  if (password) {
    password.addEventListener("input", async () => {
      const val = password.value;
      const result = checkStrength(val);

      if (pwBar) pwBar.style.width = result.score * 25 + "%";
      if (pwBar) pwBar.style.backgroundColor = result.color;
      if (pwText) {
        pwText.textContent = result.text;
        pwText.style.color = result.color;
      }

      if (val.length >= 8) {
        const reuseCheck = await fetch("/mypage/EditInfo/checkPassword", {
          method: "POST",
          headers: { "Content-Type": "text/plain" },
          body: val
        });

        if (reuseCheck.ok && await reuseCheck.text() === "reused") {
          pwText.textContent = "기존에 사용한 비밀번호는 사용할 수 없습니다.";
          pwText.style.color = "#ff4d4f";
          pwBar.style.backgroundColor = "#ff4d4f";
        }
      }
    });
  }

  if (pwConfirm) {
    pwConfirm.addEventListener("input", () => {
      pwConfirm.style.borderColor =
        pwConfirm.value && pwConfirm.value !== password.value ? "red" : "";
    });
  }

  function checkStrength(password) {
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    switch (score) {
      case 1: return { score, text: "보안 수준: 약함", color: "#ff4d4f" };
      case 2: return { score, text: "보안 수준: 보통", color: "#faad14" };
      case 3: return { score, text: "보안 수준: 좋음", color: "#52c41a" };
      case 4: return { score, text: "보안 수준: 매우 강함", color: "#389e0d" };
      default: return { score: 0, text: "비밀번호를 입력하세요", color: "#ccc" };
    }
  }

  /* =======================================================
       5) 일반 사용자 저장 처리
  ======================================================= */
  if (saveBtn) {
    saveBtn.addEventListener("click", async () => {

      const pwVal = password.value.trim();
      const pw2Val = pwConfirm.value.trim();
      if (pwVal && pwVal !== pw2Val) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }

      const birthValue = document.getElementById("birth").value;
      let year = null, month = null, day = null;

      if (birthValue) {
        const date = new Date(birthValue);
        year = date.getFullYear();
        month = date.getMonth() + 1;
        day = date.getDate();
      }

      const updated = {
        name: name.value.trim(),
        emailId: emailId.value.trim(),
        emailDomain: emailDomain.value.trim(),
        password: pwVal,
        phone_num: document.getElementById("phone").value.trim(),
        year,
        month,
        day,
        gender: document.getElementById("gender").value
      };

      const res = await fetch("/mypage/EditInfo/update", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updated)
      });

      if (res.ok) {
        alert("회원정보가 수정되었습니다.");
        const newName = name.value.trim();
        localStorage.setItem("updatedUserName", newName);
        window.location.href = "/mypage/Mypage";
      } else {
        alert(await res.text());
      }
    });
  }

  /* =======================================================
       6) 탈퇴 처리 (일반 사용자)
  ======================================================= */
  if (deleteBtn) {
    deleteBtn.addEventListener("click", async () => {
      try {
        const sessionRes = await fetch("/mypage/EditInfo/session/check", { cache: "no-store" });
        if (!sessionRes.ok) {
          alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
          window.location.href = "/signin";
          return;
        }

        const sessionData = await sessionRes.json();
        const userType = sessionData.userType;
        const userSeq = sessionData.userSeq;

        if (!confirm("정말 회원 탈퇴를 진행하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")) return;

        if (userType === "social") {
          const delRes = await fetch(`/mypage/EditInfo/delete/${userSeq}`, { method: "DELETE" });
          if (delRes.ok) {
            alert("회원 탈퇴가 완료되었습니다.");
            window.location.href = "/signin";
          }
          return;
        }

        const pw = prompt("비밀번호를 입력해주세요:");
        if (!pw) return;

        const verifyRes = await fetch(`/mypage/EditInfo/verify-password/${userSeq}`, {
          method: "POST",
          headers: { "Content-Type": "text/plain" },
          body: pw,
        });

        if (await verifyRes.text() !== "valid") {
          alert("비밀번호가 올바르지 않습니다.");
          return;
        }

        const delRes = await fetch(`/mypage/EditInfo/delete/${userSeq}`, { method: "DELETE" });
        if (delRes.ok) {
          alert("회원 탈퇴가 완료되었습니다.");
          window.location.href = "/signin";
        }

      } catch (err) {
        console.error("회원탈퇴 처리 중 오류:", err);
        alert("회원탈퇴 처리 중 오류가 발생했습니다.");
      }
    });
  }

});
