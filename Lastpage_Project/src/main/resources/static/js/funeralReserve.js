document.addEventListener("DOMContentLoaded", () => {

  // ------------------------------
  // 1) 상태 객체 & 헬퍼 함수
  // ------------------------------
  const state = {
    id: "",
    ownerName: "", ownerPhone: "", ownerEmail: "", ownerAddr: "",
    petName: "", petType: "", petBreed: "", petWeight: "",
    passedAt: "", place: "", funeralDate: "", type: "", ash: "",
    pickup: "직접 방문", pickupAddr: "", pickupTime: "",
    time: "", memo: ""
  };
  const qs = s => document.querySelector(s);
  const qsa = s => [...document.querySelectorAll(s)];

  // ------------------------------
  // 2) URL 파라미터 확인 (수정 모드 여부)
  // ------------------------------
  const params = new URLSearchParams(window.location.search);
  const reserveId = params.get("id");
  if (reserveId) {
    state.id = reserveId;
    qs("#formTitle").textContent = "장례 예약 수정";
    qs("#finalTitle").textContent = "예약 수정이 완료되었습니다.";
  }

  // ------------------------------
  // 3) 날짜 제한 설정
  // ------------------------------
  (function setDateLimits() {
    const t = new Date();
    const yyyy = t.getFullYear(), mm = String(t.getMonth()+1).padStart(2,'0'), dd = String(t.getDate()).padStart(2,'0');
    const hh = String(t.getHours()).padStart(2,'0'), mi = String(t.getMinutes()).padStart(2,'0');
    qs('#funeralDate').min = `${yyyy}-${mm}-${dd}`;
    qs('#passedAt').max = `${yyyy}-${mm}-${dd}T${hh}:${mi}`;
  })();

  // ------------------------------
  // 4) 품종 데이터 및 핸들러
  // ------------------------------
  const BREEDS = {
    dog: ["말티즈","푸들","시바","리트리버","포메라니안","믹스"],
    cat: ["코리안숏헤어","페르시안","러시안블루","먼치킨","랙돌","믹스"],
    small: ["햄스터","토끼","기니피그","패럿","앵무새"]
  };
  const petTypeEl = qs("#petType");
  const petBreedSel = qs("#petBreed");
  const petBreedText = qs("#petBreedText");

  petTypeEl.addEventListener("change", () => {
    const type = petTypeEl.value;
    state.petType = type;
    if (!type) {
      petBreedSel.innerHTML = '<option value="">종류 선택 먼저</option>';
      petBreedSel.disabled = true;
      petBreedText.style.display = "none";
      return validateStep1();
    }
    if (type === "other") {
      petBreedSel.disabled = true;
      petBreedSel.innerHTML = '<option value="">직접 입력</option>';
      petBreedText.style.display = "block";
    } else {
      petBreedSel.disabled = false;
      petBreedText.style.display = "none";
      petBreedText.value = "";
      petBreedSel.innerHTML = '<option value="">품종 선택</option>' + BREEDS[type].map(b => `<option>${b}</option>`).join('');
    }
    validateStep1();
  });
  petBreedSel.addEventListener("change", () => { state.petBreed = petBreedSel.value; validateStep1(); });
  petBreedText.addEventListener("input", () => { state.petBreed = petBreedText.value.trim(); validateStep1(); });

  // ------------------------------
  // 5) 수정 모드 데이터 불러오기
  // ------------------------------
  if (state.id) {
    fetch(`/api/funeral_reserve/${state.id}`)
      .then(res => res.json())
      .then(data => {
        Object.assign(state, data);
        fillForm(data);
      })
      .catch(err => console.error("예약 정보 불러오기 실패:", err));
  }

  // ------------------------------
  // 6) STEP1 검증
  // ------------------------------
  function validateStep1() {
    const name = qs("#ownerName").value.trim();
    const phone = qs("#ownerPhone").value.replace(/\D/g, '');
    const email = qs("#ownerEmail").value.trim();
    const addr = qs("#ownerAddr").value.trim();
    const pet = qs("#petName").value.trim();
    const weight = qs("#petWeight").value.trim();

    const emailOK = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    const breedOK = state.petType === "other"
      ? !!state.petBreed
      : !!state.petType && !!state.petBreed;

    const all = name && phone.length >= 10 && emailOK && addr && pet && breedOK && Number(weight) >= 0;
    qs("#next1").disabled = !all;

    if (all)
      Object.assign(state, { ownerName: name, ownerPhone: phone, ownerEmail: email, ownerAddr: addr, petName: pet, petWeight: weight });
  }
  qsa("#step1 input, #step1 select").forEach(el => el.addEventListener("input", validateStep1));
  validateStep1();

  // ------------------------------
  // 7) STEP2 시간 슬롯 빌드
  // ------------------------------
  const AM = ["07:00","07:30","08:00","08:30","09:00","09:30","10:00","10:30","11:00","11:30"];
  const PM = ["12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00"];
  let currentSeg = "am", selectedBtn = null;

  function buildSlots() {
    const box = qs("#timeSlots");
    box.innerHTML = "";
    const list = currentSeg === "am" ? AM : PM;
    list.forEach(t => {
      const b = document.createElement("button");
      b.type = "button";
      b.className = "slot";
      b.textContent = t;
      b.dataset.value = t;
      b.addEventListener("click", () => {
        if (selectedBtn) selectedBtn.classList.remove("active");
        b.classList.add("active");
        selectedBtn = b;
        state.time = t;
        validateStep2();
      });
      box.appendChild(b);
    });
  }
  buildSlots();
  qsa(".seg-btn").forEach(b => {
    b.addEventListener("click", () => {
      qsa(".seg-btn.is-active").forEach(btn => btn.classList.remove("is-active"));
      b.classList.add("is-active");
      currentSeg = b.dataset.seg;
      buildSlots();
    });
  });

  // ------------------------------
  // 8) STEP2 검증
  // ------------------------------
  qs("#pickup").addEventListener("change", () => {
    const need = qs("#pickup").value === "운구 요청";
    qs("#pickupExtra").style.display = need ? "grid" : "none";
    if (!need) {
      qs("#pickupAddr").value = "";
      qs("#pickupTime").value = "";
    }
    validateStep2();
  });

  function validateStep2() {
    const passedAt = qs("#passedAt").value;
    const place = qs("#place").value;
    const funeralDate = qs("#funeralDate").value;
    const type = qs("#type").value;
    const ash = qs("#ash").value;
    const pickup = qs("#pickup").value;

    Object.assign(state, { passedAt, place, funeralDate, type, ash, pickup });

    let ok = !!passedAt && !!place && !!funeralDate && !!type && !!ash && !!state.time;

    if (pickup === "운구 요청") {
      state.pickupAddr = qs("#pickupAddr").value.trim();
      state.pickupTime = qs("#pickupTime").value;
      ok = ok && !!state.pickupAddr && !!state.pickupTime;
    } else {
      state.pickupAddr = "";
      state.pickupTime = "";
    }

    qs("#next2").disabled = !ok;
  }
  qsa("#step2 input, #step2 select").forEach(el => el.addEventListener("input", validateStep2));

  qs("#memo").addEventListener("input", e => {
    qs("#memoCount").textContent = e.target.value.length;
    state.memo = e.target.value;
  });

  // ------------------------------
  // 9) 단계 이동
  // ------------------------------
  const goto = n => {
    qsa(".step").forEach((el, i) => el.classList.toggle("active", i === n - 1));
    qsa(".step-dot").forEach((d, i) => d.classList.toggle("active", i <= n - 1));
    window.scrollTo({ top: 0, behavior: "smooth" });
    if (n === 3) renderSummary();
    if (n === 4) renderFinal();
  };

  qs("#next1").addEventListener("click", () => goto(2));
  qs("#prev2").addEventListener("click", () => goto(1));
  qs("#next2").addEventListener("click", () => goto(3));
  qs("#prev3").addEventListener("click", () => goto(2));
  qs("#next3").addEventListener("click", () => goto(4));
  qs("#prev4").addEventListener("click", () => goto(3));

  // ------------------------------
  // 10) 요약 렌더링
  // ------------------------------
  function renderSummary() {
    const s = qs("#summary");
    const pickupInfo = state.pickup === "운구 요청"
      ? `${state.pickup} / ${state.pickupAddr} (${state.pickupTime})`
      : state.pickup;
    s.innerHTML = `
      <div><strong>신청자명</strong> : ${state.ownerName}</div>
      <div><strong>연락처</strong> : ${state.ownerPhone}</div>
      <div><strong>이메일</strong> : ${state.ownerEmail}</div>
      <div><strong>주소</strong> : ${state.ownerAddr}</div>
      <hr>
      <div><strong>반려동물</strong> : ${state.petName} (${state.petType} / ${state.petBreed}, ${state.petWeight}kg)</div>
      <div><strong>사망 일시</strong> : ${state.passedAt}</div>
      <div><strong>장례식장</strong> : ${state.place}</div>
      <div><strong>장례일</strong> : ${state.funeralDate}</div>
      <div><strong>장례 유형</strong> : ${state.type}</div>
      <div><strong>유골 처리</strong> : ${state.ash}</div>
      <div><strong>운구</strong> : ${pickupInfo}</div>
      <div><strong>예약 시간</strong> : ${state.time}</div>
      <div><strong>요청사항</strong> : ${state.memo || '-'}</div>
    `;
  }

  qs("#confirmChk").addEventListener("change", () => {
    qs("#next3").disabled = !qs("#confirmChk").checked;
  });

  // ------------------------------
  // 11) 최종 안내 출력
  // ------------------------------
  function renderFinal() {
    const f = qs("#finalSummary");
    f.innerHTML = `
      <div><strong>${state.ownerName}</strong> 님의 ${state.id ? "예약이 수정되었습니다." : "예약 요청이 완료되었습니다."}</div>
      <div class="muted">장례일시: ${state.funeralDate}, 장소: ${state.place}</div>
    `;
  }

  // ------------------------------
  // 12) 수정 모드 폼 채우기
  // ------------------------------
  function fillForm(data) {
    qs("#ownerName").value = data.ownerName || "";
    qs("#ownerPhone").value = data.ownerPhone || "";
    qs("#ownerEmail").value = data.ownerEmail || "";
    qs("#ownerAddr").value = data.ownerAddr || "";
    qs("#petName").value = data.petName || "";
    qs("#petType").value = data.petType || "";
    qs("#petWeight").value = data.petWeight || "";
    qs("#passedAt").value = data.passedAt || "";
    qs("#place").value = data.place || "";
    qs("#funeralDate").value = data.funeralDate || "";
    qs("#type").value = data.type || "";
    qs("#ash").value = data.ash || "";
    qs("#pickup").value = data.pickup || "";
    qs("#memo").value = data.memo || "";

    if (data.petType === "other") {
      qs("#petBreedText").style.display = "block";
      qs("#petBreedText").value = data.petBreed || "";
    } else {
      qs("#petBreed").value = data.petBreed || "";
    }

    if (data.time) {
      const btn = [...document.querySelectorAll(".slot")].find(b => b.dataset.value === data.time);
      if (btn) {
        btn.classList.add("active");
      }
    }
    validateStep1();
    validateStep2();
  }

  // ------------------------------
  // 13) 제출 (REST API)
  // ------------------------------
  qs("#submitBtn").addEventListener("click", async () => {
    const data = { ...state };

    let userSeq = document.body.dataset.userseq || localStorage.getItem("userSeq");
    if (!userSeq) {
      alert("로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.");
      window.location.href = "/signin";
      return;
    }
    data.userSeq = userSeq;

    const url = data.id
      ? `/reserve/funeral_reserve/${data.id}`
      : "/reserve/save3";
    const method = data.id ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      if (res.ok) {
        alert(data.id ? "예약이 성공적으로 수정되었습니다!" : "예약이 성공적으로 저장되었습니다!");
        window.location.href = `/mypage/Mypage?userSeq=${userSeq}`;
      } else {
        const errorText = await res.text();
        console.error("서버 오류 응답:", errorText);
        alert("저장 중 오류가 발생했습니다.");
      }
    } catch (err) {
      console.error("서버 통신 오류:", err);
      alert("서버 통신 중 오류가 발생했습니다.");
    }
  });

  // ------------------------------
  // 초기 포커스
  // ------------------------------
  qs("#ownerName").focus();
});
