document.addEventListener("DOMContentLoaded", () => {

  // 관리자 삭제 버튼
  document.querySelectorAll(".admin-delete-funeral").forEach(btn => {
    btn.addEventListener("click", async e => {
      e.preventDefault();

      if (!confirm("정말 이 예약을 삭제하시겠습니까?")) return;

      const id = btn.dataset.id;          // 예약 ID
      const userSeq = btn.dataset.userseq; // 예약 소유자 userSeq

      try {
        const res = await fetch(`/reserve/admin/reserve/funeral_reserve/${id}`, {
          method: "DELETE"
        });

        if (res.ok) {
          alert("예약이 관리자 권한으로 삭제되었습니다.");
          location.reload();
        } else {
          const msg = await res.text();
          alert(msg || "삭제 중 오류가 발생했습니다.");
        }

      } catch (err) {
        console.error("관리자 삭제 오류:", err);
        alert("서버 통신 중 오류가 발생했습니다.");
      }
    });
  });

  // 관리자 삭제 버튼
    document.querySelectorAll(".admin-delete-psy").forEach(btn => {
      btn.addEventListener("click", async e => {
        e.preventDefault();

        if (!confirm("정말 이 예약을 삭제하시겠습니까?")) return;

        const id = btn.dataset.id;          // 예약 ID
        const userSeq = btn.dataset.userseq; // 예약 소유자 userSeq

        try {
          const res = await fetch(`/reserve/admin/reserve/psy_reserve/${id}`, {
            method: "DELETE"
          });

          if (res.ok) {
            alert("예약이 관리자 권한으로 삭제되었습니다.");
            location.reload();
          } else {
            const msg = await res.text();
            alert(msg || "삭제 중 오류가 발생했습니다.");
          }

        } catch (err) {
          console.error("관리자 삭제 오류:", err);
          alert("서버 통신 중 오류가 발생했습니다.");
        }
      });
    });

    // 관리자 삭제 버튼
      document.querySelectorAll(".admin-delete-goods").forEach(btn => {
        btn.addEventListener("click", async e => {
          e.preventDefault();

          if (!confirm("정말 이 예약을 삭제하시겠습니까?")) return;

          const id = btn.dataset.id;          // 예약 ID
          const userSeq = btn.dataset.userseq; // 예약 소유자 userSeq

          try {
            const res = await fetch(`/reserve/admin/reserve/goods_reserve/${id}`, {
              method: "DELETE"
            });

            if (res.ok) {
              alert("예약이 관리자 권한으로 삭제되었습니다.");
              location.reload();
            } else {
              const msg = await res.text();
              alert(msg || "삭제 중 오류가 발생했습니다.");
            }

          } catch (err) {
            console.error("관리자 삭제 오류:", err);
            alert("서버 통신 중 오류가 발생했습니다.");
          }
        });
      });
    });


