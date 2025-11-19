document.addEventListener('DOMContentLoaded', () => {
    // 1. Thymeleaf가 이미 만들어둔 HTML 요소들을 선택합니다.
    const gridContainer = document.querySelector('.grid');
    const overlay = document.getElementById('overlay');
    const items = document.querySelectorAll('.grid-item'); // HTML에 이미 있는 아이템들

    let activeClone = null;
    let originalItem = null;
    let isAnimating = false;

    // 2. 각 아이템에 클릭 이벤트 연결 (데이터 생성 X, 이벤트 연결 O)
    items.forEach(item => {
        item.addEventListener('click', () => {
            // 애니메이션 중이거나 이미 열려있으면 무시
            if (activeClone || isAnimating) return;
            expandItem(item);
        });
    });

    // 3. 오버레이(배경) 클릭 시 닫기
    overlay.addEventListener('click', () => {
        if (activeClone) {
            collapseItem();
        }
    });

    // ---------------------------------------------------------
    // [기능 1] 아이템 확장 (애니메이션)
    // ---------------------------------------------------------
    function expandItem(item) {
        if (isAnimating) return;
        isAnimating = true;

        originalItem = item;
        const rect = item.getBoundingClientRect();

        // 원본을 복제해서 애니메이션용 클론 생성
        activeClone = item.cloneNode(true);
        activeClone.classList.add('expanded-clone');
        document.body.appendChild(activeClone);

        // 클론의 초기 위치를 원본 위치로 잡음
        activeClone.style.position = 'fixed';
        activeClone.style.left = `${rect.left}px`;
        activeClone.style.top = `${rect.top}px`;
        activeClone.style.width = `${rect.width}px`;
        activeClone.style.height = `${rect.height}px`;
        activeClone.style.transform = 'none';
        activeClone.style.margin = '0';

        // 원본은 안 보이게 처리, 배경 어둡게
        originalItem.classList.add('is-expanding');
        overlay.classList.add('active');

        // 화면 중앙으로 이동 애니메이션
        requestAnimationFrame(() => {
            const centerX = window.innerWidth / 2;
            const centerY = window.innerHeight / 2;

            activeClone.style.left = `${centerX}px`;
            activeClone.style.top = `${centerY}px`;

            // 반응형 크기 조절
            activeClone.style.width = `min(60vw, 750px)`;
            activeClone.style.height = `auto`;
            activeClone.style.transform = `translate(-50%, -50%)`;
        });

        // 애니메이션 완료 대기
        setTimeout(() => { isAnimating = false; }, 600);

        // 닫기 버튼(X) 이벤트 연결
        const closeBtn = activeClone.querySelector('.close-btn');
        if (closeBtn) {
            closeBtn.onclick = (e) => {
                e.stopPropagation(); // 부모 클릭 방지
                collapseItem();
            };
        }
    }

    // ---------------------------------------------------------
    // [기능 2] 아이템 축소 (닫기)
    // ---------------------------------------------------------
    function collapseItem() {
        if (isAnimating || !activeClone) return;
        isAnimating = true;

        const rect = originalItem.getBoundingClientRect();

        // 내용물 먼저 숨기고, 카드 모양으로 복귀
        const expandedContent = activeClone.querySelector('.expanded-content');
        const previewContent = activeClone.querySelector('.item-content-preview');
        const glassDoor = activeClone.querySelector('.glass-door');

        if(expandedContent) expandedContent.style.opacity = 0;
        if(previewContent) previewContent.style.opacity = 1;
        if(glassDoor) glassDoor.style.transform = 'rotateY(0deg)';

        // 원래 위치로 이동
        setTimeout(() => {
            activeClone.style.left = `${rect.left}px`;
            activeClone.style.top = `${rect.top}px`;
            activeClone.style.width = `${rect.width}px`;
            activeClone.style.height = `${rect.height}px`;
            activeClone.style.transform = 'none';
        }, 50);

        // 배경 밝게
        overlay.classList.remove('active');

        // 애니메이션 끝나면 클론 삭제
        setTimeout(() => {
            if (activeClone) activeClone.remove();
            if (originalItem) originalItem.classList.remove('is-expanding');
            activeClone = null;
            originalItem = null;
            isAnimating = false;
        }, 650);
    }
});