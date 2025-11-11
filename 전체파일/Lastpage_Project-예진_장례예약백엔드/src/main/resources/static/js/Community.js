document.addEventListener('DOMContentLoaded', function () {

    // --- 요소 선택 ---
    const writeBtn = document.getElementById('write-btn');
    const myPostsBtn = document.getElementById('my-posts-view-btn');
    const scrollToTopBtn = document.getElementById("scrollToTopBtn");
    const writeFormBackBtn = document.getElementById('write-form-back-btn');
    const profileBackBtn = document.getElementById('profile-back-btn');

    const feedContainer = document.getElementById('feed-container');
    const writeContainer = document.getElementById('write-container');
    const myPostsContainer = document.getElementById('my-posts-container');
    const profileSidebar = document.querySelector('.profile-sidebar');


    // --- 화면 전환 함수 ---
    function showView(viewToShow) {
        feedContainer.style.display = 'none';
        writeContainer.style.display = 'none';
        myPostsContainer.style.display = 'none';

        viewToShow.style.display = 'block';
        window.scrollTo(0, 0);

        profileSidebar.style.display =
            (viewToShow === myPostsContainer) ? 'none' : 'block';
    }

    // --- 로그인 체크 후 글쓰기 버튼 ---
    if (writeBtn) {
        writeBtn.addEventListener('click', function () {
            if (!window.isLogin) {
                if (confirm("로그인 후 이용 가능합니다.\n로그인 페이지로 이동할까요?")) {
                    location.href = "/signin";
                }
                return; // 로그인 안 된 상태 → 글쓰기 막기
            }

            // 로그인된 상태 → 글쓰기 화면으로 전환
            showView(writeContainer);
        });
    }


    // --- 버튼 이벤트 ---
    if (myPostsBtn) myPostsBtn.addEventListener('click', () => showView(myPostsContainer));
    if (writeFormBackBtn) writeFormBackBtn.addEventListener('click', () => showView(feedContainer));
    if (profileBackBtn) profileBackBtn.addEventListener('click', () => showView(feedContainer));


//    // --- 글쓰기 폼 ---
//    const postForm = document.getElementById('post-form');
//    if (postForm) {
//        postForm.addEventListener('submit', (e) => {
//            e.preventDefault();
//            alert('글이 작성되었습니다! (실제 서버 연동 필요)');
//            showView(feedContainer);
//            postForm.reset();
//        });
//    }
//
//
//    // --- 좋아요 / 저장하기 UI ---
//    function initializePostActions() {
//        const likeBtns = document.querySelectorAll('.like-btn');
//
//        likeBtns.forEach(btn => {
//            btn.addEventListener('click', () => {
//                btn.classList.toggle('active');
//                const postCard = btn.closest('.feed-post-card');
//                const likeCountEl = postCard.querySelector('.like-count');
//                let likeCount = parseInt(likeCountEl.textContent.replace(/[^0-9]/g, ''));
//
//                likeCount += btn.classList.contains('active') ? 1 : -1;
//                likeCountEl.textContent = `좋아요 ${likeCount.toLocaleString()}개`;
//            });
//        });
//
//        document.querySelectorAll('.bookmark')
//            .forEach(btn => btn.addEventListener('click', () => btn.classList.toggle('active')));
//    }
//
//    initializePostActions();


    // --- 스크롤 Top 버튼 ---
    window.onscroll = function () {
        if (!scrollToTopBtn) return;
        scrollToTopBtn.style.display =
            (document.documentElement.scrollTop > 300 || document.body.scrollTop > 300)
                ? "flex"
                : "none";
    };

    if (scrollToTopBtn) {
        scrollToTopBtn.onclick = () => window.scrollTo({ top: 0, behavior: 'smooth' });
    }


    // --- 프로필 fetch ---
    fetch("/api/user/profile")
        .then(res => res.json())
        .then(data => {

            const nameEl = document.getElementById("profile-name");
            const userIdEl = document.getElementById("profile-userid");
            const bioEl = document.getElementById("profile-bio");
            const statsEl = document.getElementById("profile-stats");

            const signinBtn = document.getElementById("signin-btn");
            const signupBtn = document.getElementById("signup-btn");

            if (!data.login) {
                nameEl.textContent = "Guest";
                userIdEl.textContent = "";
                bioEl.textContent = "로그인 후 커뮤니티 서비스를 이용해보세요";

                statsEl.style.display = "none";
                myPostsBtn.style.display = "none";
                signinBtn.style.display = "block";
                signupBtn.style.display = "block";

            } else {
                nameEl.textContent = data.name;
                userIdEl.textContent = "@" + data.username;
                bioEl.textContent = data.bio || "나를 소개하는 글을 남겨보세요";

                statsEl.style.display = "flex";
                signinBtn.style.display = "none";
                signupBtn.style.display = "none";
                myPostsBtn.style.display = "block";

                if (data.avatar) {
                    document.querySelector(".avatar").outerHTML =
                        `<img src="${data.avatar}" class="avatar" style="width:70px;height:70px;border-radius:50%;object-fit:cover;">`;
                }
            }
        });

    document.getElementById("signin-btn")?.addEventListener("click", () => location.href = "/signin");
    document.getElementById("signup-btn")?.addEventListener("click", () => location.href = "/signup");

});

