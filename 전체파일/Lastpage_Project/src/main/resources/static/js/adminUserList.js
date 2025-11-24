 function deleteUser(userSeq) {
        if (!confirm("정말로 해당 회원을 삭제하시겠습니까?")) return;

        fetch(`/admin/user/${userSeq}`, {
            method: "DELETE"
        })
            .then(res => {
                if (res.ok) {
                    alert("삭제되었습니다.");
                    location.reload();
                } else {
                    alert("삭제 실패");
                }
            })
            .catch(() => alert("서버 오류 발생"));
    }