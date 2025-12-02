// /static/js/adminQna.js

document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("qnaTableBody");
    const noResult = document.getElementById("noResult");

    // ===== API 경로 정리 =====
    const API_ADMIN_QNA_LIST   = "/supportService/api/admin/qna";
    const API_QNA_DETAIL       = "/supportService/api/qna";
    const API_ADMIN_QNA_DELETE = "/supportService/api/admin/qna";
    const API_ADMIN_QNA_ANSWER = "/admin/qna/answer";  // 기존 경로 유지

    // 초기 로딩
    loadQnaList();

    // ============================
    // 0. 삭제 버튼(Event Delegation)
    // ============================
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-delete");
        if (!btn) return;

        const id = btn.dataset.id;
        deleteQna(id);
    });

    // ============================
    // (A) Summary 숫자 즉시 갱신 기능 추가
    // ============================
    function updateSummary(list) {
        const totalEl = document.querySelector(".summary-card:nth-child(1) .summary-number");
        const waitEl  = document.querySelector(".summary-card:nth-child(2) .summary-number");
        const doneEl  = document.querySelector(".summary-card:nth-child(3) .summary-number");

        const total = list.length;
        const wait = list.filter(q => !q.adminAnswer || q.adminAnswer.trim() === "").length;
        const done = list.filter(q => q.adminAnswer && q.adminAnswer.trim() !== "").length;

        if (totalEl) totalEl.textContent = total;
        if (waitEl)  waitEl.textContent = wait;
        if (doneEl)  doneEl.textContent = done;
    }

    // ============================
    // 1. QnA 전체 조회 + summary 갱신
    // ============================
    function loadQnaList(keyword = "") {
        fetch(API_ADMIN_QNA_LIST)
            .then(res => {
                if (!res.ok) throw new Error("목록 조회 실패");
                return res.json();
            })
            .then(list => {
                // 검색어 필터
                if (keyword) {
                    list = list.filter(q =>
                        (q.title?.includes(keyword)) ||
                        (q.content?.includes(keyword)) ||
                        (q.nickname?.includes(keyword))
                    );
                }

                // ===== Summary UI 즉시 반영 =====
                updateSummary(list);

                tableBody.innerHTML = "";

                if (!list || list.length === 0) {
                    if (noResult) noResult.style.display = "table-row";
                    return;
                } else {
                    if (noResult) noResult.style.display = "none";
                }

                list.forEach((q, idx) => {
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td class="col-number">${idx + 1}</td>

                        <td class="col-category">
                            <span class="tag">${q.category}</span>
                        </td>

                        <td class="col-title">
                            ${q.secret ? "[비공개] " : ""}${q.title}
                        </td>

                        <td class="col-nickname">${q.nickname || "익명"}</td>

                        <td class="col-date">${formatDate(q.createdAt)}</td>

                        <td class="col-status">
                            <span class="badge-status ${q.adminAnswer ? "badge-done" : "badge-wait"}">
                                ${q.adminAnswer ? "답변완료" : "미답변"}
                            </span>
                        </td>

                        <td class="col-answer">
                            <a class="btn-answer btn btn-sm btn-primary"
                               href="/supportService/admin/qna/${q.id}">
                                ${q.adminAnswer ? "수정하기" : "답변하기"}
                            </a>
                        </td>

                        <td class="col-delete">
                            <a class="btn-delete btn btn-sm btn-outline-danger"
                                    data-id="${q.id}">
                                삭제
                            </a>
                        </td>
                    `;
                    tableBody.appendChild(tr);
                });

            })
            .catch(err => {
                console.error(err);
                alert("문의 목록을 불러오는 중 오류가 발생했습니다.");
            });
    }

    // ============================
    // 검색 기능
    // ============================
    const btnSearch = document.getElementById("btnSearch");
    if (btnSearch) {
        btnSearch.addEventListener("click", () => {
            const keyword = document.getElementById("searchKeyword").value.trim();
            loadQnaList(keyword);
        });
    }

    // ============================
    // 2. 상세 모달 열기
    // ============================
    tableBody.addEventListener("click", e => {
        const btn = e.target.closest("button[data-act='view']");
        if (!btn) return;

        const id = btn.dataset.id;
        openModal(id);
    });

    function openModal(id) {
        fetch(`${API_QNA_DETAIL}/${id}`)
            .then(res => {
                if (!res.ok) throw new Error("상세 조회 실패");
                return res.json();
            })
            .then(q => {
                document.getElementById("modalTitle").textContent     = q.title;
                document.getElementById("modalCategory").textContent  = q.category;
                document.getElementById("modalNickname").textContent  = q.nickname;
                document.getElementById("modalCreatedAt").textContent = formatDate(q.createdAt);
                document.getElementById("modalContent").textContent   = q.content;

                renderImages(q.images);
                renderLinks(q.links);
                renderAnswer(q);

                const btnDelete = document.getElementById("btnDelete");
                if (btnDelete) btnDelete.onclick = () => deleteQna(q.id);

                const modal = new bootstrap.Modal(document.getElementById("qnaModal"));
                modal.show();
            })
            .catch(err => {
                console.error(err);
                alert("상세 내용을 불러오는 중 오류가 발생했습니다.");
            });
    }

    function renderImages(list) {
        const wrap = document.getElementById("modalImagesWrap");
        if (!wrap) return;

        if (!list || list.length === 0) {
            wrap.innerHTML = "";
            return;
        }
        wrap.innerHTML = `
            <label class="fw-bold">사진</label>
            <div class="d-flex gap-2 mt-2">
                ${list.map(src => `
                    <img src="${src}"
                         style="width:100px;height:100px;border-radius:12px;object-fit:cover">
                `).join("")}
            </div>
        `;
    }

    function renderLinks(list) {
        const wrap = document.getElementById("modalLinksWrap");
        if (!wrap) return;

        if (!list || list.length === 0) {
            wrap.innerHTML = "";
            return;
        }
        wrap.innerHTML = `
            <label class="fw-bold">링크</label>
            <ul class="mt-2">
                ${list.map(u => `<li><a href="${u}" target="_blank">${u}</a></li>`).join("")}
            </ul>
        `;
    }

    // ============================
    // 3. 답변 입력 / 수정 UI
    // ============================
    function renderAnswer(q) {
        const wrap = document.getElementById("modalAnswerWrap");
        if (!wrap) return;

        if (!q.adminAnswer) {
            wrap.innerHTML = `
                <label class="fw-bold">답변 작성</label>
                <textarea id="answerText" class="form-control mt-2" rows="5"
                          placeholder="답변을 입력하세요"></textarea>
                <button class="btn btn-dark mt-3" id="btnAnswerSave">등록하기</button>
            `;
        } else {
            wrap.innerHTML = `
                <div class="answer-view">
                    <div>${q.adminAnswer}</div>
                    <div class="meta">답변자: ${q.adminName || ""} · ${q.answerAt ? formatDate(q.answerAt) : ""}</div>
                </div>

                <label class="fw-bold mt-3">답변 수정</label>
                <textarea id="answerText" class="form-control mt-2" rows="5">${q.adminAnswer}</textarea>
                <button class="btn btn-dark mt-3" id="btnAnswerSave">수정하기</button>
            `;
        }

        const btnSave = document.getElementById("btnAnswerSave");
        if (btnSave) btnSave.onclick = () => saveAnswer(q.id);
    }

    // ============================
    // 4. 답변 저장 + summary 즉시 반영
    // ============================
    function saveAnswer(id) {
        const textEl = document.getElementById("answerText");
        if (!textEl) return;

        const text = textEl.value.trim();
        if (!text) {
            alert("답변을 입력하세요");
            return;
        }

        fetch(`${API_ADMIN_QNA_ANSWER}/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ answer: text })
        })
            .then(res => res.ok ? alert("저장되었습니다.") : alert("오류 발생"))
            .then(() => loadQnaList())
            .catch(err => {
                console.error(err);
                alert("답변 저장 중 오류가 발생했습니다.");
            });
    }

    // ============================
    // 5. 삭제 + Summary 즉시 갱신
    // ============================
    function deleteQna(id) {
        if (!confirm("정말 삭제하시겠습니까?")) return;

        fetch(`${API_ADMIN_QNA_DELETE}/${id}`, {
            method: "DELETE"
        })
            .then(res => {
                if (res.ok) alert("삭제되었습니다.");
                else alert("오류 발생");
            })
            .then(() => {
                loadQnaList(); // 즉시 갱신

                const modalEl = document.getElementById("qnaModal");
                if (modalEl) {
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();
                }
            })
            .catch(err => {
                console.error(err);
                alert("삭제 중 오류가 발생했습니다.");
            });
    }

    // ============================
    // 공통 날짜 포맷
    // ============================
    function formatDate(d) {
        if (!d) return "";
        return new Date(d).toLocaleString("ko-KR", { hour12: false });
    }
});
