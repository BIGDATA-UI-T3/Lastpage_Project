// ===========================================
// 커뮤니티 스크립트 (카테고리 + 좋아요 + 팔로우 통합)
// ===========================================
document.addEventListener("DOMContentLoaded", function () {

  // ---------- 요소 선언 ----------
  const writeBtn = document.getElementById("write-btn");
  const myPostsBtn = document.getElementById("my-posts-view-btn");
  const scrollToTopBtn = document.getElementById("scrollToTopBtn");
  const writeFormBackBtn = document.getElementById("write-form-back-btn");
  const profileBackBtn = document.getElementById("profile-back-btn");

  const feedContainer = document.getElementById("feed-container");
  const writeContainer = document.getElementById("write-container");
  const myPostsContainer = document.getElementById("my-posts-container");
  const profileSidebar = document.querySelector(".profile-sidebar");

  const postForm = document.getElementById("post-form");
  const postFeed = document.querySelector(".post-feed");
  const postGrid = document.querySelector(".post-grid-container");

  const commentModal = document.getElementById("comment-modal");
  const commentListEl = document.querySelector(".comment-list");
  const noCommentMsgEl = document.querySelector(".no-comment-message");
  const commentInput = document.getElementById("comment-input");
  const commentSubmitBtn = document.getElementById("comment-submit-btn");
  const commentModalCloseBtn = document.querySelector(".comment-modal-close");
  const commentModalOverlay = document.querySelector(".comment-modal-overlay");

  let currentPostIdForComment = null;  // 현재 댓글 보는 게시글 ID
  // 로그인한 사용자 username을 전역에 저장해서 팔로우 버튼에서 사용
  window.currentUsername = null;
  window.currentUserUsername = null;
  window.currentCategory = "ALL"; // 현재 선택된 카테고리 상태

  new Swiper(".banner_yj", {
            loop: true,
            autoplay: {
                delay: 3000,
                disableOnInteraction: false,
            },
            speed: 800,
            effect: "fade",
            fadeEffect: {
                crossFade: true,
            }
  });

  // ---------- 화면 전환 ----------
  function showView(viewToShow) {
    if (!feedContainer || !writeContainer || !myPostsContainer) return;

    feedContainer.style.display = "none";
    writeContainer.style.display = "none";
    myPostsContainer.style.display = "none";

    viewToShow.style.display = "block";
    window.scrollTo(0, 0);

    // 내 프로필에서 사이드바 숨김
    if (profileSidebar) {
      profileSidebar.style.display = viewToShow === myPostsContainer ? "none" : "block";
    }
  }

  // ---------- 스크롤 Top 버튼 ----------
  window.addEventListener("scroll", () => {
    if (!scrollToTopBtn) return;
    const show = window.scrollY > 300;
    scrollToTopBtn.style.display = show ? "flex" : "none";
  });

  scrollToTopBtn?.addEventListener("click", () =>
    window.scrollTo({ top: 0, behavior: "smooth" })
  );

  // ---------- 비로그인 프로필 ----------
  function applyGuestProfile() {
    const nameEl = document.getElementById("profile-name");
    const userIdEl = document.getElementById("profile-userid");
    const bioEl = document.getElementById("profile-bio");
    const statsEl = document.getElementById("profile-stats");
    const signinBtn = document.getElementById("signin-btn");
    const signupBtn = document.getElementById("signup-btn");

    if (nameEl) nameEl.textContent = "Guest";
    if (userIdEl) userIdEl.textContent = "";
    if (bioEl) bioEl.textContent = "로그인 후 커뮤니티 서비스를 이용해보세요";
    if (statsEl) statsEl.style.display = "none";

    if (myPostsBtn) myPostsBtn.style.display = "none";
    if (signinBtn) signinBtn.style.display = "block";
    if (signupBtn) signupBtn.style.display = "block";

    window.currentUsername = null;
    window.currentUserUsername = null;
  }

  // ---------- 로그인 프로필 적용 ----------
  function applyUserProfile(data) {

      // ==========================
      // 1) 사이드바 정보 반영
      // ==========================
      const nameEl = document.getElementById("profile-name");
      const userIdEl = document.getElementById("profile-userid");
      const bioEl = document.getElementById("profile-bio");
      const statsEl = document.getElementById("profile-stats");
      const signinBtn = document.getElementById("signin-btn");
      const signupBtn = document.getElementById("signup-btn");

      if (nameEl) nameEl.textContent = data.name || "";
      if (userIdEl) userIdEl.textContent = "@" + (data.username || "");
      if (bioEl) bioEl.textContent = data.bio || "나를 소개하는 글을 남겨보세요.";
      if (statsEl) statsEl.style.display = "flex";

      if (myPostsBtn) myPostsBtn.style.display = "block";
      if (signinBtn) signinBtn.style.display = "none";
      if (signupBtn) signupBtn.style.display = "none";

      window.currentUsername = data.username || null;
      window.currentUserUsername = data.username || null;


      // ==========================
      // 2) 사이드바 프로필 이미지 (img 태그)
      // ==========================
      const sidebarAvatar = document.getElementById("sidebar-profile-avatar");
      if (sidebarAvatar) {
          sidebarAvatar.src = data.profileImage || "/Asset/default-avatar.svg";
      }


      // ==========================
      // 3) 내 프로필 페이지 이미지 (img 태그)
      // ==========================
      const myProfileAvatar = document.getElementById("my-profile-avatar");
      if (myProfileAvatar) {
          myProfileAvatar.src = data.profileImage || "/Asset/default-avatar.svg";
      }

      // 이름 / 소개글
      const myNameEl = document.getElementById("my-profile-name");
      const myBioEl = document.getElementById("my-profile-bio");
      const myUserIdEl = document.getElementById("my-profile-userid");

      if (myNameEl) myNameEl.textContent = data.name || "";
      if (myBioEl) myBioEl.textContent = data.bio || "";
      if (myUserIdEl) myUserIdEl.textContent = "@" + (data.username || "");

      // 내 글 개수
      fetch("/api/community/my")
        .then(res => res.json())
        .then(posts => {
          const postCountEl = document.getElementById("post-count");
          const myPostCountEl = document.getElementById("my-post-count");

          if (postCountEl) postCountEl.textContent = posts.length;
          if (myPostCountEl) myPostCountEl.textContent = posts.length;
        })
        .catch(() => {});

      // ==========================
      // 4) 팔로워·팔로잉 숫자
      // ==========================
      const followerCountEl = document.getElementById("follower-count");
      const followingCountEl = document.getElementById("following-count");
      const myFollowerEl = document.getElementById("my-follower-count");
      const myFollowingEl = document.getElementById("my-following-count");

      if (typeof data.followerCount === "number") {
          if (followerCountEl) followerCountEl.textContent = data.followerCount;
          if (myFollowerEl) myFollowerEl.textContent = data.followerCount;
      }
      if (typeof data.followingCount === "number") {
          if (followingCountEl) followingCountEl.textContent = data.followingCount;
          if (myFollowingEl) myFollowingEl.textContent = data.followingCount;
      }
  }

  // -----------------------------
  // 프로필 팔로워 숫자 갱신
  // -----------------------------
  async function refreshProfileStats() {
    try {
      const res = await fetch("/api/user/profile");
      if (!res.ok) return;
      const data = await res.json();

      const followerCountEl = document.getElementById("follower-count");
      const followingCountEl = document.getElementById("following-count");

      if (typeof data.followerCount === "number" && followerCountEl) {
        followerCountEl.textContent = data.followerCount;
      }
      if (typeof data.followingCount === "number" && followingCountEl) {
        followingCountEl.textContent = data.followingCount;
      }
    } catch (err) {
      console.error("프로필 통계 갱신 실패:", err);
    }
  }

  // ---------- 게시글 카드 렌더 ----------
  function renderPostCard(post) {
    const article = document.createElement("article");
    article.className = "feed-post-card effect_yj";
    article.dataset.id = post.id;

    const writerName = post.writer?.name || "익명";
    const writerUsername = post.writer?.username;
    const isMe = window.currentUserUsername === writerUsername;

    const isFollowing = post.writer?.isFollowing || false;

    let followButtonHtml = "";
    if (window.isLogin && writerUsername && !isMe) {
      const btnClass = isFollowing ? "follow-btn following" : "follow-btn";
      const btnText = isFollowing ? "팔로우 취소" : "팔로우";

      followButtonHtml = `
        <button class="${btnClass}" data-target="${writerUsername}">
          ${btnText}
        </button>
      `;
    }

    const likedClass = post.liked ? "liked" : "";

    article.innerHTML = `
      <div class="post-header" style="display:flex; justify-content:space-between; align-items:center;">
        <div style="display:flex; align-items:center;">
          <img src="${post.writer.profileImage || '/Asset/default-avatar.svg'}"
               class="author-avatar">
          <span class="author-name">${writerName}</span>
        </div>
        ${followButtonHtml}
      </div>

      <div class="post-image-wrapper">
        <img src="${post.imageUrl}" alt="photo">
      </div>

      <div class="post-actions">
        <svg class="like-btn ${likedClass}" fill="currentColor" viewBox="0 0 24 24">
          <path pointer-events="none"
                d="M12 21.35l-1.45-1.32C5.4
                   15.36 2 12.28 2 8.5 2 5.42 4.42 3
                   7.5 3c1.74 0 3.41.81 4.5
                   2.09C13.09 3.81 14.76 3
                   16.5 3 19.58 3 22 5.42
                   22 8.5c0 3.78-3.4 6.86-8.55
                   11.54L12 21.35z"/>
        </svg>

        <div class="comment-wrapper">
             <svg class="comment-btn" viewBox="0 0 24 24" fill="currentColor">
             <path d="M21.99 4c0-1.1-.89-2-1.99-2H4
                      c-1.1 0-2 .9-2 2v12c0 1.1.9
                      2 2 2h14l4 4-.01-18zM18
                      14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
             </svg>
             <span class="comment-count-badge">${post.commentCount || 0}</span>
        </div>
      </div>

      <div class="post-details">
        <div class="like-count">좋아요 ${post.likeCount || 0}개</div>
        <div class="post-caption">
          <span class="author-name">${writerName}</span>
          ${post.content || ""}
        </div>
      </div>
    `;
    return article;
  }

  // ---------- 카테고리 기반 피드 로딩 ----------
  function loadFeed(category = window.currentCategory) {
    const safeCategory = encodeURIComponent(category || "ALL");

    fetch(`/api/community/list?category=${safeCategory}`)
      .then(res => res.json())
      .then(posts => {
        postFeed.innerHTML = "";
        posts.forEach(p => postFeed.appendChild(renderPostCard(p)));

        observeFeedCards();
      })
      .catch(err => console.error("피드 불러오기 실패:", err));
  }

  // ---------- 로그인 여부 확인 후 프로필 + 피드 로딩 ----------
  fetch("/api/user/profile")
    .then(res => res.json())
    .then(data => {
      window.isLogin = data?.login === true;

      if (!window.isLogin) {
        applyGuestProfile();
      } else {
        applyUserProfile(data);
      }

      // username 설정 후 피드 로드
      loadFeed(window.currentCategory);
    })
    .catch(err => {
      console.error(err);
      window.isLogin = false;
      applyGuestProfile();
      loadFeed(window.currentCategory);
    });

  // ---------- 로그인 / 회원가입 버튼 ----------
  document.getElementById("signin-btn")?.addEventListener("click", () => (location.href = "/signin"));
  document.getElementById("signup-btn")?.addEventListener("click", () => (location.href = "/signup"));

  // ---------- 버튼 이벤트 ----------
  writeBtn?.addEventListener("click", () => {
    if (!window.isLogin) {
      if (confirm("로그인 후 이용 가능합니다.\n로그인 페이지로 이동할까요?")) {
        location.href = "/signin";
      }
      return;
    }
    showView(writeContainer);
  });

  writeFormBackBtn?.addEventListener("click", () => {
      window.currentCategory = "ALL";
      document.querySelectorAll(".category-btn").forEach(b => b.classList.remove("active"));
      document.querySelector('[data-category="ALL"]')?.classList.add("active");

      showView(feedContainer);
      loadFeed("ALL");
  });
  profileBackBtn?.addEventListener("click", () => showView(feedContainer));

  // ==================================================
  // 내 게시글 보기
  // ==================================================
  myPostsBtn?.addEventListener("click", async () => {

    window.currentCategory = "ALL";
    document.querySelectorAll(".category-btn").forEach(b => b.classList.remove("active"));
    document.querySelector('[data-category="ALL"]')?.classList.add("active");

    showView(myPostsContainer);


    const grid = document.querySelector(".post-grid-container");
    if (!grid) return;

    grid.innerHTML = "";

    try {
      const res = await fetch("/api/community/my");
      if (!res.ok) {
        alert("로그인이 필요합니다.");
        return;
      }

      const posts = await res.json();

      if (posts.length === 0) {
        grid.innerHTML = `
          <p style="grid-column: span 3; text-align:center; color:#666;">
            아직 작성한 게시글이 없습니다.
          </p>
        `;
        return;
      }

      posts.forEach(post => {
        const item = document.createElement("div");
        item.className = "grid-item";
        item.dataset.id = post.id;
        item.innerHTML = `<img src="${post.imageUrl}" alt="my post" />`;
        grid.appendChild(item);
      });
    } catch (err) {
      console.error("내 게시글 로드 실패:", err);
    }
  });

    // ---------- 글 등록 + 수정 ----------
    postForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!window.isLogin) {
      alert("로그인 후 이용 가능합니다.");
      location.href = "/signin";
      return;
    }

    const postId = document.getElementById("editing-post-id")?.value;
    const content = document.getElementById("content").value.trim();
    const image = document.getElementById("image").files[0];
    const category = document.getElementById("post-category")?.value || "ETC";

    const formData = new FormData();
    formData.append("content", content || "");
    formData.append("category", category);

    if (!postId) {
      // 등록
      if (!image) {
        alert("사진을 첨부해야 합니다.");
        return;
      }
      formData.append("image", image);
    } else {
      // 수정
      if (image) {
        formData.append("image", image);
      }
    }

    if (content.length > 50) {
        alert("글자 수는 최대 50자까지 입력할 수 있습니다.");
        return;
    }

    try {
      let url = "/api/community/save";
      let method = "POST";

      if (postId) {
        url = `/api/community/update/${postId}`;
        method = "PUT";
      }

      const res = await fetch(url, {
        method: method,
        body: formData,
      });

      const result = await res.json().catch(() => null);

      if (!res.ok) {
        alert(result?.message || "오류가 발생했습니다.");
        return;
      }

      if (postId) {
        alert("게시글이 수정되었습니다!");
      } else {
        alert("게시글이 등록되었습니다!");
        if (postFeed && result) {
          postFeed.prepend(renderPostCard(result));
        }
      }

      // 폼 초기화
      postForm.reset();
      const idInput = document.getElementById("editing-post-id");
      if (idInput) idInput.remove();

      const preview = document.getElementById("image-preview-area");
      if (preview) preview.innerHTML = "";

      // 피드로 전환 + 현재 카테고리 기준 재조회
      showView(feedContainer);
      loadFeed(window.currentCategory);

    } catch (err) {
      console.error("업로드 오류:", err);
      alert("서버 오류: " + err.message);
    }
  });

    // === 글자수 카운터(전역에서 한 번만 등록) ===
    const textarea = document.getElementById("content");
    const counter = document.getElementById("content-count");

    if (textarea && counter) {
      textarea.addEventListener("input", () => {
        counter.textContent = `${textarea.value.length} / 50`;
      });
    }

  // ---------- 카테고리 버튼 클릭 (전체/장례/굿즈/상담/기타) ----------
  document.addEventListener("click", (e) => {
    const btn = e.target.closest(".category-btn");
    if (!btn) return;

    document.querySelectorAll(".category-btn").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");

    window.currentCategory = btn.dataset.category || "ALL";

    loadFeed(window.currentCategory);
  });

  // ===============================
  // 내가 쓴 글 클릭 → 수정 모드
  // ===============================
  postGrid?.addEventListener("click", async (e) => {
    const item = e.target.closest(".grid-item");
    if (!item) return;

    const postId = item.dataset.id;
    const res = await fetch(`/api/community/detail/${postId}`);
    const post = await res.json();

    showView(writeContainer);

    const contentEl = document.getElementById("content");
    if (contentEl) contentEl.value = post.content || "";

    const fileDisplay = document.getElementById("file-name-display");
    if (fileDisplay && post.imageUrl) {
      const origName = post.imageUrl.split("/").pop();
      fileDisplay.textContent = "현재 등록된 파일: " + origName;
    }

    const preview = document.getElementById("image-preview-area");
    if (preview) preview.innerHTML = "";

    // 카테고리 셀렉트도 기존 값으로 세팅
    const categorySelect = document.getElementById("post-category");
    if (categorySelect && post.category) {
      categorySelect.value = post.category;
    }

    let idInput = document.getElementById("editing-post-id");
    if (!idInput) {
      idInput = document.createElement("input");
      idInput.type = "hidden";
      idInput.id = "editing-post-id";
      idInput.name = "postId";
      postForm.appendChild(idInput);
    }
    idInput.value = postId;

    const delBtn = document.getElementById("delete-post-btn");
    if (delBtn) {
      delBtn.style.display = "block";
      delBtn.dataset.postId = postId;
    }
  });

  // ===============================
  // 파일 선택 시 파일명 표시
  // ===============================
  document.getElementById("image")?.addEventListener("change", function () {
    const fileDisplay = document.getElementById("file-name-display");
    if (!fileDisplay) return;

    if (this.files.length > 0) {
      fileDisplay.textContent = "선택한 파일: " + this.files[0].name;
    } else {
      fileDisplay.textContent = "";
    }
  });

  // ===============================
  // 삭제하기 버튼 클릭 → 게시글 삭제
  // ===============================
  document.getElementById("delete-post-btn")?.addEventListener("click", async () => {
    const delBtn = document.getElementById("delete-post-btn");
    if (!delBtn) return;

    const postId = delBtn.dataset.postId;
    if (!postId) return;

    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
      const res = await fetch(`/api/community/${postId}`, {
        method: "DELETE"
      });

      if (!res.ok) {
        const msg = await res.text();
        alert(msg || "삭제 실패");
        return;
      }

      alert("게시글이 삭제되었습니다.");

      postForm?.reset();
      delBtn.style.display = "none";
      const idInput = document.getElementById("editing-post-id");
      if (idInput) idInput.remove();

      showView(feedContainer);
      loadFeed(window.currentCategory);

    } catch (err) {
      console.error("삭제 오류:", err);
      alert("서버 오류: " + err.message);
    }
  });

  // ===============================
  // 팔로우 / 언팔로우 버튼 이벤트
  // ===============================
  document.addEventListener("click", async (e) => {
    const btn = e.target.closest(".follow-btn");
    if (!btn) return;

    if (!window.isLogin) {
      if (confirm("로그인 후 이용 가능합니다.\n로그인 페이지로 이동할까요?")) {
        location.href = "/signin";
      }
      return;
    }

    const targetUsername = btn.dataset.target;
    if (!targetUsername) return;

    if (window.currentUsername && window.currentUsername === targetUsername) {
      alert("자기 자신은 팔로우할 수 없습니다.");
      return;
    }

    const isFollowing = btn.classList.contains("following");

    try {
      let res;
      if (!isFollowing) {
        res = await fetch(`/api/follow/${encodeURIComponent(targetUsername)}`, {
          method: "POST"
        });
      } else {
        res = await fetch(`/api/follow/${encodeURIComponent(targetUsername)}`, {
          method: "DELETE"
        });
      }

      if (!res.ok) {
        if (res.status === 401) {
          alert("로그인이 필요합니다.");
        } else {
          alert("팔로우 처리 실패");
        }
        return;
      }

      const action = isFollowing ? "unfollow" : "follow";

      document.querySelectorAll(`.follow-btn[data-target="${targetUsername}"]`)
        .forEach(b => {
          if (action === "follow") {
            b.textContent = "팔로우 취소";
            b.classList.add("following");
          } else {
            b.textContent = "팔로우";
            b.classList.remove("following");
          }
        });

      refreshProfileStats();

    } catch (err) {
      console.error("팔로우 처리 실패:", err);
      alert("서버 오류가 발생했습니다.");
    }
  });

  // =====================================================
  // 좋아요 버튼 이벤트
  // =====================================================
  document.addEventListener("click", async (e) => {
    const likeBtn = e.target.closest(".like-btn");
    if (!likeBtn) return;

    if (!window.isLogin) {
      alert("로그인 후 이용 가능합니다.");
      return;
    }

    const postCard = likeBtn.closest(".feed-post-card");
    const postId = postCard.dataset.id;

    try {
      const res = await fetch(`/api/community/like/${postId}`, {
        method: "POST",
      });

      const contentType = res.headers.get("content-type") || "";

      if (!contentType.includes("application/json")) {
        const text = await res.text();
        console.error("JSON 아님, 서버 응답:", text);

        if (res.status === 401) {
          alert("로그인 세션이 만료되었습니다. 다시 로그인해 주세요.");
          window.location.href = "/signin";
        } else {
          alert("좋아요 처리 중 오류가 발생했습니다.");
        }
        return;
      }

      const data = await res.json();

      if (!res.ok) {
        alert(data.message || "좋아요 처리 실패");
        return;
      }

      const likeCnt = postCard.querySelector(".like-count");
      likeCnt.textContent = `좋아요 ${data.likeCount}개`;

      if (data.liked) {
        likeBtn.classList.add("liked");
      } else {
        likeBtn.classList.remove("liked");
      }

    } catch (err) {
      console.error("좋아요 오류:", err);
      alert("서버 오류 발생");
    }
  });

  // =====================================================
  // 댓글 모달 열기 / 닫기
  // =====================================================
    function openCommentModal(postId) {
      currentPostIdForComment = postId;

      if (!commentModal) return;

      commentModal.classList.add("show");
      commentInput.value = "";

      // 댓글 불러오기
      loadComments(postId);
    }

    function closeCommentModal() {
      if (!commentModal) return;
      commentModal.classList.remove("show");
      currentPostIdForComment = null;
    }

    async function loadComments(postId) {
      if (!commentListEl || !noCommentMsgEl) return;

      commentListEl.innerHTML = "";

      try {
        const res = await fetch(`/api/community/${postId}/comments`);
        if (!res.ok) {
          console.error("댓글 조회 실패");
          return;
        }
        const comments = await res.json();

        if (!comments || comments.length === 0) {
          noCommentMsgEl.style.display = "block";
          return;
        }

        noCommentMsgEl.style.display = "none";

        comments.forEach(c => {
          const li = document.createElement("li");
          li.className = "comment-item";
          li.dataset.id = c.id;

          li.innerHTML = `
            <div class="comment-meta">
              <span class="comment-writer">${c.writerName}</span>
              <span class="comment-date">${c.createdAt}</span>
            </div>
            <div class="comment-content">${c.content}</div>
            ${c.mine ? '<button class="comment-delete-btn">댓글 삭제</button>' : ''}
          `;

          commentListEl.appendChild(li);
        });

      } catch (err) {
        console.error("댓글 로딩 오류:", err);
      }
    }

  // ===============================
  // 댓글 아이콘 클릭 → 모달 열기
  // ===============================
  document.addEventListener("click", (e) => {
    const commentBtn = e.target.closest(".comment-btn");
    if (!commentBtn) return;

    const postCard = commentBtn.closest(".feed-post-card");
    if (!postCard) return;

    const postId = postCard.dataset.id;
    if (!postId) return;

    // 비로그인도 댓글 "보기"는 가능
    openCommentModal(postId);
  });

  commentModalCloseBtn?.addEventListener("click", () => {
    closeCommentModal();
  });

  // ===============================
  // 모달 닫기
  // ===============================
  commentModalOverlay?.addEventListener("click", () => {
    closeCommentModal();
  });

  // ===============================
  // 댓글 등록 버튼 이벤트
  // ===============================
  commentSubmitBtn?.addEventListener("click", async () => {
    if (!currentPostIdForComment) return;

    const content = commentInput.value.trim();
    if (!content) {
      alert("댓글 내용을 입력해주세요.");
      return;
    }

    if (!window.isLogin) {
      if (confirm("댓글을 남기려면 로그인이 필요합니다.\n로그인 페이지로 이동할까요?")) {
        location.href = "/signin";
      }
      return;
    }

    try {
      const res = await fetch(`/api/community/${currentPostIdForComment}/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ content })
      });

      const data = await res.json().catch(() => null);

      if (!res.ok) {
        alert(data?.message || "댓글 등록에 실패했습니다.");
        return;
      }

      // 성공 시: 모달 닫고, 피드 댓글 수 갱신
      closeCommentModal();
      loadFeed(window.currentCategory);  // 댓글 수도 다시 가져오기

    } catch (err) {
      console.error("댓글 등록 오류:", err);
      alert("서버 오류가 발생했습니다.");
    }
  });

  // ===============================
  // 댓글 삭제
  // ===============================
  commentListEl?.addEventListener("click", async (e) => {
    const delBtn = e.target.closest(".comment-delete-btn");
    if (!delBtn) return;

    if (!window.isLogin) {
      alert("로그인이 필요합니다.");
      return;
    }

    const item = delBtn.closest(".comment-item");
    if (!item) return;

    const commentId = item.dataset.id;

    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
      const res = await fetch(`/api/community/comments/${commentId}`, {
        method: "DELETE"
      });

      const data = await res.json().catch(() => null);

      if (!res.ok) {
        alert(data?.message || "댓글 삭제에 실패했습니다.");
        return;
      }

      // 삭제 후: 현재 모달 안의 댓글 다시 로딩 + 피드 댓글수 갱신
      if (currentPostIdForComment) {
        await loadComments(currentPostIdForComment);
      }
      loadFeed(window.currentCategory);

    } catch (err) {
      console.error("댓글 삭제 오류:", err);
      alert("서버 오류가 발생했습니다.");
    }
  });

  // ===============================
  // 내가 쓴 글 보기 -> 프로필 편집
  // ===============================
    const profileEditBtn = document.getElementById("profile-edit-btn");
    const profileEditModal = document.getElementById("profile-edit-modal");
    const profileEditOverlay = document.querySelector(".profile-edit-modal-overlay");
    const profileEditCloseBtn = document.querySelector(".profile-edit-close");

    // ---------- 모달 열기 ----------
    profileEditBtn?.addEventListener("click", async () => {
      profileEditModal.classList.add("show");
      profileEditOverlay.classList.add("show");

      const res = await fetch("/api/user/profile");
      const data = await res.json();

      document.getElementById("edit-name").value = data.name || "";
      document.getElementById("edit-bio").value = data.bio || "";
    });

    // ---------- 모달 닫기 ----------
    profileEditCloseBtn?.addEventListener("click", () => {
      profileEditModal.classList.remove("show");
      profileEditOverlay.classList.remove("show");
    });

    profileEditOverlay?.addEventListener("click", () => {
      profileEditModal.classList.remove("show");
      profileEditOverlay.classList.remove("show");
    });

    // ---------- 저장 ----------
    document.getElementById("profile-edit-save-btn")?.addEventListener("click", async () => {
      const name = document.getElementById("edit-name").value.trim();
      const bio = document.getElementById("edit-bio").value.trim();
      const image = document.getElementById("edit-profile-image").files[0];

      const formData = new FormData();
      formData.append("name", name);
      formData.append("bio", bio);
      if (image) formData.append("image", image);

      const res = await fetch("/api/user/profile/edit", {
        method: "POST",
        body: formData
      });

      const data = await res.json();

      if (res.ok) {
        alert("프로필이 수정되었습니다!");
        applyUserProfile(data);
        profileEditModal.classList.remove("show");
        profileEditOverlay.classList.remove("show");
      } else {
        alert(data.message || "수정 실패");
      }
    });

    // =======================
    // 스크롤 애니메이션 observer (한 줄 3개 순서대로)
    // =======================

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {

        if (entry.isIntersecting) {

          // 현재 카드의 index 계산
          const cards = Array.from(document.querySelectorAll(".feed-post-card.effect_yj"));
          const idx = cards.indexOf(entry.target);

          // 가로 한 줄당 3개 → 줄 내부 순서 계산
          const orderInRow = idx % 3;  // 0, 1, 2

          // 순서마다 딜레이 다르게 적용
          entry.target.style.transitionDelay = `${orderInRow * 0.15}s`;

          entry.target.classList.add("show");

          // 한 번만 실행
          observer.unobserve(entry.target);
        }
      });
    }, {
      threshold: 0.1,
      rootMargin: "100px 0px"
    });

    function observeFeedCards() {
      const cards = document.querySelectorAll(".feed-post-card.effect_yj");
      cards.forEach(card => observer.observe(card));
    }

});