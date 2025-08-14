// 페이지가 로드될 때 localStorage에서 새 글을 가져와 표시
document.addEventListener('DOMContentLoaded', () => {
    const newPost = JSON.parse(localStorage.getItem('newPost'));

    if (newPost) {
        const postGrid = document.getElementById('post-grid');

        const card = document.createElement('div');
        card.className = 'post-card';

        let cardContentHTML = '';
        if (newPost.image) {
            cardContentHTML += `<img src="${newPost.image}" alt="추모 사진">`;
        }
        cardContentHTML += `
                    <div class="post-card-content">
                        <p>${newPost.message}</p>
                    </div>
                `;

        card.innerHTML = cardContentHTML;
        postGrid.prepend(card); // 새 글을 맨 앞에 추가

        // 데이터를 사용한 후에는 삭제하여 새로고침 시 중복 추가 방지
        localStorage.removeItem('newPost');
    }
});