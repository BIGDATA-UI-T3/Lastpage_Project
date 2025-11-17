/* ==========================================
   공통 설정 / 유틸
========================================== */

async function fetchJson(url, options = {}) {
  const res = await fetch(url, options);
  if (!res.ok) {
    const msg = await res.text().catch(() => "");
    throw new Error(msg || "요청 처리 중 오류가 발생했습니다.");
  }
  try { return await res.json(); }
  catch { return null; }
}

const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

const formatTime = (d) =>
  new Date(d).toLocaleString("ko-KR", { hour12: false });

const nl2br = (str = "") => str.replace(/\n/g, "<br>");
const escapeHtml = (str = "") =>
  str.replace(/[&<>"']/g, (m) =>
    ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#39;",
    }[m])
  );

/* ==========================================
   서버 API 베이스
========================================== */

const API_QNA_BASE = "/supportService/api/qna";

/* 현재 로그인 유저 */
const LOGIN_USER_SEQ = document.body.dataset.userseq || null;

/* QnA 캐시 */
let qnaCache = new Map();
let editingId = null;
let editingPassword = null;
let selectedImages = [];

/* ==========================================
   LocalStorage 유틸 (FAQ 전용)
========================================== */
const LS = {
  get(key, fallback) {
    try {
      return JSON.parse(localStorage.getItem(key)) ?? fallback;
    } catch {
      return fallback;
    }
  },
  set(key, val) {
    localStorage.setItem(key, JSON.stringify(val));
  },
};

/* ==========================================
   기본 FAQ (fallback)
========================================== */
const defaultFaq = [
  { q:"반려동물 장례란 무엇인가요?", a:"A. 반려동물 장례는 가족과 같은 반려동물의 마지막 시간을 예의를 갖춰 보내는 절차입니다. 종합 상담, 운구, 작별실, 화장, 수골, 추모까지 안내해 드립니다." },
  { q:"사체는 어떻게 보관해야 하나요?", a:"A. 가능하면 2~3시간 내 상담을 권장하며, 바로 방문이 어려우면 시신이 마르지 않도록 깨끗한 수건으로 감싸고 아이스팩을 복부에 대 주세요." },
  { q:"개별 화장과 합동 화장의 차이는?", a:"A. 개별 화장은 반려동물 한 개체만 단독으로 진행하고 유골 전량을 돌려드립니다. 합동 화장은 여러 아이가 함께 진행되어 유골 반환이 없습니다." },
  { q:"유골함/기념품(굿즈) 구매가 가능한가요?", a:"A. 가능합니다. 다양한 사이즈와 소재의 유골함, 모발/발바닥 모양 프레임, 각인 서비스 등을 제공합니다." },
  { q:"심리상담은 어떻게 진행되나요?", a:"A. 상담사는 슬픔 단계 평가와 애도 과업을 기반으로 1:1 맞춤 세션을 제공합니다. 대면/비대면 모두 가능하며 예약제로 운영됩니다." },
  { q:"예약/접수는 24시간 되나요?", a:"A. 긴급 접수는 24시간 연락 가능하며, 야간에는 대기 시간이 발생할 수 있습니다." },
  { q:"주차와 접근성은 어떤가요?", a:"A. 장례식장마다 상이합니다. 예약한 장례식장에 직접문의하기 또는 사이트 내에서 장례식장 정보를 얻을 수 있도록 지도와 함께 링크가 걸려있어 확인 가능합니다." },
  { q:"유골은 어디에 안치할 수 있나요?", a:"A. 납골당/수목장/가정 안치 등 여러 형태를 안내해 드리며, 법규를 준수합니다." },
  { q:"비용은 어떻게 산정되나요?", a:"A. 체중, 선택 서비스(개별/합동, 염습 여부, 추모실 사용, 굿즈 등)에 따라 달라집니다." },
  { q:"카드결제/현금영수증 발급되나요?", a:"A. 모두 가능합니다. 현금영수증은 개인/사업자 구분하여 발급합니다." },
  { q:"유골을 아파트 화단에 뿌려도 되나요?", a:"A. 관할 지자체 규정과 공동주거 공간의 규약을 확인해야 합니다. 수목장을 권장드립니다." },
  { q:"추모공간 예약은 어떻게 하나요?", a:"A. 모바일 또는 전화로 예약 후 방문해 주시면 됩니다." }
];

