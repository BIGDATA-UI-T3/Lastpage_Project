document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('post-form');
    const messageInput = document.getElementById('message');
    const photoInput = document.getElementById('photo-upload');
    const fileNameSpan = document.getElementById('file-name');

    photoInput.addEventListener('change', () => {
        fileNameSpan.textContent = photoInput.files.length > 0 ? photoInput.files[0].name : '선택된 파일 없음';
    });

    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const message = messageInput.value.trim();
        const photoFile = photoInput.files[0];

        if (!message && !photoFile) {
            alert('메시지나 사진 중 하나는 꼭 필요해요.');
            return;
        }

        const postData = { message: escapeHTML(message), image: null };

        if (photoFile) {
            const reader = new FileReader();
            reader.onload = function (event) {
                postData.image = event.target.result;
                saveAndRedirect(postData);
            };
            reader.readAsDataURL(photoFile);
        } else {
            saveAndRedirect(postData);
        }
    });

    function saveAndRedirect(data) {
        localStorage.setItem('newPost', JSON.stringify(data));
        window.location.href = 'ourpage.html'; // 메인 페이지로 이동
    }

    function escapeHTML(str) {
        return str.replace(/[&<>'"]/g,
            tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag));
    }
});