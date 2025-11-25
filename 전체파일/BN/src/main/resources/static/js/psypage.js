document.addEventListener('DOMContentLoaded', () => {
    // 후기 슬라이더 로직
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');
    let idx = 0;
    let timer;

    function showSlide(n) {
        slides.forEach(s => s.classList.remove('active'));
        dots.forEach(d => d.classList.remove('active'));

        slides[n].classList.add('active');
        dots[n].classList.add('active');
        idx = n;
    }

    function nextSlide() {
        showSlide((idx + 1) % slides.length);
    }

    function startAutoplay() {
        clearInterval(timer);
        timer = setInterval(nextSlide, 4000);
    }

    dots.forEach((dot, i) => {
        dot.addEventListener('click', () => {
            showSlide(i);
            startAutoplay();
        });
    });

    if (slides.length > 0) {
        startAutoplay();
    }
});