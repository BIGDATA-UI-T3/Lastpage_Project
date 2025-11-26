 document.addEventListener("DOMContentLoaded", () => {

      // 인삿말 메시지
      const hours = new Date().getHours();
      const msg =
        hours < 12
          ? "님, 오늘도 함께 걸어가겠습니다 ☀️"
          : hours < 18
          ? "님, 따뜻한 오후 보내세요 🌼"
          : "님, 편안한 저녁 되세요 🌙";
      const msgEl = document.getElementById("timeMessage");
      if (msgEl) msgEl.textContent = msg;

      // 스크롤 애니메이션 (IntersectionObserver)
      const boxes = document.querySelectorAll(".effect_yj");
      if (boxes.length > 0) {
        const observer = new IntersectionObserver(
          (entries, obs) => {
            entries.forEach(entry => {
              if (entry.isIntersecting) {
                entry.target.classList.add("show");
                obs.unobserve(entry.target);
              }
            });
          },
          { threshold: 0.2 }
        );
        boxes.forEach(box => observer.observe(box));
      }

      //상담예약 삭제 기능
      document.querySelectorAll(".psy-delete").forEach(btn => {
        btn.addEventListener("click", async e => {
          e.preventDefault();
          if (!confirm("상담 예약을 삭제하시겠습니까?")) return;
          const id = btn.dataset.id;
          const res = await fetch(`/reserve/psy_reserve/${id}`, { method: "DELETE" });
          if (res.ok) {
            alert("상담 예약이 삭제되었습니다.");
            location.reload();
          } else {
            alert((await res.text()) || "삭제 중 오류가 발생했습니다.");
          }
        });
      });

      //굿즈 예약 삭제 기능
      document.querySelectorAll(".goods-delete").forEach(btn => {
        btn.addEventListener("click", async e => {
          e.preventDefault();
          if (!confirm("굿즈 예약을 삭제하시겠습니까?")) return;
          const id = btn.dataset.id;
          const res = await fetch(`/reserve/goods_reserve/${id}`, { method: "DELETE" });
          if (res.ok) {
            alert("굿즈 예약이 삭제되었습니다.");
            location.reload();
          } else {
            alert((await res.text()) || "삭제 중 오류가 발생했습니다.");
          }
        });
      });

      // 장례예약 삭제 기능
      document.querySelectorAll(".funeral-delete").forEach(btn => {
        btn.addEventListener("click", async e => {
          e.preventDefault();
          if (!confirm("장례 예약을 삭제하시겠습니까?")) return;
          const id = btn.dataset.id;
          const res = await fetch(`/reserve/funeral_reserve/${id}`, { method: "DELETE" });
          if (res.ok) {
            alert("장례 예약이 삭제되었습니다.");
            location.reload();
          } else {
            alert((await res.text()) || "삭제 중 오류가 발생했습니다.");
          }
        });
      });
    });