 // 섹션 페이드업 등장
        const io = new IntersectionObserver((entries) => {
            entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('on'); io.unobserve(e.target); } });
        }, { threshold: .15 });
        document.querySelectorAll('.reveal').forEach(el => io.observe(el));

        // 후기 페이드 슬라이더
        const slides = [...document.querySelectorAll('.slide')];
        const dots = [...document.querySelectorAll('.dot')];
        let idx = 0, timer;
        const show = (i) => {
            slides.forEach(s => s.classList.remove('active'));
            dots.forEach(d => d.classList.remove('active'));
            slides[i].classList.add('active');
            dots[i].classList.add('active');
            idx = i;
        };
        const next = () => show((idx + 1) % slides.length);
        const autoplay = () => { clearInterval(timer); timer = setInterval(next, 3800); };
        dots.forEach((d, i) => d.addEventListener('click', () => { show(i); autoplay(); }));
        autoplay();

        (function () {
            const header = document.querySelector('header');
            window.addEventListener('scroll', () => {
                if (window.scrollY > 200) {
                    header.style.top = '-120px';
                } else {
                    header.style.top = '0';
                }
            });
        })();