(function () {
    // 1. 메인 런처와 선택 패널 요소
    const mainLauncher = document.getElementById('mainChatLauncher');
    const selectionPanel = document.getElementById('chatSelectionPanel');
    const selectionItems = document.querySelectorAll('.chat-selection-item');

    // 2. 개별 챗봇 패널 요소 (토글 시 닫아주기 위함)
    const advancedPanel = document.getElementById('chatPanel');
    const simplePanel = document.getElementById('simpleChatPanel');
    const docPanel = document.getElementById('docChatPanel');

    // 현재 열려있는 챗봇 패널 추적 (JS 파일이 여러 개라 이중 관리됨)
    const allChatPanels = [advancedPanel, simplePanel, docPanel];

    // 선택 패널 토글 함수
    function toggleSelectionPanel() {
        const isVisible = selectionPanel.classList.toggle('visible');

        // 선택 패널이 열릴 때, 혹시 모를 다른 챗봇 패널을 모두 닫기
        if (isVisible) {
            closeAllChatPanels();
        }
    }

    // 다른 모든 챗봇 패널을 닫는 함수
    function closeAllChatPanels() {
        allChatPanels.forEach(panel => {
            if (panel && panel.classList.contains('visible')) {
                panel.classList.remove('visible');
            }
        });
    }

    // 클릭 이벤트 바인딩
    function bind() {
        if (!mainLauncher || !selectionPanel) return;

        // 메인 런처 클릭 시 선택 패널 토글
        mainLauncher.addEventListener('click', toggleSelectionPanel);

        // 선택 항목 클릭 시
        selectionItems.forEach(item => {
            item.addEventListener('click', () => {
                // 1. 선택 패널 닫기
                selectionPanel.classList.remove('visible');

                // 2. 이전에 열려있던 패널 닫기 (새로운 패널이 열리기 전 정리)
                // (선택한 항목에 해당하는 패널은 그 챗봇의 JS가 열어줄 것입니다.)

                // 3. (옵션) 선택된 버튼을 눌러 해당 챗봇의 JS가 동작하도록 트리거
                //    - 기존 JS 파일들은 각 런처 버튼ID에 바인딩되어 있습니다.
                //    - 여기서 실제 런처 버튼 클릭 이벤트를 강제로 발생시킵니다.
                //    - 주의: 이 코드는 기존 JS 파일이 전역에서 실행되어 버튼 클릭을 기다린다는 가정 하에 작동합니다.
                const originalLauncherId = item.id;
                const originalLauncher = document.getElementById(originalLauncherId);
                if (originalLauncher) {
                    originalLauncher.click();
                }
            });
        });

        // ★ 중요: 기존 챗봇의 런처 버튼은 이제 선택 패널 내에 있으므로,
        // 기존 JS 파일이 패널을 열 때, 선택 패널을 닫도록 보강합니다.

        // 문서 전체를 클릭했을 때 선택 패널 닫기
        document.addEventListener('click', (e) => {
            if (!mainLauncher.contains(e.target) && !selectionPanel.contains(e.target)) {
                 selectionPanel.classList.remove('visible');
            }
        });
    }

    if (document.readyState === 'loading') {
       document.addEventListener('DOMContentLoaded', bind);
    } else {
       bind();
    }
})();