// 로그아웃
function logout() {
    fetch("/logout", { method: "POST" })
        .then(() => window.location.href = "/");
}

// ===============================
// 글쓰기 폼 → 피드 자동 반영 스크립트
// ===============================

document.addEventListener("DOMContentLoaded", () => {
  const postForm = document.getElementById("post-form");
  const feedContainer = document.querySelector(".post-feed"); // 피드 리스트 컨테이너
  const writeContainer = document.getElementById("write-container");
  const writeFormBackBtn = document.getElementById("write-form-back-btn");

  // 피드로 돌아가기 버튼
  if (writeFormBackBtn) {
    writeFormBackBtn.addEventListener("click", () => {
      writeContainer.style.display = "none";
      feedContainer.style.display = "block";
    });
  }

  // 글쓰기 폼 제출 이벤트
//  if (postForm) {
//    postForm.addEventListener("submit", (e) => {
//      e.preventDefault();
//
//      const message = document.getElementById("message").value.trim();
//      const fileInput = document.getElementById("photo-upload");
//      const file = fileInput.files[0];
//
//      if (!message && !file) {
//        alert("내용이나 사진 중 하나는 입력해주세요.");
//        return;
//      }
//
//      const reader = new FileReader();
//
//      reader.onload = function (event) {
//        const imageUrl = file ? event.target.result : "https://placehold.co/600x400";
//
//        const article = document.createElement("article");
//        article.className = "feed-post-card";
//        article.innerHTML = `
//          <div class="post-header">
//              <img src="https://placehold.co/50x50" class="author-avatar" alt="프로필">
//              <span class="author-name">현재 로그인 사용자</span>
//          </div>
//          <div class="post-image-wrapper">
//              <img src="${imageUrl}" alt="업로드 이미지">
//          </div>
//          <div class="post-actions">
//              <svg class="like-btn" fill="currentColor" viewBox="0 0 24 24">
//                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5C2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3C19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"></path>
//              </svg>
//              <svg class="comment-btn" fill="currentColor" viewBox="0 0 24 24">
//                  <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18zM18 14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"></path>
//              </svg>
//              <svg class="share-btn" fill="currentColor" viewBox="0 0 24 24">
//                  <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path>
//              </svg>
//              <svg class="bookmark" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
//                  <path class="bookmark-icon-outline" stroke-linecap="round" stroke-linejoin="round"
//                        d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2v16z"></path>
//                  <path class="bookmark-icon-filled" stroke-linecap="round" stroke-linejoin="round"
//                        d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2v16z"></path>
//              </svg>
//          </div>
//          <div class="post-details">
//              <div class="like-count">좋아요 0개</div>
//              <div class="post-caption"><span class="author-name">현재 로그인 사용자</span> ${message}</div>
//          </div>
//        `;

// 기존 게시글 불러오기
  fetch("/comm/list")
    .then(res => res.json())
    .then(posts => {
      posts.forEach(post => {
        const article = document.createElement("article");
        article.className = "feed-post-card";
        article.innerHTML = `
          <div class="post-header">
              <img src="https://placehold.co/50x50" class="author-avatar" alt="프로필">
              <span class="author-name">${post.member?.username || "익명"}</span>
          </div>
          <div class="post-image-wrapper">
              <img src="${post.imageUrl || "https://placehold.co/600x400"}" alt="사진">
          </div>
          <div class="post-details">
              <div class="like-count">좋아요 0개</div>
              <div class="post-caption"><span class="author-name">${post.member?.username || "익명"}</span> ${post.content}</div>
          </div>`;
        feedContainer.appendChild(article);
      });
    })
    .catch(err => console.error("피드 불러오기 실패:", err));

        // 피드에 추가 (맨 위로)
        if (feedContainer) feedContainer.prepend(article);

        // 폼 초기화 및 화면 전환
        postForm.reset();
        writeContainer.style.display = "none";
        feedContainer.style.display = "block";

        // 좋아요 버튼 활성화 이벤트 초기화
        initializePostActions();
      };

      if (file) reader.readAsDataURL(file);
      else reader.onload(); // 이미지 없을 때도 실행
    });
  }