/* LocalStorage FAQ 초기화 */
if (!LS.get("faqData")) {
  LS.set("faqData", defaultFaq);
}

/* ==========================================
   FAQ 로딩 (LocalStorage → default)
   ※ 서버 API 호출 제거됨
========================================== */
async function loadFaq() {
  const wrap = $("#faqList");
  wrap.innerHTML = "<div class='hint'>불러오는 중...</div>";

  // DB 조회 제거 → localStorage만 사용
  const local = LS.get("faqData", defaultFaq);
  renderFaq(local);
}

function renderFaq(list) {
  const wrap = $("#faqList");
  wrap.innerHTML = "";

  if (!list.length) {
    wrap.innerHTML = "<p class='hint'>FAQ가 없습니다.</p>";
    return;
  }

  list.forEach((item) => {
    const el = document.createElement("article");
    el.className = "item";

    el.innerHTML = `
      <div class="q">
        <div class="left">
          <span class="badge">Q</span>
          <span>${escapeHtml(item.q || item.question)}</span>
        </div>
        <span class="caret">▶</span>
      </div>
      <div class="a"><p>${nl2br(escapeHtml(item.a || item.answer))}</p></div>
    `;

    $(".q", el).addEventListener("click", () => el.classList.toggle("open"));
    wrap.appendChild(el);
  });
}



async function loadQnaList() {
  const filter = $("#qnaFilter").value;
  const listWrap = $("#qnaList");

  try {
    // 검색 제거 — category 필터만 전달
     //  디버깅 로그
        console.log("[loadQnaList] 요청:", `${API_QNA_BASE}?category=${filter}`);

        const list = await fetchJson(`${API_QNA_BASE}?category=${filter}`);

        //  응답이 뭔지 확인
        console.log("[loadQnaList] 응답:", list);

    qnaCache = new Map();
    list.forEach((q) => qnaCache.set(q.id, q));

    renderQnaList(list);
  } catch (e) {
    listWrap.innerHTML = `<div class="hint">문의 목록을 가져올 수 없습니다.</div>`;
  }
}



function renderQnaList(list) {
  const wrap = $("#qnaList");
  wrap.innerHTML = "";

  if (!list.length) {
    wrap.innerHTML = `<div class="hint">등록된 문의가 없습니다.</div>`;
    return;
  }

  list.forEach((post) => {
    const locked = post.secret === true;

    // null-safe 답변 여부 체크 (핵심 수정)
    const answered = (post.adminAnswer ?? "").trim() !== "";

    const el = document.createElement("article");
    el.className = "post";
    el.dataset.id = post.id;

    el.innerHTML = `
      <div class="post-head">
        <div class="row">
          <span class="tag">${escapeHtml(post.category)}</span>
          <span class="post-title">
            ${locked ? "[비공개] " : ""}${escapeHtml(post.title)}
          </span>
        </div>
        <div class="meta">
          ${escapeHtml(post.nickname)}
          · ${formatTime(post.createdAt)}
          ${answered ? " · <span style='color:#0d7a43;'>[답변완료]</span>" : ""}
        </div>
      </div>

      <div class="post-body">
        <div class="post-content">
          ${
            locked
              ? "<em>비공개 글입니다. 비밀번호가 필요합니다.</em>"
              : nl2br(escapeHtml(post.content))
          }
        </div>

        ${
          post.images?.length
            ? `<div class="files">${post.images
                .map((src) => `<img class="preview" src="${src}">`)
                .join("")}</div>`
            : ""
        }

        ${
          post.links?.length
            ? `<div class="grid">${post.links
                .map(
                  (u) =>
                    `<a href="${u}" target="_blank">${escapeHtml(u)}</a>`
                )
                .join("")}</div>`
            : ""
        }

        <div class="toolbar">
          ${locked ? '<button data-act="view-secret">비공개 글 보기</button>' : ""}
          <button data-act="edit">수정</button>
          <button class="danger" data-act="delete">삭제</button>
        </div>

        <div class="admin-answer" ${answered ? "" : "hidden"}>
          <div class="tag">관리자 답변</div>
          <div>${nl2br(escapeHtml(post.adminAnswer ?? ""))}</div>
          <div class="meta">by ${escapeHtml(post.adminName ?? "")} · ${post.answerAt ? formatTime(post.answerAt) : ""}</div>
        </div>
      </div>
    `;

    $(".post-head", el).addEventListener("click", () => el.classList.toggle("open"));

    el.addEventListener("click", (e) => {
      const btn = e.target.closest("button");
      if (!btn) return;

      const postData = qnaCache.get(post.id);
      if (!postData) return;

      const act = btn.dataset.act;

      if (act === "view-secret") handleViewSecret(el, postData);
      else if (act === "edit") handleEdit(postData);
      else if (act === "delete") handleDelete(postData);

      e.stopPropagation();
    });

    wrap.appendChild(el);
  });
}


