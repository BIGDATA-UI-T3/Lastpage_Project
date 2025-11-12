 // ---------- 데이터 셋업 (FAQ 더미) ----------
    const defaultFaq = [
      { q:"반려동물 장례란 무엇인가요?", a:"A. 반려동물 장례는 가족과 같은 반려동물의 마지막 시간을 예의를 갖춰 보내는 절차입니다. 종합 상담, 운구, 작별실, 화장, 수골, 추모까지 안내해 드립니다." },
      { q:"사체는 어떻게 보관해야 하나요?", a:"A. 가능하면 2~3시간 내 상담을 권장하며, 바로 방문이 어려우면 시신이 마르지 않도록 깨끗한 수건으로 감싸고 아이스팩을 복부에 대 주세요." },
      { q:"개별 화장과 합동 화장의 차이는?", a:"A. 개별 화장은 반려동물 한 개체만 단독으로 진행하고 유골 전량을 돌려드립니다. 합동 화장은 여러 아이가 함께 진행되어 유골 반환이 없습니다." },
      { q:"유골함/기념품(굿즈) 구매가 가능한가요?", a:"A. 가능합니다. 다양한 사이즈와 소재의 유골함, 모발/발바닥 모양 프레임, 각인 서비스 등을 제공합니다." },
      { q:"심리상담은 어떻게 진행되나요?", a:"A. 상담사는 슬픔 단계 평가와 애도 과업을 기반으로 1:1 맞춤 세션을 제공합니다. 대면/비대면 모두 가능하며 예약제로 운영됩니다." },
      { q:"예약/접수는 24시간 되나요?", a:"A. 긴급 접수는 24시간 연락 가능하며, 야간에는 대기 시간이 발생할 수 있습니다." },
      { q:"주차와 접근성은 어떤가요?", a:"A. 장례식장마다 상이합니다. 예약한 장례식장에 직접문의하기 또는 사이트 내에서 장례식장 정보를 얻을 수 있도록 지도와 함께 링크가 걸려있어 확인 가능합니다." },
      { q:"유골은 어디에 안치할 수 있나요?", a:"A. 납골당/수목장/가정 안치 등 여러 형태를 안내해 드리며, 법규를 준수합니다." },
      { q:"비용은 어떻게 산정되나요?", a:"A. 체중, 선택 서비스(개별/합동, 염습 여부, 추모실 사용, 굿즈 등)에 따라 달라집니다. 상세 견적은 상담 시 안내드립니다." },
      { q:"카드결제/현금영수증 발급되나요?", a:"A. 모두 가능합니다. 현금영수증은 개인/사업자 구분하여 발급합니다." },
      { q:"유골을 아파트 화단에 뿌려도 되나요?", a:"A. 관할 지자체 규정과 공동주거 공간의 규약을 확인해야 합니다. 수목장을 권장드립니다." },
      { q:"추모공간 예약은 어떻게 하나요?", a:"A. 모바일 또는 전화로 예약 후 방문해 주시면 됩니다." }
    ];

    // ---------- 상태 ----------
    const state = {
      faqPage: 1,
      faqPerPage: 8,
      editingId: null,
    };

    // ---------- 유틸 ----------
    const $ = (sel, root=document) => root.querySelector(sel);
    const $$ = (sel, root=document) => Array.from(root.querySelectorAll(sel));
    const formatTime = (d) => new Date(d).toLocaleString('ko-KR',{hour12:false});
    const uid = () => Math.random().toString(36).slice(2)+Date.now().toString(36);

    // ---------- 로컬 스토리지 ----------
    const LS = {
      get(key, fallback){
        try{ return JSON.parse(localStorage.getItem(key)) ?? fallback }catch{ return fallback }
      },
      set(key, val){ localStorage.setItem(key, JSON.stringify(val)) }
    };

    // 초기 FAQ
    if(!LS.get('faqData')) LS.set('faqData', defaultFaq);

    // QnA 저장 구조
    if(!LS.get('qnaPosts')) LS.set('qnaPosts', []);

    // ---------- FAQ 렌더 ----------
    function renderFaq(){
      const list = LS.get('faqData', []);
      const totalPages = Math.max(1, Math.ceil(list.length / state.faqPerPage));
      if(state.faqPage>totalPages) state.faqPage=totalPages;
      const start = (state.faqPage-1)*state.faqPerPage;
      const pageItems = list.slice(start, start+state.faqPerPage);
      const wrap = $('#faqList');
      wrap.innerHTML = '';
      pageItems.forEach((it)=>{
        const el = document.createElement('article');
        el.className = 'item';
        el.innerHTML = `
          <div class="q" role="button" aria-expanded="false">
            <div class="left"><span class="badge">Q</span><span>${it.q}</span></div>
            <span class="caret">▶</span>
          </div>
          <div class="a"><p>${it.a}</p></div>
        `;
        const btn = $('.q', el);
        btn.addEventListener('click',()=>{
          el.classList.toggle('open');
          btn.setAttribute('aria-expanded', el.classList.contains('open'));
        });
        wrap.appendChild(el);
      });
      const pager = $('#faqPager');
      pager.innerHTML = '';
      for(let i=1;i<=totalPages;i++){
        const p = document.createElement('button');
        p.className = 'page-btn'+(i===state.faqPage?' active':'');
        p.textContent=i;
        p.addEventListener('click',()=>{state.faqPage=i;renderFaq();window.scrollTo({top:0,behavior:'smooth'});});
        pager.appendChild(p);
      }
    }

    // ---------- QnA 렌더 ----------
    function renderQna(){
      const listWrap = $('#qnaList');
      const posts = LS.get('qnaPosts', []);
      const filter = $('#qnaFilter').value;
      const kw = ($('#qnaSearch').value||'').trim();
      const filtered = posts
        .filter(p=> (filter==='ALL'||p.category===filter) && (kw===''||p.title.includes(kw)||p.content.includes(kw)) )
        .sort((a,b)=> b.createdAt - a.createdAt);

      listWrap.innerHTML = '';
      if(filtered.length===0){
        const none = document.createElement('div');
        none.className='hint';
        none.style.padding='24px';
        none.textContent='등록된 질문이 없습니다. 상단 “질문하기”를 눌러 첫 질문을 남겨보세요.';
        listWrap.appendChild(none);
        return;
      }

      filtered.forEach(post=>{
        const el = document.createElement('article');
        el.className='post';
        const locked = post.secret === true;
        const answerDone = !!post.answer;
        el.innerHTML = `
          <div class="post-head" role="button">
            <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap">
              <span class="tag">${post.category}</span>
              <span class="post-title">${locked? '[비공개] ' : ''}${escapeHtml(post.title)}</span>
            </div>
            <div class="meta">작성자: ${escapeHtml(post.nickname||'익명')} · ${formatTime(post.createdAt)} ${answerDone? ' · <span style="color:#0d7a43;font-weight:700">[답변완료]</span>':''}</div>
          </div>
          <div class="post-body">
            <div>
              ${locked? '<em class="hint">비공개 글입니다. 열람하려면 비밀번호를 입력하세요.</em>' : nl2br(escapeHtml(post.content))}
            </div>
            ${post.links?.length? `<div style="margin-top:10px" class="grid">${post.links.map(u=>`<a href="${u}" target="_blank" rel="noopener">🔗 ${u}</a>`).join('')}</div>`:''}
            ${post.images?.length? `<div class="files" style="margin-top:10px">${post.images.map(src=>`<img class="preview" alt="첨부 이미지" src="${src}">`).join('')}</div>`:''}

            <div class="toolbar" style="margin-top:12px; flex-wrap:wrap">
              <button class="btn-ghost" data-act="view">${locked? '비공개 글 보기' : '내용 접기'}</button>
              <button class="btn-ghost" data-act="edit">수정</button>
              <button class="btn-ghost danger" data-act="del">삭제</button>
            </div>

            <div class="grid" style="margin-top:10px" ${answerDone? '':'hidden'}>
              <div class="tag">관리자 답변</div>
              <div>${answerDone? nl2br(escapeHtml(post.answer.text)) : ''}</div>
              ${answerDone? `<div class="meta">by ${escapeHtml(post.answer.adminName||'관리자')} · ${formatTime(post.answer.createdAt)}</div>`:''}
            </div>
          </div>
        `;

        const head = $('.post-head', el);
        head.addEventListener('click',()=>{ el.classList.toggle('open'); });

        // 버튼 핸들링
        el.addEventListener('click', (e)=>{
          const b = e.target.closest('button'); if(!b) return;
          const act = b.dataset.act;
          if(act==='view'){
            if(post.secret){
              const pw = prompt('비공개 글입니다. 비밀번호를 입력하세요.');
              if(pw && pw === post.password){
                const body = $('.post-body', el);
                body.querySelector('div').innerHTML = nl2br(escapeHtml(post.content));
                b.textContent = '내용 접기';
              } else if(pw!==null) { alert('비밀번호가 일치하지 않습니다.'); }
            } else {
              el.classList.remove('open');
            }
          }
          if(act==='edit'){
            const pw = prompt('수정 비밀번호를 입력하세요.');
            if(pw===post.password){ openAskModal(post) } else if(pw!==null){ alert('비밀번호가 일치하지 않습니다.'); }
          }
          if(act==='del'){
            const pw = prompt('삭제 비밀번호를 입력하세요.');
            if(pw===post.password){ if(confirm('정말 삭제하시겠어요?')){ removePost(post.id); } } else if(pw!==null){ alert('비밀번호가 일치하지 않습니다.'); }
          }
        });

        listWrap.appendChild(el);
      });
    }

    function removePost(id){
      const posts = LS.get('qnaPosts', []);
      LS.set('qnaPosts', posts.filter(p=>p.id!==id));
      renderQna();
    }

    function savePost(post){
      const posts = LS.get('qnaPosts', []);
      const idx = posts.findIndex(p=>p.id===post.id);
      if(idx>-1) posts[idx]=post; else posts.push(post);
      LS.set('qnaPosts', posts);
    }

    // ---------- 모달/폼 ----------
    const askModal = $('#askModal');
    $('#btnAsk').addEventListener('click', ()=> openAskModal());
    $('#btnCloseAsk').addEventListener('click', closeAskModal);

    function openAskModal(post){
      askModal.classList.add('open');
      askModal.setAttribute('aria-hidden','false');
      const linkBox = $('#linkBox'); linkBox.innerHTML=''; addLinkField(post?.links||[]);
      $('#previews').innerHTML=''; $('#photos').value='';
      if(post){
        state.editingId = post.id;
        $('#title').value = post.title;
        $('#category').value = post.category;
        $('#nickname').value = post.nickname||'';
        $('#content').value = post.content;
        $('#secret').checked = !!post.secret;
        (post.images||[]).forEach(src=>{
          const img = document.createElement('img'); img.src=src; img.className='preview'; $('#previews').append(img);
        });
      } else {
        state.editingId = null;
        $('#askForm').reset();
      }
    }
    function closeAskModal(){
      askModal.classList.remove('open');
      askModal.setAttribute('aria-hidden','true');
    }

    // 링크 필드 동적 추가
    $('#btnAddLink').addEventListener('click', ()=> addLinkField());
    function addLinkField(values=[]){
      const linkBox = $('#linkBox');
      const group = document.createElement('div');
      group.className='field-inline';
      group.innerHTML = `
        <input type="url" placeholder="https:// 예) 참고 링크" value="${values[0]||''}" />
        <button type="button" class="btn-ghost" >삭제</button>
      `;
      group.querySelector('button').addEventListener('click',()=> group.remove());
      linkBox.appendChild(group);
    }

    // 이미지 프리뷰 + 용량 체크
    $('#photos').addEventListener('change', (e)=>{
      const files = Array.from(e.target.files).slice(0,3);
      const box = $('#previews'); box.innerHTML='';
      for(const f of files){
        if(f.size > 2*1024*1024){ alert('이미지 '+f.name+' 이(가) 2MB를 초과하여 제외됩니다.'); continue; }
        const reader = new FileReader();
        reader.onload = ev => {
          const img = document.createElement('img'); img.className='preview'; img.src = ev.target.result; box.appendChild(img);
        };
        reader.readAsDataURL(f);
      }
    });

    // 제출 처리
    $('#askForm').addEventListener('submit', (e)=>{
      e.preventDefault();
      const title = $('#title').value.trim();
      const category = $('#category').value;
      const nickname = $('#nickname').value.trim();
      const password = $('#writerPass').value;
      const content = $('#content').value.trim();
      const secret = $('#secret').checked;
      if(!title || !content || !password){ alert('제목/내용/비밀번호는 필수입니다.'); return; }

      const links = $$('#linkBox input').map(i=>i.value.trim()).filter(Boolean);
      const images = $$('#previews img').map(img=>img.src).slice(0,3);

      const now = Date.now();
      const post = state.editingId ? LS.get('qnaPosts', []).find(p=>p.id===state.editingId) : { id: uid(), createdAt: now };
      Object.assign(post, { title, category, nickname, content, images, links, secret, password, updatedAt: now });
      savePost(post);
      closeAskModal();
      switchTab('qna');
      setTimeout(()=>{ alert('등록되었습니다. QnA 목록에서 확인하세요.'); }, 50);
    });

    // ---------- 탭 ----------
    function switchTab(name){
      const faqOn = name==='faq';
      $('#faqSection').hidden = !faqOn; $('#qnaSection').hidden = faqOn;
      $('#tab-faq').classList.toggle('active', faqOn); $('#tab-qna').classList.toggle('active', !faqOn);
      if(!faqOn) renderQna(); else renderFaq();
      window.scrollTo({top:0,behavior:'smooth'});
    }
    $('#tab-faq').addEventListener('click', ()=> switchTab('faq'));
    $('#tab-qna').addEventListener('click', ()=> switchTab('qna'));

    // ---------- helpers ----------
    function escapeHtml(str){ return (str||'').replace(/[&<>"']/g, m=>({"&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;"}[m])) }
    function nl2br(str){ return (str||'').replace(/\n/g,'<br>') }

    // 초기 렌더
    renderFaq();