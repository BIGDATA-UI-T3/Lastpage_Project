document.addEventListener("DOMContentLoaded", () => {
         const form = document.getElementById("loginForm");
         const pw = document.getElementById("password");

         /** ------------------------------
          *  CapsLock 감지
          * ------------------------------ */
         if (pw) {
           pw.addEventListener("keyup", (e) => {
             const hint = document.getElementById("capsHint");
             if (!hint) return;
             const caps = e.getModifierState && e.getModifierState("CapsLock");
             hint.style.display = caps ? "block" : "none";
           });
         }

         /** ------------------------------
          *  비밀번호 보기 토글
          * ------------------------------ */
         const togglePw = document.getElementById("togglePw");
         const eyeOpen = document.getElementById("eyeOpen");
         const eyeClosed = document.getElementById("eyeClosed");
         if (togglePw && pw && eyeOpen && eyeClosed) {
           togglePw.addEventListener("click", () => {
             const isHidden = pw.type === "password";
             pw.type = isHidden ? "text" : "password";
             eyeOpen.style.display = isHidden ? "none" : "block";
             eyeClosed.style.display = isHidden ? "block" : "none";
           });
         }

         /** ------------------------------
          *  자체 로그인 (REST)
          * ------------------------------ */
         form.addEventListener("submit", async (e) => {
           e.preventDefault();

           const id = (document.getElementById("userid").value || "").trim();
           const password = (document.getElementById("password").value || "").trim();

           if (!id || !password) {
             alert("아이디와 비밀번호를 모두 입력해주세요.");
             return;
           }

           try {
             const res = await fetch("/api/loginProc", {
               method: "POST",
               headers: { "Content-Type": "application/json" },
               credentials: "same-origin", // 세션 쿠키 포함
               body: JSON.stringify({ id, password }),
             });

             const text = await res.text();
             let data;
             try {
               data = JSON.parse(text);
             } catch {
               data = { result: "fail", message: text };
             }

             if (res.ok && data.result === "ok") {
               const displayName =
                 data.name ||
                 data.username ||
                 (data.user && (data.user.name || data.user.id)) ||
                 data.id ||
                 "회원";

               //  헤더 즉시 업데이트 (이벤트 브로드캐스트)
               window.dispatchEvent(
                 new CustomEvent("lp:login-success", {
                   detail: {
                     userSeq: data.userSeq,
                     id: data.id,
                     name: displayName,
                   },
                 })
               );

               alert(`${displayName}님 환영합니다!`);

       //  세션 유지된 상태로 메인 페이지 이동
       window.location.href = "/";
     } else {
               alert(data.message || "로그인 실패");
             }
           } catch (err) {
             console.error("로그인 요청 실패:", err);
             alert("서버 통신 중 오류가 발생했습니다.");
           }
         });

         /** ------------------------------
          *  소셜 로그인 (카카오/네이버/구글)
          * ------------------------------ */
         const kakaoBtn = document.getElementById("kakaoLoginBtn");
         if (kakaoBtn) {
           kakaoBtn.addEventListener("click", () => {
             const clientId = "844a49fe8f08e70d91ff67190a508258";
             const redirectUri = "http://localhost:8090/login/oauth2/code/kakao";
             window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code`;
           });
         }

         const naverBtn = document.getElementById("naverLoginBtn");
         if (naverBtn) {
           naverBtn.addEventListener("click", () => {
             const clientId = "bwe65meU2ddvhnuGNtPX";
             const redirectUri = "http://localhost:8090/login/oauth2/code/naver";
             const state = Math.random().toString(36).substring(2, 15);
             window.location.href = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
           });
         }

         const googleBtn = document.getElementById("googleLoginBtn");
         if (googleBtn) {
           googleBtn.addEventListener("click", () => {
             //값 넣기
           });
         }
       });