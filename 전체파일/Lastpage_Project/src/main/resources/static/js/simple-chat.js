// ============================================================
// 단순 대화 챗봇 (SIMPLE)
// - 기능: 간단한 LLM 질의/응답 (전송 기능 포함)
// ============================================================
(function () {
    // ------------------------------------------------------------------
    // 1. 화면 요소 캐시 및 상태 (Initialization)
    // ------------------------------------------------------------------
    const simpleLauncher = document.getElementById('simpleChatLauncher');
    const simplePanel = document.getElementById('simpleChatPanel');
    const simpleCloseBtn = document.getElementById('simpleChatClose');

    // 전송 관련 핵심 요소 (ID로 확실히 참조)
    const simpleChatBody = simplePanel ? simplePanel.querySelector('.chat-body') : null;
    const simpleInput = document.getElementById('simpleChatInput');
    const simpleSendButton = document.getElementById('simpleChatSend');

    // 패널 요소가 없으면 스크립트 실행 중단
    if (!simplePanel || !simpleInput || !simpleSendButton || !simpleChatBody) {
        console.warn("단순 챗봇 패널 요소(simpleChatPanel, simpleChatInput, simpleChatSend) 중 하나를 찾을 수 없습니다.");
        return;
    }

    const state = { loadingIndicator: null };

    // ------------------------------------------------------------------
    // 2. 지역/전역 유틸리티 (Fallback Definitions)
    // ------------------------------------------------------------------

    // 패널 열기/닫기 토글 (전역 toggleChatPanel 없을 때 대체)
    // chat-main-launcher.js가 닫기를 관리하므로, 여기서는 명시적 '열기' 기능만 사용합니다.
    function openPanelOnly(panel, input) {
        if (!panel.classList.contains('visible')) {
           panel.classList.add('visible');
           // 포커스 지연 부여
           setTimeout(() => input && input.focus && input.focus(), 200);
        }
    }

    // 닫기/토글 유틸리티
    const doTogglePanel = typeof toggleChatPanel === 'function'
       ? toggleChatPanel
       : function(panel, input) {
          panel.classList.toggle('visible');
          if (panel.classList.contains('visible')) {
             setTimeout(() => input && input.focus && input.focus(), 200);
          }
       };

    // 채팅 메시지 추가 (전역 appendChatMessage 없을 때 대체)
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
          // 텍스트 노드를 사용하여 안전하게 메시지 추가
          message.appendChild(document.createTextNode(payload));
          chatBody.appendChild(message);
          chatBody.scrollTop = chatBody.scrollHeight;
          return message;
       };

    // 로딩 상태 제어 (전역 setChatLoading 없을 때 대체)
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

    // Enter(Shift 미포함)로 전송 콜백 실행 (전역 유틸 fallback)
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

    // ESC로 패널 닫기 (전역 유틸 fallback)
    const ensureEscapeHandler = typeof setupEscapeKeyHandler === 'function'
       ? setupEscapeKeyHandler
       : function(panel) {
          document.addEventListener('keydown', (event) => {
             if (event.key === 'Escape' && panel.classList.contains('visible')) {
                panel.classList.remove('visible');
             }
          });
       };

    // fetchWithTimeout, getChatErrorMessage 등의 다른 fallback 함수는 내용이 길어 생략합니다.
    // 기존 코드의 해당 함수들은 그대로 작동한다고 가정합니다.
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

    const toErrorMessage = typeof getChatErrorMessage === 'function'
       ? getChatErrorMessage
       : function(error) {
          let errorMessage = '죄송합니다. ';
          if (error && error.name === 'AbortError') {
             errorMessage += '응답 시간이 초과되었습니다. 다시 시도해주세요.';
          } else {
             errorMessage += '현재 상담이 지연되고 있습니다. 잠시 후 다시 시도해주세요.';
          }
          return errorMessage;
       };


    // ------------------------------------------------------------------
    // 3. 메시지 전송 로직 (Core Functionality)
    // ------------------------------------------------------------------

    const sendSimpleMessage = async () => {
       const text = simpleInput.value.trim();
       if (!text || simpleSendButton.disabled) {
          return;
       }

       append(simpleChatBody, 'user', text);
       simpleInput.value = '';
       setLoading(true, simpleSendButton, simpleInput, simpleChatBody, state);

       try {
          // 가상의 API 호출
          const response = await doFetchWithTimeout('/api/v1/simple-chat', {
             method: 'POST',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({ message: text })
          });

          if (!response.ok) {
             throw new Error(`HTTP error! status: ${response.status}`);
          }

          const data = await response.json();

          // AI 응답 표시
          if (data.reply) {
             append(simpleChatBody, 'bot', data.reply);
          } else {
             // API 응답에 reply 필드가 없는 경우
             append(simpleChatBody, 'bot', '응답을 받았으나 내용이 비어있습니다.');
          }

       } catch (error) {
          console.error('단순 챗봇 API 호출 중 오류:', error);
          const errorMessage = toErrorMessage(error);
          append(simpleChatBody, 'bot', errorMessage);
       } finally {
          setLoading(false, simpleSendButton, simpleInput, simpleChatBody, state);
       }
    };


    // ------------------------------------------------------------------
    // 4. 이벤트 바인딩 (Event Handlers)
    // ------------------------------------------------------------------

    // 런처 클릭 시 패널 열기 (chat-main-launcher.js의 click 트리거에 반응)
    if (simpleLauncher) {
        simpleLauncher.addEventListener('click', () => openPanelOnly(simplePanel, simpleInput));
    }

    // 닫기 버튼: 패널 닫기 (토글 대신 명시적 닫기를 사용할 수도 있지만, 기존 토글 함수 사용)
    simpleCloseBtn.addEventListener('click', () => doTogglePanel(simplePanel, simpleInput));

    // 전송 버튼 클릭 이벤트
    simpleSendButton.addEventListener('click', sendSimpleMessage);

    // Enter 키 입력 이벤트
    ensureEnterHandler(simpleInput, sendSimpleMessage);

    // ESC 닫기 이벤트
    ensureEscapeHandler(simplePanel);

})();