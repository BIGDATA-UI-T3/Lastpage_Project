document.addEventListener('DOMContentLoaded', function () {
    // --- 요소 선택 ---
    // const logoBtn = document.getElementById('logo-home-btn');
    const writeBtn = document.getElementById('write-btn');
    const myPostsBtn = document.getElementById('my-posts-view-btn');
    const scrollToTopBtn = document.getElementById("scrollToTopBtn");
    const writeFormBackBtn = document.getElementById('write-form-back-btn');
    const profileBackBtn = document.getElementById('profile-back-btn');

    const feedContainer = document.getElementById('feed-container');
    const writeContainer = document.getElementById('write-container');
    const myPostsContainer = document.getElementById('my-posts-container');

    // --- 화면 전환 함수 ---
    function showView(viewToShow) {
        feedContainer.style.display = 'none';
        writeContainer.style.display = 'none';
        myPostsContainer.style.display = 'none';

        viewToShow.style.display = 'block';
        window.scrollTo(0, 0);

        if (viewToShow === myPostsContainer) {
            myPostsBtn.style.display = 'none';
        } else {
            myPostsBtn.style.display = 'block';
        }
    }

    // --- 이벤트 리스너 ---
    // logoBtn.addEventListener('click', () => showView(feedContainer));
    writeBtn.addEventListener('click', () => showView(writeContainer));
    myPostsBtn.addEventListener('click', () => showView(myPostsContainer));
    writeFormBackBtn.addEventListener('click', () => showView(feedContainer));
    profileBackBtn.addEventListener('click', () => showView(feedContainer));

    // 글쓰기 폼 제출
    const postForm = document.getElementById('post-form');
    postForm.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('글이 작성되었습니다! (실제 서버 연동 필요)');
        showView(feedContainer);
        postForm.reset();
    });

    // --- 맨 위로 가기 버튼 스크립트 ---
    window.onscroll = function () {
        if (document.body.scrollTop > 300 || document.documentElement.scrollTop > 300) {
            scrollToTopBtn.style.display = "flex";
        } else {
            scrollToTopBtn.style.display = "none";
        }
    };

    scrollToTopBtn.onclick = function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };
});