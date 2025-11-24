package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.FuneralReserveRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class FuneralReserveService {

    private final FuneralReserveRepository repository;
    private final SignupRepository signupRepository;

    /* =========================================================
     *  [공용] 관리자/사용자 겸용 조회
     *  isAdmin=true → 소유권 검사 없이 조회 허용
     * ========================================================= */
    @Transactional(readOnly = true)
    public FuneralReserveDto findByIdForAdminOrUser(Long id, String userSeq, boolean isAdmin) {

        FuneralReserve entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (!isAdmin) {

            log.info("isAdminRequest={}", isAdmin);
            // 사용자일 때만 소유권 검사
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
     *  [공용] 관리자/사용자 겸용 수정
     *  isAdmin=true → 소유권 검사 생략
     * ========================================================= */
    @Transactional
    public FuneralReserveDto updateForAdminOrUser(Long id, FuneralReserveDto updated,
                                                  String requestUserSeq, boolean isAdmin) {

        FuneralReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (!isAdmin) {
            if (existing.getUser() == null ||
                    !existing.getUser().getUserSeq().equals(requestUserSeq)) {
                throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
            }
        }

        // 기존 수정 로직 재사용
        existing.setOwnerName(updated.getOwnerName());
        existing.setOwnerPhone(updated.getOwnerPhone());
        existing.setOwnerEmail(updated.getOwnerEmail());
        existing.setOwnerAddr(updated.getOwnerAddr());
        existing.setPetName(updated.getPetName());
        existing.setPetType(updated.getPetType());
        existing.setPetBreed(updated.getPetBreed());
        existing.setPetWeight(updated.getPetWeight());
        existing.setPassedAt(updated.getPassedAt());
        existing.setPlace(updated.getPlace());
        existing.setFuneralDate(updated.getFuneralDate());
        existing.setType(updated.getType());
        existing.setAsh(updated.getAsh());
        existing.setPickup(updated.getPickup());
        existing.setPickupAddr(updated.getPickupAddr());
        existing.setPickupTime(updated.getPickupTime());
        existing.setTime(updated.getTime());
        existing.setMemo(updated.getMemo());

        FuneralReserve saved = repository.save(existing);

        log.info("[{} 예약 수정] ID={}, 수행자={}, 예약소유자={}",
                isAdmin ? "ADMIN" : "USER",
                id, requestUserSeq, existing.getUser().getUserSeq());

        return toDto(saved);
    }

    /* =========================================================
     *  [공용] 관리자/사용자 겸용 삭제
     *  isAdmin=true → 소유권 검사 생략
     * ========================================================= */
    @Transactional
    public boolean deleteForAdminOrUser(Long id, String requestUserSeq, boolean isAdmin) {

        FuneralReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        if (!isAdmin) {
            if (existing.getUser() == null ||
                    !existing.getUser().getUserSeq().equals(requestUserSeq)) {

                log.warn("[삭제 권한 오류] ID={}, sessionUserSeq={}, 예약소유자={}",
                        id, requestUserSeq,
                        existing.getUser() != null ? existing.getUser().getUserSeq() : "null-user");

                throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
            }
        }

        repository.delete(existing);
        repository.flush();

        log.info("[{} 예약 삭제] ID={}, 수행자={}, 예약소유자={}",
                isAdmin ? "ADMIN" : "USER",
                id, requestUserSeq, existing.getUser().getUserSeq());

        return true;
    }


    /* =========================================================
     *  ⬇⬇⬇⬇⬇ 기존 사용자 전용 로직은 그대로 유지 (삭제 금지)
     * ========================================================= */

    /** 저장 */
    public FuneralReserveDto saveReservation(FuneralReserveDto dto) {
        log.info("[DEBUG 예약 저장 진입] dto.userSeq={}, ownerName={}, petName={}",
                dto.getUserSeq(), dto.getOwnerName(), dto.getPetName());

        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다: " + dto.getUserSeq()));

        FuneralReserve entity = FuneralReserve.builder()
                .user(user)
                .ownerName(dto.getOwnerName())
                .ownerPhone(dto.getOwnerPhone())
                .ownerEmail(dto.getOwnerEmail())
                .ownerAddr(dto.getOwnerAddr())
                .petName(dto.getPetName())
                .petType(dto.getPetType())
                .petBreed(dto.getPetBreed())
                .petWeight(dto.getPetWeight())
                .passedAt(dto.getPassedAt())
                .place(dto.getPlace())
                .funeralDate(dto.getFuneralDate())
                .type(dto.getType())
                .ash(dto.getAsh())
                .pickup(dto.getPickup())
                .pickupAddr(dto.getPickupAddr())
                .pickupTime(dto.getPickupTime())
                .time(dto.getTime())
                .memo(dto.getMemo())
                .build();

        FuneralReserve saved = repository.save(entity);

        log.info("[장례 예약 등록 완료] id={}, user_seq={}", saved.getId(),
                saved.getUser() != null ? saved.getUser().getUserSeq() : "null-user");

        return toDto(saved);
    }


//    /** 사용자 전용 수정 (기존 유지) */
//    @Transactional
//    public FuneralReserveDto updateReserve(Long id, FuneralReserveDto updated, String userSeq) {
//        FuneralReserve existing = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));
//
//        if (existing.getUser() == null || !existing.getUser().getUserSeq().equals(userSeq)) {
//            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
//        }
//
//        existing.setOwnerName(updated.getOwnerName());
//        existing.setOwnerPhone(updated.getOwnerPhone());
//        existing.setOwnerEmail(updated.getOwnerEmail());
//        existing.setOwnerAddr(updated.getOwnerAddr());
//        existing.setPetName(updated.getPetName());
//        existing.setPetType(updated.getPetType());
//        existing.setPetBreed(updated.getPetBreed());
//        existing.setPetWeight(updated.getPetWeight());
//        existing.setPassedAt(updated.getPassedAt());
//        existing.setPlace(updated.getPlace());
//        existing.setFuneralDate(updated.getFuneralDate());
//        existing.setType(updated.getType());
//        existing.setAsh(updated.getAsh());
//        existing.setPickup(updated.getPickup());
//        existing.setPickupAddr(updated.getPickupAddr());
//        existing.setPickupTime(updated.getPickupTime());
//        existing.setTime(updated.getTime());
//        existing.setMemo(updated.getMemo());
//
//        FuneralReserve saved = repository.save(existing);
//        return toDto(saved);
//    }


    /** 조회 */
    public FuneralReserveDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public FuneralReserveDto findByUserSeq(String userSeq) {
        return repository.findByUser_UserSeq(userSeq).map(this::toDto).orElse(null);
    }

    public List<FuneralReserveDto> findAllByUserSeq(String userSeq) {
        return repository.findAllByUser_UserSeq(userSeq)
                .stream().map(this::toDto).toList();
    }


    /** 관리자 단건 조회 */
    public FuneralReserveDto findByIdAndUserSeq(Long id, String userSeq) {
        return repository.findByIdAndUser_UserSeq(id, userSeq).map(this::toDto).orElse(null);
    }


//    /** 삭제 */
//    @Transactional
//    public boolean deleteReserve(Long id, String userSeq) {
//        FuneralReserve existing = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));
//
//        if (!existing.getUser().getUserSeq().equals(userSeq)) {
//            throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
//        }
//
//        repository.delete(existing);
//        repository.flush();
//        return true;
//    }


    /* =========================================================
     * DTO 변환
     * ========================================================= */
    private FuneralReserveDto toDto(FuneralReserve entity) {
        FuneralReserveDto dto = new FuneralReserveDto();
        dto.setId(entity.getId());
        dto.setOwnerName(entity.getOwnerName());
        dto.setOwnerPhone(entity.getOwnerPhone());
        dto.setOwnerEmail(entity.getOwnerEmail());
        dto.setOwnerAddr(entity.getOwnerAddr());
        dto.setPetName(entity.getPetName());
        dto.setPetType(entity.getPetType());
        dto.setPetBreed(entity.getPetBreed());
        dto.setPetWeight(entity.getPetWeight());
        dto.setPassedAt(entity.getPassedAt());
        dto.setPlace(entity.getPlace());
        dto.setFuneralDate(entity.getFuneralDate());
        dto.setType(entity.getType());
        dto.setAsh(entity.getAsh());
        dto.setPickup(entity.getPickup());
        dto.setPickupAddr(entity.getPickupAddr());
        dto.setPickupTime(entity.getPickupTime());
        dto.setTime(entity.getTime());
        dto.setMemo(entity.getMemo());

        if (entity.getUser() != null)
            dto.setUserSeq(entity.getUser().getUserSeq());

        return dto;
    }
}
