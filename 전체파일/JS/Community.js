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

                if (viewToShow === myPostsContainer) {
                    profileSidebar.style.display = 'none';
                } else {
                    profileSidebar.style.display = 'block';
                }
            }

            // --- 이벤트 리스너 ---
            if (writeBtn) writeBtn.addEventListener('click', () => showView(writeContainer));
            if (myPostsBtn) myPostsBtn.addEventListener('click', () => showView(myPostsContainer));
            if (writeFormBackBtn) writeFormBackBtn.addEventListener('click', () => showView(feedContainer));
            if (profileBackBtn) profileBackBtn.addEventListener('click', () => showView(feedContainer));

            // --- 글쓰기 폼 제출 ---
            const postForm = document.getElementById('post-form');
            if (postForm) {
                postForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    alert('글이 작성되었습니다! (실제 서버 연동 필요)');
                    showView(feedContainer);
                    postForm.reset();
                });
            }

            // --- 맨 위로 가기 버튼 ---
            window.onscroll = function () {
                if (scrollToTopBtn) {
                    if (document.body.scrollTop > 300 || document.documentElement.scrollTop > 300) {
                        scrollToTopBtn.style.display = "flex";
                    } else {
                        scrollToTopBtn.style.display = "none";
                    }
                }
            };
            if (scrollToTopBtn) {
                scrollToTopBtn.onclick = function () {
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                };
            }

            // --- 좋아요 및 저장하기 기능 ---
            function initializePostActions() {
                const likeBtns = document.querySelectorAll('.like-btn');
                likeBtns.forEach(btn => {
                    btn.addEventListener('click', () => {
                        btn.classList.toggle('active');
                        const postCard = btn.closest('.feed-post-card');
                        const likeCountEl = postCard.querySelector('.like-count');
                        let likeCount = parseInt(likeCountEl.textContent.replace('좋아요 ', '').replace(',', '').replace('개', ''));

                        if (btn.classList.contains('active')) {
                            likeCount++;
                        } else {
                            likeCount--;
                        }
                        
                        likeCountEl.textContent = `좋아요 ${likeCount.toLocaleString()}개`;
                    });
                });

                const bookmarkBtns = document.querySelectorAll('.bookmark');
                bookmarkBtns.forEach(btn => {
                    btn.addEventListener('click', () => {
                        btn.classList.toggle('active');
                    });
                });
            }

            initializePostActions();
        });