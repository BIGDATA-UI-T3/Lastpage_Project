document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("qnaTableBody");
    const noResult = document.getElementById("noResult");

    loadQnaList();

    // ============================
    // 1. QnA 전체 조회
    // ============================
    function loadQnaList(keyword = "") {
        fetch(`/admin/qna/all`)
            .then(res => res.json())
            .then(list => {
                if (keyword) {
                    list = list.filter(q =>
                        (q.title?.includes(keyword)) ||
                        (q.content?.includes(keyword)) ||
                        (q.nickname?.includes(keyword))
                    );
                }

                tableBody.innerHTML = "";
                noResult.style.display = list.length === 0 ? "block" : "none";

                list.forEach(q => {
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>${q.category}</td>
                        <td>${q.title}</td>
                        <td>${q.nickname || "익명"}</td>
                        <td>${formatDate(q.createdAt)}</td>
                        <td>${q.adminAnswer ? "<span class='text-success fw-bold'>답변완료</span>" : "미답변"}</td>
                        <td>
                            <button class="btn btn-sm btn-dark" data-id="${q.id}" data-act="view">보기</button>
                        </td>
                    `;
                    tableBody.appendChild(tr);
                });
            });
    }

    // 검색 버튼
    document.getElementById("btnSearch").addEventListener("click", () => {
        const keyword = document.getElementById("searchKeyword").value.trim();
        loadQnaList(keyword);
    });

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
        fetch(`/admin/qna/${id}`)
            .then(res => res.json())
            .then(q => {
                document.getElementById("modalTitle").textContent = q.title;
                document.getElementById("modalCategory").textContent = q.category;
                document.getElementById("modalNickname").textContent = q.nickname;
                document.getElementById("modalCreatedAt").textContent = formatDate(q.createdAt);
                document.getElementById("modalContent").textContent = q.content;

                renderImages(q.images);
                renderLinks(q.links);
                renderAnswer(q);

                document.getElementById("btnDelete").onclick = () => deleteQna(q.id);

                const modal = new bootstrap.Modal(document.getElementById("qnaModal"));
                modal.show();
            });
    }

    function renderImages(list) {
        const wrap = document.getElementById("modalImagesWrap");
        if (!list || list.length === 0) {
            wrap.innerHTML = "";
            return;
        }
        wrap.innerHTML = `<label class="fw-bold">사진</label><div class="d-flex gap-2 mt-2">
            ${list.map(src => `<img src="${src}" style="width:100px;height:100px;border-radius:12px;object-fit:cover">`).join("")}
        </div>`;
    }

    function renderLinks(list) {
        const wrap = document.getElementById("modalLinksWrap");
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
    // 3. 답변 입력 UI
    // ============================
    function renderAnswer(q) {
        const wrap = document.getElementById("modalAnswerWrap");

        if (!q.adminAnswer) {
            wrap.innerHTML = `
                <label class="fw-bold">답변 작성</label>
                <textarea id="answerText" class="form-control mt-2" rows="5" placeholder="답변을 입력하세요"></textarea>
                <button class="btn btn-dark mt-3" id="btnAnswerSave">등록하기</button>
            `;
        } else {
            wrap.innerHTML = `
                <div class="answer-view">
                    <div>${q.adminAnswer}</div>
                    <div class="meta">답변자: ${q.adminName} · ${formatDate(q.answerAt)}</div>
                </div>

                <label class="fw-bold mt-3">답변 수정</label>
                <textarea id="answerText" class="form-control mt-2" rows="5">${q.adminAnswer}</textarea>
                <button class="btn btn-dark mt-3" id="btnAnswerSave">수정하기</button>
            `;
        }

        document.getElementById("btnAnswerSave").onclick = () =>
            saveAnswer(q.id);
    }

    // ============================
    // 4. 답변 저장
    // ============================
    function saveAnswer(id) {
        const text = document.getElementById("answerText").value.trim();
        if (!text) {
            alert("답변을 입력하세요");
            return;
        }

        fetch(`/admin/qna/answer/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ answer: text })
        })
            .then(res => res.ok ? alert("저장되었습니다.") : alert("오류 발생"))
            .then(() => loadQnaList());
    }

    // ============================
    // 5. 삭제
    // ============================
    function deleteQna(id) {
        if (!confirm("정말 삭제하시겠습니까?")) return;

        fetch(`/admin/qna/${id}`, { method: "DELETE" })
            .then(res => res.ok ? alert("삭제되었습니다.") : alert("오류 발생"))
            .then(() => {
                loadQnaList();
                bootstrap.Modal.getInstance(document.getElementById("qnaModal")).hide();
            });
    }

    function formatDate(d) {
        return new Date(d).toLocaleString("ko-KR", { hour12: false });
    }
});
