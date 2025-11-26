

    const mediaArea = document.getElementById('mediaArea');
    const fileInput = document.getElementById('fileInput');
    const countBadge = document.getElementById('countBadge');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const caption = document.getElementById('caption');
    const charCount = document.getElementById('charCount');
    const shareBtn = document.getElementById('shareBtn');

    const MAX = 10;
    let files = [];
    let idx = 0;

    mediaArea.addEventListener('click', () => fileInput.click());
    mediaArea.addEventListener('keydown', (e)=>{ if(e.key==='Enter' || e.key===' '){ e.preventDefault(); fileInput.click(); }});

    fileInput.addEventListener('change', handleFiles);

    function handleFiles(){
      const picked = Array.from(fileInput.files || []);
      files = (files.concat(picked)).slice(0, MAX);
      idx = 0;
      renderPreviews();
      updateShareState();
      fileInput.value = '';
    }

    function renderPreviews(){
      Array.from(mediaArea.querySelectorAll('img.preview')).forEach(n=>n.remove());
      if(files.length){
        countBadge.hidden = false;
        countBadge.textContent = `${files.length} / ${MAX}`;
      } else {
        countBadge.hidden = true;
      }
      prevBtn.style.display = files.length>1 ? 'grid':'none';
      nextBtn.style.display = files.length>1 ? 'grid':'none';

      files.forEach((f,i)=>{
        const img = document.createElement('img');
        img.className = 'preview' + (i===idx ? ' active':'');
        img.alt = `업로드 이미지 ${i+1}`;
        img.decoding = 'async';
        img.loading = 'lazy';
        const reader = new FileReader();
        reader.onload = e => {img.src = e.target.result;};
        reader.readAsDataURL(f);
        mediaArea.appendChild(img);
      });
      updateActive();
    }

    function updateActive(){
      const imgs = mediaArea.querySelectorAll('img.preview');
      imgs.forEach((im,i)=> im.classList.toggle('active', i===idx));
      mediaArea.querySelector('.uploader-hint').style.display = files.length ? 'none':'grid';
    }

    prevBtn.addEventListener('click', (e)=>{ e.stopPropagation(); if(!files.length) return; idx = (idx-1+files.length)%files.length; updateActive();});
    nextBtn.addEventListener('click', (e)=>{ e.stopPropagation(); if(!files.length) return; idx = (idx+1)%files.length; updateActive();});

    caption.addEventListener('input', ()=>{
      if(caption.value.length>200) caption.value = caption.value.slice(0,200);
      charCount.textContent = `${caption.value.length} / 200`;
      updateShareState();
    });

    function updateShareState(){
      shareBtn.disabled = !(files.length || caption.value.trim().length);
    }

    shareBtn.addEventListener('click', ()=>{
      const formData = new FormData();
      files.forEach((f,i)=> formData.append('images', f, f.name || `image_${i}.jpg`));
      formData.append('caption', caption.value.trim());

      alert('게시물이 공유되었습니다!');

      files = []; idx = 0; caption.value=''; charCount.textContent='0 / 200';
      renderPreviews(); updateShareState();
    });
    