/* ==========================================
   비공개 글 보기
========================================== */
async function handleViewSecret(el, post) {
  const pw = prompt("비밀번호 입력:");
  if (!pw) return;

  try {
    const ok = await fetchJson(`${API_QNA_BASE}/check/${post.id}?password=${pw}`);
    if (!ok) return alert("비밀번호가 일치하지 않습니다.");

    $(".post-content", el).innerHTML = nl2br(escapeHtml(post.content));
    el.querySelector('[data-act="view-secret"]')?.remove();
  } catch {
    alert("오류가 발생했습니다.");
  }
}

/* ==========================================
   수정
========================================== */
async function handleEdit(post) {
  if (!LOGIN_USER_SEQ) return alert("로그인이 필요합니다.");
  const pw = prompt("수정 비밀번호:");

  const ok = await fetchJson(`${API_QNA_BASE}/check/${post.id}?password=${pw}`);
  if (!ok) return alert("비밀번호가 다릅니다.");

  editingId = post.id;
  editingPassword = pw;

  openAskModal(post);
}

/* ==========================================
   삭제
========================================== */
async function handleDelete(post) {
  const pw = prompt("삭제 비밀번호:");
  if (!pw) return;

  const ok = await fetchJson(`${API_QNA_BASE}/check/${post.id}?password=${pw}`);
  if (!ok) return alert("비밀번호가 다릅니다.");

  if (!confirm("정말 삭제할까요?")) return;

  await fetch(`${API_QNA_BASE}/${post.id}?password=${pw}`, { method: "DELETE" });

  alert("삭제 완료");
  loadQnaList();
}

/* ==========================================
   모달 처리 (작성/수정)
========================================== */
const askModal = $("#askModal");
const askForm = $("#askForm");
const photosInput = $("#photos");
const previewsBox = $("#previews");
const linkBox = $("#linkBox");
const writerPassInput = $("#writerPass");

$("#btnAsk").addEventListener("click", () => {
  if (!LOGIN_USER_SEQ) return alert("로그인 후 이용해주세요.");
  openAskModal(null);
});

$("#btnCloseAsk").addEventListener("click", closeAskModal);

function openAskModal(post=null) {
  askModal.classList.add("open");
  askModal.setAttribute("aria-hidden", "false");

  selectedImages = [];
  previewsBox.innerHTML = "";
  linkBox.innerHTML = "";

  if (!post) {
    editingId = null;
    editingPassword = null;
    askForm.reset();
    addLinkField();
  } else {
    $("#title").value = post.title;
    $("#category").value = post.category;
    $("#nickname").value = post.nickname;
    $("#content").value = post.content;
    $("#secret").checked = post.secret;

    writerPassInput.value = "";
    writerPassInput.placeholder = "기존 비밀번호 유지";

    const links = post.links || [];
    if (!links.length) addLinkField();
    else links.forEach((u) => addLinkField(u));

    const imgs = post.images || [];
    selectedImages = [...imgs];
    imgs.forEach((src) => {
      const img = document.createElement("img");
      img.src = src;
      img.className = "preview";
      previewsBox.appendChild(img);
    });
  }
}

