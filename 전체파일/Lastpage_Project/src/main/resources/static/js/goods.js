// 컨텐츠 박스 스크롤 효과
        const boxes = document.querySelectorAll('.effect_yj');

        const observer = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('show');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.2 });

        boxes.forEach(effect_yj => {
            observer.observe(effect_yj);
        });