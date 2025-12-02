package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.PsyReserveRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PsyReserveService {

    private final PsyReserveRepository repository;
    private final SignupRepository signupRepository;


    /* =========================================================
     *   [공용] 관리자/사용자 겸용 조회
     *   isAdmin=true → 소유권 검사 없이 조회 허용
     * ========================================================= */
    @Transactional(readOnly = true)
    public PsyReserveDto findByIdForAdminOrUser(Long id, String userSeq, boolean isAdmin) {

        PsyReserve entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        // 사용자일 때만 소유권 검사
        if (!isAdmin) {
            if (entity.getUser() == null ||
                    !entity.getUser().getUserSeq().equals(userSeq)) {

                log.warn("[조회 권한 오류] 예약ID={}, sessionUserSeq={}, 예약소유자={}",
                        id, userSeq,
                        entity.getUser() != null ? entity.getUser().getUserSeq() : "null-user");

                throw new SecurityException("본인의 예약만 조회할 수 있습니다.");
            }
        }

        return toDto(entity);
    }


    /* =========================================================
     *   [공용] 관리자/사용자 겸용 수정
     *   isAdmin=true → 소유권 검사 생략
     * ========================================================= */
    @Transactional
    public PsyReserveDto updateForAdminOrUser(Long id, PsyReserveDto updated,
                                              String requestUserSeq, boolean isAdmin) {

        PsyReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));


        // 사용자일 때만 소유권 검사
        if (!isAdmin) {
            if (existing.getUser() == null ||
                    !existing.getUser().getUserSeq().equals(requestUserSeq)) {

                throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
            }
        }

        // 시간 중복 체크
        boolean sameSlot = existing.getConsultDate().equals(updated.getConsultDate())
                && existing.getTime().equals(updated.getTime());

        if (!sameSlot &&
                repository.existsByConsultDateAndTime(updated.getConsultDate(), updated.getTime())) {

            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        // 수정 적용
        existing.setName(updated.getName());
        existing.setBirth(updated.getBirth());
        existing.setGender(updated.getGender());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setConsultDate(updated.getConsultDate());
        existing.setTime(updated.getTime());
        existing.setCounselor(updated.getCounselor());
        existing.setMemo(updated.getMemo());

        PsyReserve saved = repository.save(existing);

        log.info("[{} 상담예약 수정] ID={}, 수행자={}, 예약소유자={}",
                isAdmin ? "ADMIN" : "USER",
                id, requestUserSeq, existing.getUser().getUserSeq());

        return toDto(saved);
    }


    /* =========================================================
     *   [공용] 관리자/사용자 겸용 삭제
     *   isAdmin=true → 소유권 검사 생략
     * ========================================================= */
    @Transactional
    public boolean deleteForAdminOrUser(Long id, String requestUserSeq, boolean isAdmin) {

        PsyReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        // 사용자일 때만 권한 검사
        if (!isAdmin) {
            if (existing.getUser() == null ||
                    !existing.getUser().getUserSeq().equals(requestUserSeq)) {

                throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
            }
        }

        repository.delete(existing);
        repository.flush();

        log.info("[{} 상담예약 삭제] ID={}, 수행자={}, 예약소유자={}",
                isAdmin ? "ADMIN" : "USER",
                id, requestUserSeq, existing.getUser().getUserSeq());

        return true;
    }



    /* =========================================================
     *  ⬇⬇⬇ 기존 사용자 전용 로직 유지 (삭제 금지)
     * ========================================================= */


    /** 신규 등록 */
    @Transactional
    public PsyReserveDto saveReservation(PsyReserveDto dto) {

        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다: " + dto.getUserSeq()));

        if (repository.existsByConsultDateAndTime(dto.getConsultDate(), dto.getTime())) {
            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        PsyReserve entity = PsyReserve.builder()
                .user(user)
                .name(dto.getName())
                .birth(dto.getBirth())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .consultDate(dto.getConsultDate())
                .time(dto.getTime())
                .counselor(dto.getCounselor())
                .memo(dto.getMemo())
                .build();

        PsyReserve saved = repository.save(entity);

        log.info("[상담예약 등록 완료] user_seq={}, 날짜={}, 시간={}",
                user.getUserSeq(), saved.getConsultDate(), saved.getTime());

        return toDto(saved);
    }


    /** 사용자 전용 수정 (기존 유지) */
    @Transactional
    public PsyReserveDto updateReserve(Long id, PsyReserveDto updated, String userSeq) {

        PsyReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (!existing.getUser().getUserSeq().equals(userSeq)) {
            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
        }

        boolean sameSlot = existing.getConsultDate().equals(updated.getConsultDate())
                && existing.getTime().equals(updated.getTime());

        if (!sameSlot &&
                repository.existsByConsultDateAndTime(updated.getConsultDate(), updated.getTime())) {
            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        existing.setName(updated.getName());
        existing.setBirth(updated.getBirth());
        existing.setGender(updated.getGender());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setConsultDate(updated.getConsultDate());
        existing.setTime(updated.getTime());
        existing.setCounselor(updated.getCounselor());
        existing.setMemo(updated.getMemo());

        PsyReserve saved = repository.save(existing);
        return toDto(saved);
    }


    /** 사용자 전용 조회 */
    public PsyReserveDto findByUserSeq(String userSeq) {
        return repository.findByUser_UserSeq(userSeq)
                .map(this::toDto)
                .orElse(null);
    }

    public List<PsyReserveDto> findAllByUserSeq(String userSeq) {
        return repository.findAllByUser_UserSeq(userSeq)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    /** 관리자/사용자 공용 단건 조회 */
    public PsyReserveDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }


    /** 날짜별 예약된 시간 조회 */
    public List<String> getBookedTimesByDate(String date) {
        return repository.findByConsultDate(date)
                .stream()
                .map(PsyReserve::getTime)
                .collect(Collectors.toList());
    }


    /** 관리자 모니터링용 전체 조회 */
    public List<PsyReserveDto> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    /** 사용자 전용 삭제 */
    @Transactional
    public boolean deleteReserve(Long id, String userSeq) {

        PsyReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        if (existing.getUser() == null ||
                !existing.getUser().getUserSeq().equals(userSeq)) {

            throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
        }

        repository.delete(existing);
        repository.flush();

        return true;
    }


    public long countAll() {
        return repository.count();
    }

    public List<PsyReserveDto> findRecent5() {
        return repository.findTop5ByOrderByCreated_atDesc()
                .stream()
                .map(PsyReserveDto::fromEntity)
                .toList();
    }


    /* =========================================================
     *  DTO 변환
     * ========================================================= */
    private PsyReserveDto toDto(PsyReserve entity) {
        PsyReserveDto dto = new PsyReserveDto();

        dto.setId(entity.getId());

        if (entity.getUser() != null)
            dto.setUserSeq(entity.getUser().getUserSeq());

        dto.setName(entity.getName());
        dto.setBirth(entity.getBirth());
        dto.setGender(entity.getGender());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setAddress(entity.getAddress());
        dto.setConsultDate(entity.getConsultDate());
        dto.setTime(entity.getTime());
        dto.setCounselor(entity.getCounselor());
        dto.setMemo(entity.getMemo());

        return dto;
    }
}

//=============================================
//package com.example.demo.Domain.Common.Service;
//
//import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
//import com.example.demo.Domain.Common.Dto.PsyReserveDto;
//import com.example.demo.Domain.Common.Entity.GoodsReserve;
//import com.example.demo.Domain.Common.Entity.PsyReserve;
//import com.example.demo.Domain.Common.Entity.Signup;
//import com.example.demo.Repository.PsyReserveRepository;
//import com.example.demo.Repository.SignupRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;

//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PsyReserveService {
//
//    private final PsyReserveRepository repository;
//    private final SignupRepository signupRepository;
//
//    /** ------------------------------
//     *  신규 상담예약 등록 (회원 FK 기반)
//     * ------------------------------ */
//    @Transactional
//    public PsyReserveDto saveReservation(PsyReserveDto dto) {
//        // FK 검증
//        Signup user = signupRepository.findById(dto.getUserSeq())
//                .orElseThrow(() -> new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다: " + dto.getUserSeq()));
//
//        // 동일 시간 중복 검사
//        if (repository.existsByConsultDateAndTime(dto.getConsultDate(), dto.getTime())) {
//            throw new IllegalStateException("이미 예약된 시간입니다.");
//        }
//
//        // DTO → Entity 변환
//        PsyReserve entity = PsyReserve.builder()
//                .user(user)
//                .name(dto.getName())
//                .birth(dto.getBirth())
//                .gender(dto.getGender())
//                .phone(dto.getPhone())
//                .email(dto.getEmail())
//                .address(dto.getAddress())
//                .consultDate(dto.getConsultDate())
//                .time(dto.getTime())
//                .counselor(dto.getCounselor())
//                .memo(dto.getMemo())
//                .build();
//
//        PsyReserve saved = repository.save(entity);
//
//
//        log.info("[상담예약 등록 완료] user_seq={}, 날짜={}, 시간={}",
//                user.getUserSeq(), saved.getConsultDate(), saved.getTime());
//
//        return toDto(saved);
//    }
//
//    /** ------------------------------
//     *  상담예약 수정 (본인만 가능)
//     * ------------------------------ */
//    @Transactional
//    public PsyReserveDto updateReserve(Long id, PsyReserveDto updated, String userSeq) {
//        PsyReserve existing = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));
//
//        // 본인 예약인지 확인
//        if (!existing.getUser().getUserSeq().equals(userSeq)) {
//            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
//        }
//
//        boolean sameSlot = existing.getConsultDate().equals(updated.getConsultDate())
//                && existing.getTime().equals(updated.getTime());
//
//        if (!sameSlot && repository.existsByConsultDateAndTime(updated.getConsultDate(), updated.getTime())) {
//            throw new IllegalStateException("이미 예약된 시간입니다.");
//        }
//
//        existing.setName(updated.getName());
//        existing.setBirth(updated.getBirth());
//        existing.setGender(updated.getGender());
//        existing.setPhone(updated.getPhone());
//        existing.setAddress(updated.getAddress());
//        existing.setConsultDate(updated.getConsultDate());
//        existing.setTime(updated.getTime());
//        existing.setCounselor(updated.getCounselor());
//        existing.setMemo(updated.getMemo());
//
//        PsyReserve saved = repository.save(existing);
//
//        log.info("[상담예약 수정 완료] ID={}, user_seq={}, 날짜={}, 시간={}",
//                id, userSeq, saved.getConsultDate(), saved.getTime());
//
//        return toDto(saved);
//    }
//
//    /** ------------------------------
//     *  user_seq 기준 단건 예약 조회 (마이페이지용)
//     * ------------------------------ */
//    public PsyReserveDto findByUserSeq(String userSeq) {
//        Optional<PsyReserve> result = repository.findByUser_UserSeq(userSeq);
//        if (result.isEmpty()) {
//            log.info("[예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
//            return null;
//        }
//        return toDto(result.get());
//    }
//
////    /** ------------------------------
////     *  user_seq 기준 다건 예약 조회 (마이페이지용)
////     * ------------------------------ */
//public List<PsyReserveDto> findAllByUserSeq(String userSeq) {
//    List<PsyReserve> list = repository.findAllByUser_UserSeq(userSeq);
//    if (list.isEmpty()) {
//        log.info("[심리 예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
//        return Collections.emptyList();
//    }
//    log.info("[심리 예약 조회 완료] user_seq={}, {}건", userSeq, list.size());
//    return list.stream().map(this::toDto).collect(Collectors.toList());
//}
//
//    /** ------------------------------
//     *  ID 기준 예약 조회 (수정폼 진입용)
//     * ------------------------------ */
//    public PsyReserveDto findById(Long id) {
//        return repository.findById(id)
//                .map(this::toDto)
//                .orElse(null);
//    }
//
//    /** ------------------------------
//     *  날짜별 예약된 시간 목록 조회 (시간 선택 비활성화용)
//     * ------------------------------ */
//    public List<String> getBookedTimesByDate(String date) {
//        return repository.findByConsultDate(date)
//                .stream()
//                .map(PsyReserve::getTime)
//                .collect(Collectors.toList());
//    }
//
//    /** ------------------------------
//     *  전체 예약 조회 (관리자용)
//     * ------------------------------ */
//    public List<PsyReserveDto> findAll() {
//        return repository.findAll()
//                .stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }
//
//    /** ------------------------------
//     *  상담예약 삭제 (본인만 가능)
//     * ------------------------------ */
//    @Transactional
//    public boolean deleteReserve(Long id, String userSeq) {
//        PsyReserve existing = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));
//
//        if (existing.getUser() == null || !existing.getUser().getUserSeq().equals(userSeq)) {
//            log.info("ID: {}, user_seq={}", id, userSeq);
//            throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
//        }
//
//        repository.delete(existing);
//        repository.flush();  // 강제로 DB 반영
//        log.info("[상담예약 삭제 완료] ID={}, user_seq={}", id, userSeq);
//        return true;
//    }
//
//    public long countAll() {
//        return repository.count();
//    }
//
//    public List<PsyReserveDto> findRecent5() {
//        return repository.findTop5ByOrderByCreated_atDesc()
//                .stream()
//                .map(PsyReserveDto::fromEntity)
//                .toList();
//    }
//
//    /** ------------------------------
//     *  Entity → DTO 변환
//     * ------------------------------ */
//    private PsyReserveDto toDto(PsyReserve entity) {
//        PsyReserveDto dto = new PsyReserveDto();
//        dto.setId(entity.getId());
//        dto.setUserSeq(entity.getUser().getUserSeq());
//        dto.setName(entity.getName());
//        dto.setBirth(entity.getBirth());
//        dto.setGender(entity.getGender());
//        dto.setPhone(entity.getPhone());
//        dto.setEmail(entity.getEmail());
//        dto.setAddress(entity.getAddress());
//        dto.setConsultDate(entity.getConsultDate());
//        dto.setTime(entity.getTime());
//        dto.setCounselor(entity.getCounselor());
//        dto.setMemo(entity.getMemo());
//        return dto;
//    }
//
//
//}
