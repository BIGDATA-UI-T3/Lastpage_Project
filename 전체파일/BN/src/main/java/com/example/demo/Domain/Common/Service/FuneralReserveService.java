package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.FuneralReserveRepository;
import com.example.demo.Repository.PsyReserveRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FuneralReserveService {

    private final FuneralReserveRepository repository ;
    private final SignupRepository signupRepository;

    public FuneralReserveDto saveReservation(FuneralReserveDto dto) {
        // FK 검증
        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다: " + dto.getUserSeq()));

        // 동일 시간 중복 검사
//        if (repository.existsByConsultDateAndTime(dto.getConsultDate(), dto.getTime())) {
//            throw new IllegalStateException("이미 예약된 시간입니다.");
//        }

        // DTO → Entity 변환
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

        log.info("[장례 예약 등록 완료] user_seq={}",
                user.getUserSeq());

        return toDto(saved);
    }

    /** ------------------------------
     *  상담예약 수정 (본인만 가능)
     * ------------------------------ */
    @Transactional
    public FuneralReserveDto updateReserve(Long id, FuneralReserveDto updated, String userSeq) {
        FuneralReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        // 본인 예약인지 확인
        if (!existing.getUser().getUserSeq().equals(userSeq)) {
            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
        }

//        boolean sameSlot = existing.getConsultDate().equals(updated.getConsultDate())
//                && existing.getTime().equals(updated.getTime());
//
//        if (!sameSlot && repository.existsByConsultDateAndTime(updated.getConsultDate(), updated.getTime())) {
//            throw new IllegalStateException("이미 예약된 시간입니다.");
//        }

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

        log.info("[장례 예약 수정 완료] ID={}, user_seq={}",
                id, userSeq);

        return toDto(saved);
    }

    /** ------------------------------
     *  user_seq 기준 단건 예약 조회 (마이페이지용)
     * ------------------------------ */
    public FuneralReserveDto findByUserSeq(String userSeq) {
        Optional<FuneralReserve> result = repository.findByUser_UserSeq(userSeq);
        if (result.isEmpty()) {
            log.info("[예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
            return null;
        }
        return toDto(result.get());
    }

    //    /** ------------------------------
//     *  user_seq 기준 다건 예약 조회 (마이페이지용)
//     * ------------------------------ */
    public List<FuneralReserveDto> findAllByUserSeq(String userSeq) {
        List<FuneralReserve> list = repository.findAllByUser_UserSeq(userSeq);
        if (list.isEmpty()) {
            log.info("[장례 예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
            return Collections.emptyList();
        }
        log.info("[장례 예약 조회 완료] user_seq={}, {}건", userSeq, list.size());
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** ------------------------------
     *  ID 기준 예약 조회 (수정폼 진입용)
     * ------------------------------ */
    public FuneralReserveDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /** ------------------------------
     *  날짜별 예약된 시간 목록 조회 (시간 선택 비활성화용)
     * ------------------------------ */
//    public List<String> getBookedTimesByDate(String date) {
//        return repository.findByConsultDate(date)
//                .stream()
//                .map(PsyReserve::getTime)
//                .collect(Collectors.toList());
//    }

    /** ------------------------------
     *  전체 예약 조회 (관리자용)
     * ------------------------------ */
    public List<FuneralReserveDto> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** ------------------------------
     *  상담예약 삭제 (본인만 가능)
     * ------------------------------ */
    @Transactional
    public boolean deleteReserve(Long id, String userSeq) {
        FuneralReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        if (existing.getUser() == null || !existing.getUser().getUserSeq().equals(userSeq)) {
            log.info("ID: {}, user_seq={}", id, userSeq);
            throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
        }

        repository.delete(existing);
        repository.flush();  // 강제로 DB 반영
        log.info("[장례 예약 삭제 완료] ID={}, user_seq={}", id, userSeq);
        return true;
    }

    /** ------------------------------
     *  Entity → DTO 변환
     * ------------------------------ */
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
        return dto;
    }
}
