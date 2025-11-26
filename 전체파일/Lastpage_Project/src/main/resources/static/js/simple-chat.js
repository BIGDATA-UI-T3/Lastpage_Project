// ============================================================
// 단순 대화 챗봇 (SIMPLE)
// - 기능: 간단한 LLM 질의/응답
// - 의존성 분리: 전역 유틸이 없을 때를 대비해 지역 fallback을 포함
// ============================================================
(function () {
    //-------------------------------
    // 함수용도: 화면 요소 캐시
    // - 입력: 없음
    // - 출력: 없음
    // - 비고: DOMContentLoaded 시점 이후 스크립트 로드 전제
    // 실행되는 순서 : 1 (초기 실행 시 상수 바인딩)
    //-------------------------------
    // NOTE: simpleLauncher 요소는 이제 chat-main-launcher.js가 클릭 이벤트를 관리합니다.
    const simpleLauncher = document.getElementById('simpleChatLauncher');
    const simplePanel = document.getElementById('simpleChatPanel');
    const simpleCloseBtn = document.getElementById('simpleChatClose');
    const simpleChatBody = simplePanel.querySelector('.chat-body');
    const simpleInput = simplePanel.querySelector('#simpleChatInput'); // ID를 명확히 찾도록 수정
    const simpleSendButton = simplePanel.querySelector('#simpleChatSend'); // ID를 명확히 찾도록 수정

    //-------------------------------
    // 함수용도: 로딩 인디케이터 참조를 보관하는 상태
    // - 입력: 없음
    // - 출력: 없음
    // - 비고: setLoading에서 사용
    // 실행되는 순서 : 1
    //-------------------------------
    const state = { loadingIndicator: null };

    // 유틸 존재하지 않을 경우 지역 구현(fallback)
    //-------------------------------
    // 함수용도: 패널 열기/닫기 토글 (전역 toggleChatPanel 없을 때 대체)
    // - 입력: panel(Element), input(Element)
    // - 출력: 없음
    // - 비고: 열릴 때 입력 포커스 지연 부여
    // 실행되는 순서 : 필요 시 호출
    //-------------------------------
    const doTogglePanel = typeof toggleChatPanel === 'function'
       ? toggleChatPanel
       : function(panel, input) {
          panel.classList.toggle('visible');
          if (panel.classList.contains('visible')) {
             setTimeout(() => input && input.focus && input.focus(), 200);
          }
       };

    //-------------------------------
    // 함수용도: 채팅 메시지 추가 (전역 appendChatMessage 없을 때 대체)
    // - 입력: chatBody(Element), role('user'|'bot'), payload(string)
    // - 출력: 생성된 메시지(Element)
    // - 비고: bot일 때 배지 출력
    // 실행되는 순서 : 필요 시 호출
    //-------------------------------
    const append = typeof appendChatMessage === 'function'
       ? appendChatMessage
       : function(chatBody, role, payload) {
          const message = document.createElement('div');
          message.classList.add('chat-message', role);
          if (role === 'bot') {
             const badge = document.createElement('span');
             badge.className = 'bot-badge';
             badge.textContent = 'AI 상담';
             message.appendChild(badge);
          }
          const textNode = document.createTextNode(payload);
          message.appendChild(textNode);
          chatBody.appendChild(message);
          chatBody.scrollTop = chatBody.scrollHeight;
          return message;
       };

    //-------------------------------
    // 함수용도: 로딩 상태 제어 (전역 setChatLoading 없을 때 대체)
    // - 입력: isLoading(boolean), sendButton, input, chatBody, st(state)
    // - 출력: 없음
    // - 비고: 로딩 시작 시 봇 메시지로 간단한 안내 표시
    // 실행되는 순서 : 필요 시 호출
    //-------------------------------
    const setLoading = typeof setChatLoading === 'function'
       ? setChatLoading
       : function(isLoading, sendButton, input, chatBody, st) {
          if (isLoading) {
             if (sendButton) sendButton.disabled = true;
             if (input) input.disabled = true;
             if (chatBody) {
                st.loadingIndicator = append(chatBody, 'bot', '처리 중...');
             }
          } else {
             if (sendButton) sendButton.disabled = false;
             if (input) input.disabled = false;
             if (st && st.loadingIndicator && st.loadingIndicator.parentNode) {
                st.loadingIndicator.parentNode.removeChild(st.loadingIndicator);
                st.loadingIndicator = null;
             }
             if (input && input.focus) input.focus();
          }
       };

    //-------------------------------
    // 함수용도: Enter(Shift 미포함)로 전송 콜백 실행 (전역 유틸 fallback)
    // - 입력: input(Element), sendCallback(Function)
    // - 출력: 없음
    // - 비고: 기본 채팅 UX
    // 실행되는 순서 : 이벤트 바인딩 시
    //-------------------------------
    const ensureEnterHandler = typeof setupEnterKeyHandler === 'function'
       ? setupEnterKeyHandler
       : function(input, sendCallback) {
          input.addEventListener('keydown', (event) => {
             if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault();
                sendCallback();
             }
          });
       };

    //-------------------------------
    // 함수용도: ESC로 패널 닫기 (전역 유틸 fallback)
    // - 입력: panel(Element)
    // - 출력: 없음
    // - 비고: 문서 전역 keydown 리스너
    // 실행되는 순서 : 이벤트 바인딩 시
    //-------------------------------
    const ensureEscapeHandler = typeof setupEscapeKeyHandler === 'function'
       ? setupEscapeKeyHandler
       : function(panel) {
          document.addEventListener('keydown', (event) => {
             if (event.key === 'Escape' && panel.classList.contains('visible')) {
                panel.classList.remove('visible');
             }
          });
       };

    //-------------------------------
    // 함수용도: fetch 타임아웃 처리 (전역 fetchWithTimeout 없을 때 대체)
    // - 입력: url(string), options(RequestInit), timeout(ms)
    // - 출력: Response
    // - 비고: AbortController 사용
    // 실행되는 순서 : API 호출 시
    //-------------------------------
    const doFetchWithTimeout = typeof fetchWithTimeout === 'function'
       ? fetchWithTimeout
       : async function(url, options, timeout = 35000) {
          const controller = new AbortController();
          const timeoutId = setTimeout(() => controller.abort(), timeout);
          try {
             const response = await fetch(url, { ...options, signal: controller.signal });
             clearTimeout(timeoutId);
             return response;
          } catch (e) {
             clearTimeout(timeoutId);
             throw e;
          }
       };

    //-------------------------------
    // 함수용도: 에러 메시지 변환 (전역 getChatErrorMessage 없을 때 대체)
    // - 입력: error(Error)
    // - 출력: 사용자용 메시지(string)
    // - 비고: 공통 정책에 맞춘 문구
    // 실행되는 순서 : 에러 처리 시
    //-------------------------------
    const toErrorMessage = typeof getChatErrorMessage === 'function'
       ? getChatErrorMessage
       : function(error) {
          let errorMessage = '죄송합니다. ';
          if (error && error.name === 'AbortError') {
             errorMessage += '응답 시간이 초과되었습니다. 다시 시도해주세요.';
          } else if (error && String(error.message || '').includes('status: 500')) {
             errorMessage += 'AI 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
          } else if (error && String(error.message || '').includes('status: 404')) {
             errorMessage += '챗봇 API를 찾을 수 없습니다. 관리자에게 문의하세요.';
          } else {
             errorMessage += '현재 상담이 지연되고 있습니다. 잠시 후 다시 시도해주세요.';
          }
          return errorMessage;
       };

    // 메시지 전송 (단순 LLM 직접 호출)
    //-------------------------------
    // 함수용도: 입력값을 서버에 전송하고 응답을 채팅창에 표시
    // - 입력: 없음(내부에서 요소 접근)
    // - 출력: 없음
    // - 비고: 로딩 상태/에러 메시지 처리 포함
    // 실행되는 순서 : 전송 이벤트 발생 시
    //-------------------------------
    const sendSimpleMessage = async () => {
       const text = simpleInput.value.trim();
       if (!text || simpleSendButton.disabled) {
          return;
       }

       append(simpleChatBody, 'user', text);
       simpleInput.value = '';
       setLoading(true, simpleSendButton, simpleInput, simpleChatBody, state);

       try {
          const response = await doFetchWithTimeout('/api/v1/simple-chat', {
             method: 'POST',
             headers: {
                'Content-Type': 'application/json',
             },
             body: JSON.stringify({
                message: text
             })
          });

          if (!response.ok) {
             throw new Error(`HTTP error! status: ${response.status}`);
          }

          const data = await response.json();

          // AI 응답 표시
          if (data.reply) {
             append(simpleChatBody, 'bot', data.reply);
          }

       } catch (error) {
          console.error('단순 챗봇 API 호출 중 오류:', error);
          const errorMessage = toErrorMessage(error);
          append(simpleChatBody, 'bot', errorMessage);
       } finally {
          setLoading(false, simpleSendButton, simpleInput, simpleChatBody, state);
       }
    };

    //-----------------------------------------------------
    // 함수용도: 선택 패널에서 런처가 클릭되었을 때 실제 패널을 여는 함수를 노출합니다.
    // NOTE: chat-main-launcher.js가 이 함수를 호출하여 패널을 엽니다.
    //-----------------------------------------------------
    function openPanelFromLauncher() {
        doTogglePanel(simplePanel, simpleInput);
    }

    //-----------------------------------------------------
    // 함수용도: chat-main-launcher.js가 대신 클릭 이벤트를 트리거할 수 있도록
    // 런처 요소를 찾은 후, 패널 열기 함수를 직접 노출합니다.
    //-----------------------------------------------------
    if (simpleLauncher) {
        // 기존 런처의 'click' 리스너를 제거합니다.
        // 대신 'openPanelFromLauncher' 함수를 직접 노출하거나,
        // 아래처럼 런처 클릭 시 패널을 여는 로직만 유지하고 ESC 닫기를 유지합니다.

        // ★ 기존 런처 클릭 이벤트 제거 및 재정의 ★
        // simpleLauncher가 이제 selection-panel 내부의 버튼 역할을 하므로
        // click 이벤트 리스너를 유지하되, 이 리스너가 호출하는 함수를
        // openPanelFromLauncher()로 변경합니다.

        // simpleLauncher.addEventListener('click', () => doTogglePanel(simplePanel, simpleInput));
        // -> 위의 코드를 제거하고, 아래처럼 닫기 버튼과 전송 로직만 남깁니다.

        // simpleLauncher를 누르면 패널을 여는 역할을 chat-main-launcher.js가 맡고,
        // chat-main-launcher.js는 이 런처 버튼에 'click' 이벤트를 강제 발생시킵니다.
        // 따라서, 기존의 리스너를 그대로 두면 됩니다.

        // 하지만 충돌을 피하기 위해, simpleLauncher를 클릭할 때의 동작을
        // chat-main-launcher.js의 역할로 위임합니다.
        // 단순 토글 대신, 명시적으로 패널을 열도록 노출하는 것이 가장 안전합니다.

        // **최적의 솔루션:** // 1. 기존의 simpleLauncher.addEventListener 코드를 제거합니다.
        // 2. chat-main-launcher.js가 강제 호출할 수 있도록 함수를 등록합니다.

        // **간단한 솔루션 (현재 코드 유지):**
        // chat-main-launcher.js에서 강제로 simpleLauncher의 click을 트리거하면,
        // 아래의 기존 코드가 실행되어 패널이 토글(열림/닫힘)됩니다.
        // 이 경우, 다른 패널이 열려있다면 닫히지 않을 수 있습니다.
        // 따라서, simpleLauncher가 클릭될 때 패널을 **열기만** 하도록 명시적으로 바꿔야 합니다.

        // 런처 클릭 시 동작: 토글 대신 열기만 하도록 함수를 재정의합니다.
        // **-> [Chat-Main-Launcher.js]가 click을 트리거하므로, 기존 리스너를 유지합니다.**
    }


    // 이벤트 바인딩
    //-------------------------------
    // 함수용도: 런처/닫기/전송/키보드 핸들러 바인딩
    // - 입력: 없음
    // - 출력: 없음
    // - 비고: 초기화 시 1회 실행
    // 실행되는 순서 : 2
    //-------------------------------

    // ★ 런처 이벤트는 chat-main-launcher.js가 담당하므로, 여기서는 제거합니다. ★
    // simpleLauncher.addEventListener('click', () => doTogglePanel(simplePanel, simpleInput));

    // 닫기 버튼은 자기 패널만 닫습니다. (유지)
    simpleCloseBtn.addEventListener('click', () => doTogglePanel(simplePanel, simpleInput));

    // 전송 로직은 유지합니다.
    simpleSendButton.addEventListener('click', sendSimpleMessage);
    ensureEnterHandler(simpleInput, sendSimpleMessage);

    // ESC 닫기 로직은 유지합니다.
    ensureEscapeHandler(simplePanel);

    // NOTE: chat-main-launcher.js가 simpleLauncher에 이벤트를 강제 발생시키는 대신,
    // 패널을 열고 닫는 명시적인 유틸 함수를 사용하도록 하는 것이 더 안정적입니다.
    // 하지만 현재 구조상 chat-main-launcher.js에서 simpleLauncher.click()을 사용한다고 가정하고
    // 이 파일에서는 충돌을 방지하기 위해 런처 리스너를 제거했습니다.

    // 만약 simpleLauncher를 눌러야 하는 상황이 있다면, 아래 코드를 유지해야 합니다.
    // simpleLauncher.addEventListener('click', () => doTogglePanel(simplePanel, simpleInput));

    // chat-main-launcher.js가 강제로 click()을 호출할 것이므로,
    // simpleLauncher에 리스너가 반드시 있어야 합니다.
    // **결론: 런처 리스너를 다시 포함하되, doTogglePanel 함수를 openPanelOnly 함수로 대체해야 합니다.**
    // **그러나 doTogglePanel은 이미 토글 기능밖에 없으므로, chat-main-launcher.js에서
    // 모든 패널을 닫고, simpleLauncher.click()을 호출하는 로직이 더 간단합니다.**

    // **최종 결정: simpleLauncher 리스너는 그대로 유지합니다.
    // chat-main-launcher.js에서 모든 패널 닫기 -> simpleLauncher.click()을 통해 토글을 발생시킵니다.
    // 그러나 simpleLauncher는 선택 패널 내부의 버튼이므로, 토글 대신 열기만 해야 합니다.**

    // **다시 수정: simpleLauncher의 리스너를 '열기' 기능만 하도록 명시적으로 수정합니다.**

    //-------------------------------
    // 함수용도: 패널 열기 (토글 대신)
    // - 입력: panel(Element), input(Element)
    // - 출력: 없음
    // - 비고: 열릴 때 입력 포커스 지연 부여
    //-------------------------------
    function openPanelOnly(panel, input) {
          if (!panel.classList.contains('visible')) {
             panel.classList.add('visible');
             setTimeout(() => input && input.focus && input.focus(), 200);
          }
    }

    // 기존 런처 토글을 명시적 '열기'로 변경 (chat-main-launcher.js가 닫기를 관리)
    if (simpleLauncher) {
        simpleLauncher.addEventListener('click', () => openPanelOnly(simplePanel, simpleInput));
    }

})();