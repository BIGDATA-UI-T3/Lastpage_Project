//document.addEventListener("DOMContentLoaded", () => {
//
//
// /* ================================
//   * 0) 관리자 모드 감지
//   * ================================ */
//  const mode = document.body.dataset.mode || "create";              // "admin-edit" | "edit" | "create"
//  const targetUserSeq = document.body.dataset.targetuserseq || null; // 관리자 모드일 때만 세팅됨
//  const loginUserSeq = document.body.dataset.userseq || null;
//
//  const isAdminEdit = mode === "admin-edit";
//  // 관리자 모드일 때는 loginUserSeq != targetUserSeq 가능
//  const effectiveUserSeq = isAdminEdit ? (targetUserSeq || loginUserSeq) : loginUserSeq;
//
//  if (!effectiveUserSeq || effectiveUserSeq === "null" || effectiveUserSeq === "undefined") {
//    alert("우선 로그인을 진행해주세요.");
//    window.location.href = "/signin";
//    return;
//  }
//
//  // ------------------------------
//  // 1) 상태 객체 & 헬퍼 함수
//  // ------------------------------
//  const state = {
//    name: "", birth: "", gender: "", phone: "", email: "", address: "",
//    consultDate: "", time: "", counselor: "", memo: ""
//  };
//  const qs = s => document.querySelector(s);
//  const qsa = s => [...document.querySelectorAll(s)];
//
//  // ------------------------------
//  // 2) URL 파라미터 확인 (수정 모드 여부)
//  // ------------------------------
//  const params = new URLSearchParams(window.location.search);
//  const reserveId = params.get("id");
//  if (reserveId) {
//    state.id = reserveId;
//    qs("#formTitle").textContent = "심리상담 예약 수정";
//    qs("#finalTitle").textContent = "예약 수정이 완료되었습니다.";
//  }
//
//  // ------------------------------
//  // 3) 날짜 제한 설정
//  // ------------------------------
//  (function setDateLimits() {
//    const today = new Date();
//    qs('#birth').max = today.toISOString().slice(0, 10);
//    qs('#consultDate').min = today.toISOString().slice(0, 10);
//  })();
//
//  // ------------------------------
//  // 4) 시간 슬롯 빌드
//  // ------------------------------
//  const timeWrap = qs('#timeSlots');
//  let selectedBtn = null;
//  (function buildSlots() {
//    for (let h = 10; h <= 18; h++) {
//      const from = String(h).padStart(2, '0') + ':00';
//      const to = String(h + 1).padStart(2, '0') + ':00';
//      const b = document.createElement('button');
//      b.type = 'button';
//      b.className = 'slot';
//      b.textContent = `${from} - ${to}`;
//      b.dataset.value = `${from}-${to}`;
//      b.addEventListener('click', () => {
//        if (selectedBtn) selectedBtn.classList.remove('active');
//        b.classList.add('active');
//        selectedBtn = b;
//        state.time = b.dataset.value;
//        validateStep2();
//      });
//      timeWrap.appendChild(b);
//    }
//  })();
//
//  // ------------------------------
//  // 5) 수정 모드 데이터 불러오기
//  // ------------------------------
//  if (state.id) {
//    fetch(`/api/psy_reserve/${state.id}`)
//      .then(res => res.json())
//      .then(data => {
//        Object.assign(state, data);
//        fillForm(data);
//      })
//      .catch(err => console.error("예약 정보 불러오기 실패:", err));
//  }
//
//  // ------------------------------
//  // 6) 1단계 입력 검증
//  // ------------------------------
//  const requireFilled = () => {
//    const name = qs('#name').value.trim();
//    const birth = qs('#birth').value;
//    const gender = qs('#gender').value;
//    const phone = qs('#phone').value.replace(/\D/g, '');
//    const email = qs('#email').value.trim();
//    const address = qs('#address').value.trim();
//    const validEmail = !!email && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
//    const validPhone = phone.length >= 10;
//    const all = name && birth && gender && validPhone && validEmail && address;
//    qs('#next1').disabled = !all;
//
//    if (all) Object.assign(state, { name, birth, gender, phone, email, address });
//  };
//  qsa('#step1 input, #step1 select').forEach(el => el.addEventListener('input', requireFilled));
//  requireFilled();
//
//  // ------------------------------
//  // 7) 2단계 검증
//  // ------------------------------
//  function validateStep2() {
//    const date = qs('#consultDate').value;
//    const counselor = qs('#counselor').value;
//    const ok = !!date && !!state.time && !!counselor;
//    qs('#next2').disabled = !ok;
//    if (ok) Object.assign(state, { consultDate: date, counselor });
//  }
//  qsa('#consultDate, #counselor').forEach(el => el.addEventListener('input', validateStep2));
//
//  // ------------------------------
//  // 8) 메모 카운트
//  // ------------------------------
//  const memoEl = qs('#memo');
//  const memoCount = qs('#memoCount');
//  memoEl.addEventListener('input', () => {
//    memoCount.textContent = memoEl.value.length;
//    state.memo = memoEl.value;
//  });
//
//  // ------------------------------
//  // 9) 단계 이동
//  // ------------------------------
//  const goto = (n) => {
//    qsa('.step').forEach((el, i) => el.classList.toggle('active', i === n - 1));
//    qsa('.step-dot').forEach((d, i) => d.classList.toggle('active', i <= n - 1));
//    window.scrollTo({ top: 0, behavior: 'smooth' });
//    if (n === 3) renderSummary();
//    if (n === 4) renderFinal();
//  };
//  qs('#next1').addEventListener('click', () => goto(2));
//  qs('#prev2').addEventListener('click', () => goto(1));
//  qs('#next2').addEventListener('click', () => goto(3));
//  qs('#prev3').addEventListener('click', () => goto(2));
//  qs('#next3').addEventListener('click', () => goto(4));
//  qs('#prev4').addEventListener('click', () => goto(3));
//
//  // ------------------------------
//  // 10) 체크박스 제어
//  // ------------------------------
//  const chk = qs('#confirmChk');
//  chk.addEventListener('change', () => qs('#next3').disabled = !chk.checked);
//
//  // ------------------------------
//  // 11) 요약 렌더링
//  // ------------------------------
//  function renderSummary() {
//    const s = qs('#summary');
//    s.innerHTML = `
//      <div><strong>예약자명</strong> : ${state.name}</div>
//      <div><strong>생년월일</strong> : ${state.birth}</div>
//      <div><strong>성별</strong> : ${state.gender}</div>
//      <div><strong>휴대폰</strong> : ${state.phone}</div>
//      <div><strong>이메일</strong> : ${state.email}</div>
//      <div><strong>주소</strong> : ${state.address}</div>
//      <hr style="border:none;border-top:1px solid #eee;margin:10px 0">
//      <div><strong>상담 날짜</strong> : ${state.consultDate}</div>
//      <div><strong>상담 시간</strong> : ${state.time}</div>
//      <div><strong>상담사</strong> : ${state.counselor}</div>
//      <div><strong>메모</strong> : ${state.memo ? state.memo.replace(/\n/g, '<br>') : '-'}</div>
//    `;
//  }
//
//  // ------------------------------
//  // 12) 수정 모드 폼 채우기
//  // ------------------------------
//  function fillForm(data) {
//    qs('#name').value = data.name || "";
//    qs('#birth').value = data.birth || "";
//    qs('#gender').value = data.gender || "";
//    qs('#phone').value = data.phone || "";
//    qs('#email').value = data.email || "";
//    qs('#address').value = data.address || "";
//    qs('#consultDate').value = data.consultDate || "";
//    qs('#counselor').value = data.counselor || "";
//    qs('#memo').value = data.memo || "";
//    if (data.time) {
//      const btn = [...document.querySelectorAll('.slot')]
//        .find(b => b.dataset.value === data.time);
//      if (btn) {
//        btn.classList.add('active');
//        selectedBtn = btn;
//      }
//    }
//    requireFilled();
//    validateStep2();
//  }
//
//  // ------------------------------
//  // 13) 최종 안내 출력
//  // ------------------------------
//  function renderFinal() {
//    const f = qs('#finalSummary');
//    f.innerHTML = `
//      <div><strong>${state.name}</strong> 님의 ${state.id ? '예약이 수정되었습니다.' : '예약 요청이 접수되었습니다.'}</div>
//      <div class="muted">상담일시: ${state.consultDate} ${state.time}, 상담사: ${state.counselor}</div>
//    `;
//  }
//
//  // ------------------------------
//  // 14) 제출 (REST API)
//  // ------------------------------
//  qs('#submitBtn').addEventListener('click', async () => {
//    const data = { ...state };
//
//    // ------------------------------
//    // 1) 로그인 상태 확인
//    // ------------------------------
//    let userSeq = document.body.dataset.userseq || localStorage.getItem('userSeq');
//    if (!userSeq) {
//      alert('로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.');
//      window.location.href = '/signin';
//      return;
//    }
//    data.userSeq = userSeq;
//
//    // ------------------------------
//    // 2) 요청 URL 및 메서드 결정
//    // ------------------------------
//    const url = data.id ? `/reserve/psy_reserve/${data.id}` : '/reserve/save1';
//    const method = data.id ? 'PUT' : 'POST';
//
//    try {
//      // ------------------------------
//      // 3) 서버로 전송
//      // ------------------------------
//      const res = await fetch(url, {
//        method,
//        headers: { 'Content-Type': 'application/json' },
//        body: JSON.stringify(data)
//      });
//
//      // ------------------------------
//      // 4) 응답 상태별 처리
//      // ------------------------------
//      if (res.status === 401) {
//        // 로그인 안 된 상태 → 로그인 페이지로 리다이렉트
//        alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
//        window.location.href = '/signin';
//        return;
//      }
//
//      if (res.ok) {
//        alert(data.id ? '예약이 성공적으로 수정되었습니다!' : '예약이 성공적으로 저장되었습니다!');
//        window.location.href = `/mypage/Mypage?userSeq=${userSeq}`;
//      } else {
//        const errorText = await res.text();
//        console.error('서버 오류 응답:', errorText);
//        alert('저장 중 오류가 발생했습니다.');
//      }
//    } catch (err) {
//      // ------------------------------
//      // 5) 네트워크 또는 서버 통신 에러
//      // ------------------------------
//      console.error('서버 통신 오류:', err);
//      alert('서버 통신 중 오류가 발생했습니다.');
//    }
//  });
//
//  // ------------------------------
//  // 초기 포커스
//  // ------------------------------
//  qs('#name').focus();
//
//});

