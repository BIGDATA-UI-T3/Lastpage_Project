document.addEventListener("DOMContentLoaded", () => {

      /* ===============================
         회원정보 수정 폼 제어
      =============================== */
      const form = document.getElementById("editForm");
      if (!form) return;

      const userType = form.dataset.usertype;
      const oauthEmail = form.dataset.oauthemail;

      const name = document.getElementById("name");
      const emailId = document.getElementById("emailId");
      const emailDomain = document.getElementById("emailDomain");
      const password = document.getElementById("password");
      const pwConfirm = document.getElementById("password-confirm");

      // 소셜 로그인 사용자의 경우 — OAuth 이메일 불러오기 + 수정 불가 처리
      if (userType === "social") {
        if (oauthEmail) {
          const [idPart, domainPart] = oauthEmail.split("@");
          if (emailId) emailId.value = idPart || "";
          if (emailDomain) emailDomain.value = domainPart || "";
        }
        [name, emailId, emailDomain, password, pwConfirm].forEach(el => el.disabled = true);
      }

      //  자체 로그인 사용자의 경우 — 모든 항목 수정 가능
      if (userType === "native") {
        [name, emailId, emailDomain, password, pwConfirm].forEach(el => el.disabled = false);
      }

      /* ===============================
         비밀번호 강도 검사 및 재사용 체크
      =============================== */
      const pwBar = document.getElementById("pwStrengthBar");
      const pwText = document.getElementById("pwStrengthText");

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
            if (reuseCheck.ok) {
              const reused = await reuseCheck.text();
              if (reused === "reused" && pwText && pwBar) {
                pwText.textContent = "기존에 사용한 비밀번호는 사용할 수 없습니다.";
                pwText.style.color = "#ff4d4f";
                pwBar.style.backgroundColor = "#ff4d4f";
              }
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
          case 0:
          case 1:
            return { score, text: "보안 수준: 약함", color: "#ff4d4f" };
          case 2:
            return { score, text: "보안 수준: 보통", color: "#faad14" };
          case 3:
            return { score, text: "보안 수준: 좋음", color: "#52c41a" };
          case 4:
            return { score, text: "보안 수준: 매우 강함", color: "#389e0d" };
          default:
            return { score: 0, text: "비밀번호를 입력하세요", color: "#ccc" };
        }
      }

      /* ===============================
         저장 요청
      =============================== */
      const saveBtn = document.getElementById("saveBtn");
      if (saveBtn) {
        saveBtn.addEventListener("click", async () => {
          const pwVal = password.value.trim();
          const pw2Val = pwConfirm.value.trim();
          if (pwVal && pwVal !== pw2Val) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
          }

          const birthValue = document.getElementById("birth").value;
          let year = null,
            month = null,
            day = null;
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

        // 새 이름을 localStorage에 저장 (백엔드 응답 대신 폼 값 사용)
        const newName = name.value.trim();
        localStorage.setItem("updatedUserName", newName);

        // 마이페이지로 이동
        window.location.href = "/mypage/Mypage";
      } else {
        const msg = await res.text();
        alert(msg);
      }
        });
      }

      const deleteBtn = document.getElementById("deleteBtn");
      if (deleteBtn) {
        deleteBtn.addEventListener("click", async () => {
          try {
            //  세션 유효성 확인
            const sessionRes = await fetch("/mypage/EditInfo/session/check", { cache: "no-store" });
            if (!sessionRes.ok) {
              alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
              window.location.href = "/signin";
              return;
            }
            const sessionData = await sessionRes.json();
            const userType = sessionData.userType;
            const userSeq = sessionData.userSeq;

            //  탈퇴 확인 모달 표시
            if (!confirm("정말 회원 탈퇴를 진행하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")) {
              return;
            }

            //  소셜 로그인 회원 → 바로 탈퇴
            if (userType === "social") {
              const delRes = await fetch(`/mypage/EditInfo/delete/${userSeq}`, {
                method: "DELETE",
              });
              if (delRes.ok) {
                alert("회원 탈퇴가 완료되었습니다.");
                window.location.href = "/signin";
              } else {
                const msg = await delRes.text();
                alert("탈퇴 실패: " + msg);
              }
              return;
            }

            // 자체 로그인 회원 → 비밀번호 확인 후 탈퇴
            const pw = prompt("회원 탈퇴를 진행하려면 비밀번호를 입력해주세요:");
            if (!pw) {
              alert("비밀번호 입력이 취소되었습니다.");
              return;
            }

            const verifyRes = await fetch(`/mypage/EditInfo/verify-password/${userSeq}`, {
              method: "POST",
              headers: { "Content-Type": "text/plain" },
              body: pw,
            });

            if (!verifyRes.ok) {
              alert("비밀번호 확인 중 오류가 발생했습니다.");
              return;
            }

            const verifyText = await verifyRes.text();
            if (verifyText !== "valid") {
              alert("비밀번호가 올바르지 않습니다.");
              return;
            }

            const delRes = await fetch(`/mypage/EditInfo/delete/${userSeq}`, {
              method: "DELETE",
            });

            if (delRes.ok) {
              alert("회원 탈퇴가 완료되었습니다.");
              window.location.href = "/signin";
            } else {
              const msg = await delRes.text();
              alert("회원 탈퇴 실패: " + msg);
            }

          } catch (err) {
            console.error("회원탈퇴 처리 중 오류:", err);
            alert("회원탈퇴 처리 중 오류가 발생했습니다.");
          }
        });
      }
});