document.addEventListener("DOMContentLoaded", () => {

  let isIdChecked = false;
  let isEmailVerified = false;
  let tempEmail = "";

  /* ----------------------------------------------------
   *  이메일 도메인 선택
   * ---------------------------------------------------- */
  document.querySelector('.domain-select').addEventListener('change', e => {
    const domainInput = document.getElementById('emailDomain');
    domainInput.value = e.target.value === '직접입력' ? '' : e.target.value;
  });

  /* ----------------------------------------------------
   *  아이디 중복검사
   * ---------------------------------------------------- */
  document.getElementById('checkIdBtn').addEventListener('click', async () => {
    const id = document.getElementById('userid').value.trim();
    if (!id) return alert("아이디를 입력하세요.");

    const res = await fetch(`/signup/checkDuplicateId?id=${id}`);
    const text = await res.text();

    const msg = document.getElementById('idCheckMsg');

    if (text === 'duplicate') {
      msg.textContent = "이미 사용 중인 아이디입니다.";
      msg.className = 'invalid-msg';
      isIdChecked = false;
    } else {
      msg.textContent = "사용 가능한 아이디입니다.";
      msg.className = 'valid-msg';
      isIdChecked = true;
    }
  });

  /* ----------------------------------------------------
   *  비밀번호 일치/유효성 검사
   * ---------------------------------------------------- */
  const pw = document.getElementById('password');
  const cpw = document.getElementById('confirm_password');
  const pwMsg = document.getElementById('pwMsg');

  cpw.addEventListener('input', () => {
    const valid = pw.value.length >= 9 && /[A-Z]/.test(pw.value) && /[!@#$%^&*]/.test(pw.value);

    if (!valid) {
      pwMsg.textContent = '대문자·특수문자 포함 9자 이상이어야 합니다.';
      pwMsg.className = 'invalid-msg';
      return;
    }

    if (pw.value === cpw.value) {
      pwMsg.textContent = '비밀번호가 일치합니다.';
      pwMsg.className = 'valid-msg';
    } else {
      pwMsg.textContent = '비밀번호가 일치하지 않습니다.';
      pwMsg.className = 'invalid-msg';
    }
  });

  /* ----------------------------------------------------
   *  이메일 인증코드 발송
   * ---------------------------------------------------- */
  document.getElementById('sendEmailBtn').addEventListener('click', async () => {
    const email = document.getElementById('emailId').value + '@' + document.getElementById('emailDomain').value;

    if (!email.includes('@')) {
      return alert('이메일 주소를 올바르게 입력해주세요.');
    }

    const res = await fetch('/signup/sendEmailCode', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });

    if (res.ok) {
      alert('인증메일이 발송되었습니다.');
      tempEmail = email;
      document.querySelector('.email-verify').style.display = 'flex';
    } else {
      alert('이메일 전송에 실패했습니다.');
    }
  });

  /* ----------------------------------------------------
   *  이메일 인증번호 확인
   * ---------------------------------------------------- */
  document.getElementById('verifyEmailBtn').addEventListener('click', async () => {
    const code = document.getElementById('emailCode').value.trim();

    const res = await fetch('/signup/verifyEmailCode', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: tempEmail, code })
    });

    const text = await res.text();

    if (text === 'success') {
      alert('이메일 인증 완료!');
      isEmailVerified = true;
    } else {
      alert('인증번호가 일치하지 않습니다.');
    }
  });

  /* ----------------------------------------------------
   *  회원가입 요청
   * ---------------------------------------------------- */
  document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!isIdChecked) return alert('아이디 중복확인을 해주세요.');
    if (!isEmailVerified) return alert('이메일 인증을 완료해주세요.');

    const data = {
      name: document.getElementById('name').value.trim(),
      id: document.getElementById('userid').value.trim(),
      password: document.getElementById('password').value,
      confirm_password: document.getElementById('confirm_password').value,
      emailId: document.getElementById('emailId').value.trim(),
      emailDomain: document.getElementById('emailDomain').value.trim(),
      year: document.getElementById('year').value,
      month: document.getElementById('month').value,
      day: document.getElementById('day').value,
      gender: document.querySelector('input[name="gender"]:checked').value,
      phone_num: document.getElementById('phone').value.trim()
    };

    try {
      const res = await fetch('/signup/userinfoSave', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (!res.ok) {
        const msg = await res.text();
        return alert("회원가입 실패: " + msg);
      }

      const userSeq = await res.text();
      console.log("회원가입 성공 userSeq:", userSeq);

      alert("회원가입 완료! 자동 로그인 됩니다.");

      // 회원가입 성공 → 이미 Security + Session 등록됨
      window.location.href = "/";
      
    } catch (error) {
      console.error(error);
      alert("서버 통신 오류가 발생했습니다.");
    }
  });

});