// 좋아요
//feedContainer.addEventListener("click", async (e) => {
//  const likeBtn = e.target.closest(".like-btn");
//  if (!likeBtn) return;
//  const article = likeBtn.closest(".feed-post-card");
//  const postId = article.dataset.id;
//
//  const res = await fetch(`/comm/like/${postId}`, { method: "POST" });
//  if (!res.ok) return alert("로그인이 필요합니다.");
//  const data = await res.json();
//  article.querySelector(".like-count").textContent = `좋아요 ${data.likeCount}개`;
//});

// 좋아요 토글
const feedContainer = document.getElementById("feed-container");

feedContainer.addEventListener("click", async (e) => {
  const likeBtn = e.target.closest(".like-btn");
  if (!likeBtn) return;

  const article = likeBtn.closest(".feed-post-card");
  const postId = article.dataset.id;

  try {
    const res = await fetch(`/comm/like/${postId}`, {
      method: "POST",
    });

    if (res.status === 401) {
      alert("로그인이 필요합니다.");
      return;
    }

    const data = await res.json();
    const likeCountEl = article.querySelector(".like-count");
    likeCountEl.textContent = `좋아요 ${data.likeCount}개`;

    // 프론트 시각적 토글
    likeBtn.classList.toggle("liked");
  } catch (err) {
    console.error("좋아요 오류:", err);
  }
});


// 댓글 버튼 클릭 시 열기
feedContainer.addEventListener("click", async (e) => {
  const commentBtn = e.target.closest(".comment-btn");
  if (!commentBtn) return;
  const article = commentBtn.closest(".feed-post-card");
  const postId = article.dataset.id;

  let commentBox = article.querySelector(".comment-box");
  if (commentBox) {
    commentBox.remove();
    return;
  }

  commentBox = document.createElement("div");
  commentBox.className = "comment-box";
  commentBox.innerHTML = `
    <div class="comment-list"></div>
    <form class="comment-form">
      <input type="text" placeholder="댓글 입력..." required>
      <button type="submit">등록</button>
    </form>
  `;
  article.appendChild(commentBox);

  // 댓글 불러오기
  const res = await fetch(`/comm/comment/${postId}`);
  const comments = await res.json();
  const list = commentBox.querySelector(".comment-list");
  list.innerHTML = comments.map(c => `<p><b>${c.member?.username}</b> ${c.content}</p>`).join("");

  // 댓글 등록
  commentBox.querySelector("form").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const input = ev.target.querySelector("input");
    const text = input.value.trim();
    if (!text) return;
    await fetch(`/comm/comment/${postId}`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ content: text })
    });
    input.value = "";
    const res2 = await fetch(`/comm/comment/${postId}`);
    const comments2 = await res2.json();
    list.innerHTML = comments2.map(c => `<p><b>${c.member?.username}</b> ${c.content}</p>`).join("");
  });
});

