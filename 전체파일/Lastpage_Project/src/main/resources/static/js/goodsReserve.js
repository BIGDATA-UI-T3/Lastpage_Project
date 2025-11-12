// static/js/goods_reserve.js
document.addEventListener("DOMContentLoaded", () => {

  // ------------------------------
  // 1) 상태 객체 & 헬퍼 함수
  // ------------------------------
  const state = {
    ownerName: "", ownerPhone: "", ownerEmail: "", ownerAddr: "",
    petName: "", petType: "", petBreed: "", petWeight: "",
    materials: [],
    product: "", metalColor: "", chainLength: "", ringSize: "", quantity: 1,
    engravingText: "", engravingFont: "", optionsMemo: "",
    shipMethod: "", targetDate: "", isExpress: false,
    kitAddr: "", kitDate: "", kitTime: "",
    visitDate: "", visitTime: "", trackingInfo: "", memo: ""
  };

  const qs = s => document.querySelector(s);
  const qsa = s => [...document.querySelectorAll(s)];

  // ------------------------------
  // 2) URL 파라미터 확인 (수정 모드)
  // ------------------------------
  const params = new URLSearchParams(window.location.search);
  const reserveId = params.get("id");
  if (reserveId) {
    state.id = reserveId;
    qs("#formTitle").textContent = "굿즈 예약 수정";
  }

  // ------------------------------
  // 3) 날짜 제한 설정
  // ------------------------------
  (function setDateLimits() {
    const today = new Date();
    const y = today.getFullYear();
    const m = String(today.getMonth() + 1).padStart(2, "0");
    const d = String(today.getDate()).padStart(2, "0");
    const todayStr = `${y}-${m}-${d}`;

    const minTarget = new Date(today);
    minTarget.setDate(minTarget.getDate() + 7);
    const y2 = minTarget.getFullYear();
    const m2 = String(minTarget.getMonth() + 1).padStart(2, "0");
    const d2 = String(minTarget.getDate()).padStart(2, "0");

    if (qs("#targetDate")) qs("#targetDate").min = `${y2}-${m2}-${d2}`;
    if (qs("#kitDate")) qs("#kitDate").min = todayStr;
    if (qs("#visitDate")) qs("#visitDate").min = todayStr;
  })();

  // ------------------------------
  // 4) 1단계 검증
  // ------------------------------
  const BREEDS = {
    dog: ["말티즈", "푸들", "시바", "리트리버", "포메라니안", "믹스"],
    cat: ["코리안숏헤어", "페르시안", "러시안블루", "먼치킨", "랙돌", "믹스"],
    small: ["햄스터", "토끼", "기니피그", "패럿", "앵무새"]
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
      petBreedText.value = "";
      return validateStep1();
    }
    if (type === "other") {
      petBreedSel.disabled = true;
      petBreedText.style.display = "block";
      state.petBreed = petBreedText.value.trim();
    } else {
      petBreedSel.disabled = false;
      petBreedText.style.display = "none";
      petBreedSel.innerHTML =
        '<option value="">품종 선택</option>' +
        (BREEDS[type] || [])
          .map(b => `<option>${b}</option>`)
          .join("");
      state.petBreed = "";
    }
    validateStep1();
  });

  petBreedSel.addEventListener("change", () => {
    state.petBreed = petBreedSel.value;
    validateStep1();
  });
  petBreedText.addEventListener("input", () => {
    state.petBreed = petBreedText.value.trim();
    validateStep1();
  });

  qsa("#materialsBox .slot").forEach(btn => {
    btn.addEventListener("click", () => {
      const v = btn.dataset.mat;
      btn.classList.toggle("active");
      if (btn.classList.contains("active")) {
        if (!state.materials.includes(v)) state.materials.push(v);
      } else {
        state.materials = state.materials.filter(x => x !== v);
      }
      validateStep1();
    });
  });

  function validateStep1() {
    const name = qs("#ownerName").value.trim();
    const phone = qs("#ownerPhone").value.replace(/\D/g, "");
    const email = qs("#ownerEmail").value.trim();
    const addr = qs("#ownerAddr").value.trim();
    const pnm = qs("#petName").value.trim();
    const pwt = qs("#petWeight").value.trim();

    Object.assign(state, {
      ownerName: name,
      ownerPhone: phone,
      ownerEmail: email,
      ownerAddr: addr,
      petName: pnm,
      petWeight: pwt
    });

    const emailOK = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    const breedOK =
      state.petType === "other"
        ? !!state.petBreed
        : !!state.petType && !!state.petBreed;
    const matOK = state.materials.length > 0;
    const ok =
      name &&
      phone.length >= 10 &&
      emailOK &&
      addr &&
      pnm &&
      breedOK &&
      Number(pwt) >= 0 &&
      matOK;
    qs("#next1").disabled = !ok;
  }

  qsa("#step1 input").forEach(el =>
    el.addEventListener("input", validateStep1)
  );
  qsa("#step1 select").forEach(el =>
    el.addEventListener("change", validateStep1)
  );

  // ------------------------------
  // 5) 2단계 검증
  // ------------------------------
  const productEl = qs("#product");
  const chainRow = qs("#chainRow");
  const ringRow = qs("#ringRow");

  productEl.addEventListener("change", () => {
    state.product = productEl.value;
    chainRow.style.display = state.product === "necklace" ? "" : "none";
    ringRow.style.display = state.product === "ring" ? "" : "none";
    validateStep2();
  });

  qs("#metal").addEventListener("change", e => {
    state.metalColor = e.target.value;
    validateStep2();
  });
  qs("#chainLength").addEventListener("change", e => {
    state.chainLength = e.target.value;
    validateStep2();
  });
  qs("#ringSize").addEventListener("input", e => {
    state.ringSize = e.target.value.trim();
    validateStep2();
  });
  qs("#qty").addEventListener("input", e => {
    state.quantity = Math.max(1, parseInt(e.target.value || "1", 10));
    validateStep2();
  });
  qs("#engrave").addEventListener("input", e => {
    state.engravingText = e.target.value;
    validateStep2();
  });
  qs("#font").addEventListener("change", e => {
    state.engravingFont = e.target.value;
    validateStep2();
  });
  qs("#optMemo").addEventListener("input", e => {
    state.optionsMemo = e.target.value;
    qs("#optCount").textContent = e.target.value.length;
  });

  function validateStep2() {
    let ok = !!state.product && +state.quantity >= 1;
    if (state.product === "necklace") ok = ok && !!state.chainLength;
    if (state.product === "ring") ok = ok && !!state.ringSize;
    qs("#next2").disabled = !ok;
  }

  // ------------------------------
  // 6) 3단계 검증
  // ------------------------------
  qs("#shipMethod").addEventListener("change", e => {
    state.shipMethod = e.target.value;
    qs("#kitBox").style.display = state.shipMethod === "kit" ? "grid" : "none";
    qs("#visitBox").style.display = state.shipMethod === "visit" ? "grid" : "none";
    qs("#postBox").style.display = state.shipMethod === "post" ? "grid" : "none";
    validateStep3();
  });

  qs("#targetDate").addEventListener("change", e => {
    state.targetDate = e.target.value;
    validateStep3();
  });
  qs("#express").addEventListener("change", e => {
    state.isExpress = e.target.checked;
  });
  qs("#kitAddr").addEventListener("input", e => {
    state.kitAddr = e.target.value.trim();
    validateStep3();
  });
  qs("#kitDate").addEventListener("change", e => {
    state.kitDate = e.target.value;
    validateStep3();
  });
  qs("#visitDate").addEventListener("change", e => {
    state.visitDate = e.target.value;
    validateStep3();
  });
  qs("#visitTime").addEventListener("change", e => {
    state.visitTime = e.target.value;
    validateStep3();
  });
  qs("#tracking").addEventListener("input", e => {
    state.trackingInfo = e.target.value.trim();
  });
  qs("#memo").addEventListener("input", e => {
    state.memo = e.target.value;
    qs("#memoCount").textContent = e.target.value.length;
  });

  function validateStep3() {
    let ok = !!state.shipMethod && !!state.targetDate;
    if (state.shipMethod === "kit")
      ok = ok && !!state.kitAddr && !!state.kitDate;
    if (state.shipMethod === "visit")
      ok = ok && !!state.visitDate && !!state.visitTime;
    qs("#next3").disabled = !ok;
  }

  // ------------------------------
  // 7) 단계 이동
  // ------------------------------
  const goto = n => {
    qsa(".step").forEach((el, i) => el.classList.toggle("active", i === n - 1));
    qsa(".step-dot").forEach((d, i) => d.classList.toggle("active", i <= n - 1));
    window.scrollTo({ top: 0, behavior: "smooth" });
    if (n === 4) renderSummary();
  };

  qs("#next1").addEventListener("click", () => goto(2));
  qs("#prev2").addEventListener("click", () => goto(1));
  qs("#next2").addEventListener("click", () => goto(3));
  qs("#prev3").addEventListener("click", () => goto(2));
  qs("#next3").addEventListener("click", () => goto(4));
  qs("#prev4").addEventListener("click", () => goto(3));

  // ------------------------------
  // 8) 요약 렌더링
  // ------------------------------
  function renderSummary() {
    const matMap = { ash: "유골", hair: "모발", paw: "발자국", item: "유품" };
    const mats = state.materials.map(m => matMap[m] || m).join(", ");
    const s = qs("#summary");
    s.innerHTML = `
      <div><strong>신청자</strong> : ${state.ownerName} / ${state.ownerPhone} / ${state.ownerEmail}</div>
      <div><strong>주소</strong> : ${state.ownerAddr}</div>
      <div><strong>반려동물</strong> : ${state.petName} (${state.petType || "-"} / ${state.petBreed || "-"}, ${state.petWeight}kg)</div>
      <div><strong>보유 흔적</strong> : ${mats || "-"}</div>
      <div><strong>품목</strong> : ${state.product}</div>
      <div><strong>완성 희망일</strong> : ${state.targetDate} ${state.isExpress ? "(익스프레스 요청)" : ""}</div>
    `;
  }

  qs("#confirmChk").addEventListener("change", () => {
    qs("#submitBtn").disabled = !qs("#confirmChk").checked;
  });

  // ------------------------------
  // 9) 제출 (REST API)
  // ------------------------------
  qs("#submitBtn").addEventListener("click", async () => {
    let userSeq = document.body.dataset.userseq || localStorage.getItem("userSeq");
    if (!userSeq) {
      alert("로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.");
      window.location.href = "/signin";
      return;
    }
    state.userSeq = userSeq;

    const url = state.id ? `/reserve/goods_reserve/${state.id}` : "/reserve/save2";
    const method = state.id ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(state)
      });

      if (res.ok) {
        alert(state.id ? "예약이 수정되었습니다." : "예약이 등록되었습니다.");
        window.location.href = `/mypage/Mypage?userSeq=${userSeq}`;
      } else {
        console.error("서버 오류:", await res.text());
        alert("저장 중 오류가 발생했습니다.");
      }
    } catch (err) {
      console.error("통신 오류:", err);
      alert("서버 통신 중 오류가 발생했습니다.");
    }
  });

  // ------------------------------
  // 10) 수정 모드 데이터 로드
  // ------------------------------
  if (state.id) {
    fetch(`/api/goods_reserve/${state.id}`)
      .then(res => res.json())
      .then(data => {
        Object.assign(state, data);
        fillForm(data);
      })
      .catch(err => console.error("예약 정보 불러오기 실패:", err));
  }

  function fillForm(data) {
    qs("#ownerName").value = data.ownerName || "";
    qs("#ownerPhone").value = data.ownerPhone || "";
    qs("#ownerEmail").value = data.ownerEmail || "";
    qs("#ownerAddr").value = data.ownerAddr || "";
    qs("#petName").value = data.petName || "";
    qs("#petType").value = data.petType || "";
    petTypeEl.dispatchEvent(new Event("change"));
    if (data.petType === "other") qs("#petBreedText").value = data.petBreed || "";
    else qs("#petBreed").value = data.petBreed || "";
    qs("#petWeight").value = data.petWeight || "";
    (data.materials || []).forEach(mat => {
      qs(`#materialsBox .slot[data-mat="${mat}"]`)?.classList.add("active");
    });
    qs("#product").value = data.product || "";
    productEl.dispatchEvent(new Event("change"));
    qs("#metal").value = data.metalColor || "";
    qs("#chainLength").value = data.chainLength || "";
    qs("#ringSize").value = data.ringSize || "";
    qs("#qty").value = data.quantity || 1;
    qs("#engrave").value = data.engravingText || "";
    qs("#font").value = data.engravingFont || "";
    qs("#optMemo").value = data.optionsMemo || "";
    qs("#shipMethod").value = data.shipMethod || "";
    qs("#shipMethod").dispatchEvent(new Event("change"));
    qs("#targetDate").value = data.targetDate || "";
    qs("#express").checked = !!data.isExpress;
    qs("#kitAddr").value = data.kitAddr || "";
    qs("#kitDate").value = data.kitDate || "";
    qs("#visitDate").value = data.visitDate || "";
    qs("#visitTime").value = data.visitTime || "";
    qs("#tracking").value = data.trackingInfo || "";
    qs("#memo").value = data.memo || "";
    validateStep1();
    validateStep2();
    validateStep3();
  }

  qs("#ownerName").focus();
});