function closeAskModal() {
  askModal.classList.remove("open");
  askModal.setAttribute("aria-hidden", "true");
}

$("#btnAddLink").addEventListener("click", () => addLinkField());

function addLinkField(value = "") {
  const group = document.createElement("div");
  group.className = "field-inline";
  group.innerHTML = `
    <input type="url" value="${value}" placeholder="https:// 링크 입력" />
    <button type="button" class="btn-ghost">삭제</button>
  `;
  group.querySelector("button").addEventListener("click", () => group.remove());
  linkBox.appendChild(group);
}

/*  여기부터 사진 base64 처리  */
photosInput.addEventListener("change", (e) => {
  const files = [...e.target.files].slice(0, 3);

  selectedImages = [];
  previewsBox.innerHTML = "";

  files.forEach((file) => {
    if (file.size > 2 * 1024 * 1024) {
      alert(`${file.name} (2MB 초과) 제외`);
      return;
    }

    const reader = new FileReader();
    reader.onload = (ev) => {
      // ev.target.result = data:image/png;base64,.... 형태
      selectedImages.push(ev.target.result);

      const img = document.createElement("img");
      img.src = ev.target.result;
      img.className = "preview";
      previewsBox.appendChild(img);
    };
    reader.readAsDataURL(file);
  });
});




/* ==========================================
   제출 (등록/수정)
========================================== */
askForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  if (!LOGIN_USER_SEQ) return alert("로그인 필요");

  const dto = {
    id: editingId,
    userSeq: LOGIN_USER_SEQ,
    nickname: $("#nickname").value.trim(),
    writerPass: editingId ? editingPassword : $("#writerPass").value.trim(),
    title: $("#title").value.trim(),
    content: $("#content").value.trim(),
    category: $("#category").value,
    secret: $("#secret").checked,
    images: selectedImages,
    links: $$("#linkBox input").map((i) => i.value.trim()).filter(Boolean),
  };

  if (!dto.title || !dto.content) return alert("제목/내용 입력 필수");
  if (!editingId && dto.writerPass.length < 4) return alert("비밀번호 4자 이상");

  try {
    const method = editingId ? "PUT" : "POST";

    await fetchJson(API_QNA_BASE, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto),
    });

    alert(editingId ? "수정 완료" : "등록 완료");
    closeAskModal();
    loadQnaList();
  } catch (err) {
    alert(err.message || "오류 발생");
  }
});
/* ==========================================
   탭 전환
========================================== */
$("#tab-faq").addEventListener("click", () => {
  $("#faqSection").hidden = false;
  $("#qnaSection").hidden = true;
  $("#tab-faq").classList.add("active");
  $("#tab-qna").classList.remove("active");
});

$("#tab-qna").addEventListener("click", () => {
  $("#faqSection").hidden = true;
  $("#qnaSection").hidden = false;
  $("#tab-faq").classList.remove("active");
  $("#tab-qna").classList.add("active");

  // ★ QnA 탭 열릴 때 목록 로딩
  loadQnaList();
});

/* ==========================================
   QnA 필터 / 새로고침
========================================== */
$("#qnaFilter").addEventListener("change", loadQnaList);

$("#btnQnaRefresh").addEventListener("click", () => {
  $("#qnaFilter").value = "ALL";
  loadQnaList();
});







//$("#qnaSearch").addEventListener("keydown", (e) => {
//  if (e.key === "Enter") loadQnaList();
//});

/* ==========================================
   초기 실행
========================================== */
loadFaq();