const myPostsBtn = document.getElementById("my-posts-view-btn");
const myPostsContainer = document.getElementById("my-posts-container");
const postGrid = myPostsContainer.querySelector(".post-grid-container");

myPostsBtn.addEventListener("click", async () => {
  const res = await fetch("/comm/my");
  const posts = await res.json();

  document.querySelector(".post-feed").style.display = "none";
  myPostsContainer.style.display = "block";
  postGrid.innerHTML = posts
    .map(p => `
      <div class="grid-item" data-id="${p.id}">
        <img src="${p.imageUrl || 'https://placehold.co/400x400'}" alt="My Post">
      </div>`).join("");
});

// 내 글 클릭 시 폼으로 이동
postGrid.addEventListener("click", async (e) => {
  const item = e.target.closest(".grid-item");
  if (!item) return;
  const postId = item.dataset.id;

  const res = await fetch(`/comm/detail/${postId}`);
  const post = await res.json();

  // 글쓰기 폼 열고 내용 채우기
  myPostsContainer.style.display = "none";
  writeContainer.style.display = "block";
  document.getElementById("message").value = post.content;
  if (post.imageUrl) {
    const preview = document.createElement("img");
    preview.src = post.imageUrl;
    document.getElementById("post-form").prepend(preview);
  }

  // 삭제 버튼 추가
  let delBtn = document.getElementById("delete-post-btn");
  if (!delBtn) {
    delBtn = document.createElement("button");
    delBtn.id = "delete-post-btn";
    delBtn.textContent = "삭제하기";
    delBtn.classList.add("delete-btn");
    document.getElementById("post-form").appendChild(delBtn);

    delBtn.addEventListener("click", async () => {
      if (confirm("정말 삭제하시겠습니까?")) {
        await fetch(`/comm/${postId}`, { method: "DELETE" });
        alert("삭제되었습니다.");
        location.reload();
      }
    });
  }
});

// 댓글 목록 표시 시 내 댓글이면 수정/삭제 버튼 노출
function renderComments(list, comments, currentUser) {
  list.innerHTML = comments.map(c => `
    <div class="comment-item" data-id="${c.id}">
      <b>${c.member?.username}</b> ${c.content}
      ${currentUser === c.member?.username ? `
        <button class="edit-comment-btn">수정</button>
        <button class="delete-comment-btn">삭제</button>` : ''}
    </div>
  `).join("");
}

// 댓글 수정/삭제 이벤트
feedContainer.addEventListener("click", async (e) => {
  const delBtn = e.target.closest(".delete-comment-btn");
  const editBtn = e.target.closest(".edit-comment-btn");
  if (!delBtn && !editBtn) return;

  const commentItem = e.target.closest(".comment-item");
  const commentId = commentItem.dataset.id;
  const postId = e.target.closest(".feed-post-card").dataset.id;

  // 삭제
  if (delBtn) {
    if (confirm("댓글을 삭제하시겠습니까?")) {
      await fetch(`/comm/comment/${commentId}`, { method: "DELETE" });
      const res = await fetch(`/comm/comment/${postId}`);
      const comments = await res.json();
      renderComments(commentItem.parentElement, comments, window.currentUser);
    }
  }

  // 수정
  if (editBtn) {
    const oldText = commentItem.textContent.trim();
    const newText = prompt("댓글 수정:", oldText);
    if (!newText || newText === oldText) return;
    await fetch(`/comm/comment/${commentId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ content: newText })
    });
    const res = await fetch(`/comm/comment/${postId}`);
    const comments = await res.json();
    renderComments(commentItem.parentElement, comments, window.currentUser);
  }
});