//=========================
document.addEventListener("DOMContentLoaded", () => {

  /* ================================
   * 0) 관리자 모드 감지
   * ================================ */
  const mode = document.body.dataset.mode || "create";      // "admin-edit" | "edit" | "create"
  const targetUserSeq = document.body.dataset.targetuserseq || null;
  const loginUserSeq   = document.body.dataset.userseq || null;

  const isAdminEdit = mode === "admin-edit";

  // 관리자면 targetUserSeq, 사용자면 loginUserSeq 기준
  const effectiveUserSeq = isAdminEdit ? (targetUserSeq || loginUserSeq) : loginUserSeq;

  if (!effectiveUserSeq || effectiveUserSeq === "null") {
    alert("로그인이 필요합니다.");
    location.href = "/signin";
    return;
  }

  // ------------------------------
  // 1) 상태 객체 & 헬퍼
  // ------------------------------
  const state = {
    id: "",
    name: "", birth: "", gender: "", phone: "", email: "", address: "",
    consultDate: "", time: "", counselor: "", memo: ""
  };

  const qs  = sel => document.querySelector(sel);
  const qsa = sel => [...document.querySelectorAll(sel)];

  // ------------------------------
  // 2) 수정 모드 확인
  // ------------------------------
  const params     = new URLSearchParams(location.search);
  const reserveId  = params.get("id");

  if (reserveId) {
    state.id = reserveId;

    qs("#formTitle").textContent  = isAdminEdit ? "관리자 - 심리상담 예약 수정" : "심리상담 예약 수정";
    qs("#finalTitle").textContent = isAdminEdit ?
      "관리자가 상담 예약을 수정했습니다." :
      "예약 수정이 완료되었습니다.";
  }

  // ------------------------------
  // 3) 날짜 제한
  // ------------------------------
  (function setDateLimits() {
    const today = new Date().toISOString().slice(0, 10);
    qs("#birth").max       = today;
    qs("#consultDate").min = today;
  })();

  // ------------------------------
  // 4) 시간 슬롯 생성
  // ------------------------------
  const timeWrap = qs("#timeSlots");
  let selectedBtn = null;
  (function buildSlots() {
    for (let h = 10; h <= 18; h++) {
      const from = `${String(h).padStart(2,"0")}:00`;
      const to   = `${String(h+1).padStart(2,"0")}:00`;
      const slot = document.createElement("button");

      slot.type = "button";
      slot.className = "slot";
      slot.textContent = `${from} - ${to}`;
      slot.dataset.value = `${from}-${to}`;

      slot.addEventListener("click", () => {
        if (selectedBtn) selectedBtn.classList.remove("active");
        slot.classList.add("active");
        selectedBtn = slot;
        state.time = slot.dataset.value;
        validateStep2();
      });

      timeWrap.appendChild(slot);
    }
  })();


  // ------------------------------
  // 5) 수정 모드 데이터 로드
  // ------------------------------
  if (state.id) {
    fetch(`/api/psy_reserve/${state.id}`)
      .then(res => res.json())
      .then(data => {
        Object.assign(state, data);
        fillForm(data);
      })
      .catch(err => console.error("예약 정보 불러오기 실패:", err));
  }


  // ------------------------------
  // 6) 1단계 검증
  // ------------------------------
  const validateStep1 = () => {
    const name    = qs("#name").value.trim();
    const birth   = qs("#birth").value;
    const gender  = qs("#gender").value;
    const phone   = qs("#phone").value.replace(/\D/g, "");
    const email   = qs("#email").value.trim();
    const address = qs("#address").value.trim();

    const emailOK = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    const phoneOK = phone.length >= 10;

    const ok = name && birth && gender && emailOK && phoneOK && address;

    qs("#next1").disabled = !ok;

    if (ok) {
      Object.assign(state, { name, birth, gender, phone, email, address });
    }
  };

  qsa("#step1 input, #step1 select").forEach(el =>
    el.addEventListener("input", validateStep1)
  );

  validateStep1();


  // ------------------------------
  // 7) 2단계 검증
  // ------------------------------
  const validateStep2 = () => {
    const date = qs("#consultDate").value;
    const counselor = qs("#counselor").value;

    const ok = date && state.time && counselor;
    qs("#next2").disabled = !ok;

    if (ok) Object.assign(state, { consultDate: date, counselor });
  };

  qsa("#consultDate, #counselor").forEach(el =>
    el.addEventListener("input", validateStep2)
  );


  // ------------------------------
  // 8) 메모 글자수
  // ------------------------------
  qs("#memo").addEventListener("input", e => {
    qs("#memoCount").textContent = e.target.value.length;
    state.memo = e.target.value;
  });

  // ------------------------------
  // 9) 단계 이동
  // ------------------------------
  const goto = (step) => {
    qsa(".step").forEach((el, i) =>
      el.classList.toggle("active", i === step - 1)
    );
    qsa(".step-dot").forEach((d, i) =>
      d.classList.toggle("active", i <= step - 1)
    );

    if (step === 3) renderSummary();
    if (step === 4) renderFinal();
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  qs("#next1").addEventListener("click", () => goto(2));
  qs("#prev2").addEventListener("click", () => goto(1));
  qs("#next2").addEventListener("click", () => goto(3));
  qs("#prev3").addEventListener("click", () => goto(2));
  qs("#next3").addEventListener("click", () => goto(4));
  qs("#prev4").addEventListener("click", () => goto(3));


  // ------------------------------
  // 10) 확인 체크박스
  // ------------------------------
  qs("#confirmChk").addEventListener("change", e => {
    qs("#next3").disabled = !e.target.checked;
  });


  // ------------------------------
  // 11) 요약 렌더링
  // ------------------------------
  const renderSummary = () => {
    qs("#summary").innerHTML = `
      <div><strong>예약자명</strong> : ${state.name}</div>
      <div><strong>생년월일</strong> : ${state.birth}</div>
      <div><strong>휴대폰</strong> : ${state.phone}</div>
      <div><strong>이메일</strong> : ${state.email}</div>
      <div><strong>주소</strong> : ${state.address}</div>
      <hr>
      <div><strong>상담 날짜</strong> : ${state.consultDate}</div>
      <div><strong>상담 시간</strong> : ${state.time}</div>
      <div><strong>상담사</strong> : ${state.counselor}</div>
      <div><strong>메모</strong> : ${state.memo || "-"}</div>
    `;
  };


  // ------------------------------
  // 12) 수정 모드 폼 채우기
  // ------------------------------
  const fillForm = (data) => {
    qs("#name").value = data.name || "";
    qs("#birth").value = data.birth || "";
    qs("#gender").value = data.gender || "";
    qs("#phone").value = data.phone || "";
    qs("#email").value = data.email || "";
    qs("#address").value = data.address || "";
    qs("#consultDate").value = data.consultDate || "";
    qs("#counselor").value = data.counselor || "";
    qs("#memo").value = data.memo || "";

    if (data.time) {
      const btn = [...document.querySelectorAll(".slot")]
        .find(b => b.dataset.value === data.time);
      if (btn) {
        btn.classList.add("active");
        selectedBtn = btn;
      }
    }

    validateStep1();
    validateStep2();
  };


  // ------------------------------
  // 13) 최종 안내 출력
  // ------------------------------
  const renderFinal = () => {
    qs("#finalSummary").innerHTML = `
      <div><strong>${state.name}</strong> 님의
        ${isAdminEdit ? "예약이 관리자에 의해 수정되었습니다." :
        (state.id ? "예약이 수정되었습니다." : "예약 요청이 완료되었습니다.")}
      </div>
      <div class="muted">상담일시: ${state.consultDate} ${state.time}, 상담사: ${state.counselor}</div>
    `;
  };


  // ------------------------------
  // 14) 제출 (REST API) : 관리자/사용자 공용
  // ------------------------------
  qs("#submitBtn").addEventListener("click", async () => {
    const data = { ...state, userSeq: effectiveUserSeq };

    // URL 분기
    const url = data.id
      ? (isAdminEdit
         ? `/reserve/admin/reserve/psy_reserve/${data.id}?targetUserSeq=${effectiveUserSeq}`
         : `/reserve/psy_reserve/${data.id}`)
      : "/reserve/save1";

    const method = data.id ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      if (res.ok) {
        if (isAdminEdit) {
          alert("관리자가 상담 예약을 수정했습니다.");
          location.href = "/admin/reserves";
        } else {
          alert(data.id ? "예약이 수정되었습니다!" : "예약이 저장되었습니다.");
          location.href = `/mypage/Mypage?userSeq=${effectiveUserSeq}`;
        }
      } else {
        alert(await res.text());
      }

    } catch (err) {
      console.error("통신 오류:", err);
      alert("서버 통신 중 오류가 발생했습니다.");
    }
  });

  // ------------------------------
  // 초기 포커스
  // ------------------------------
  qs("#name").focus();

});
