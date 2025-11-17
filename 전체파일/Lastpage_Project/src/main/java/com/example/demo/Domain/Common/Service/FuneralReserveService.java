package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.FuneralReserveRepository;
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
@Transactional
public class FuneralReserveService {

    private final FuneralReserveRepository repository ;
    private final SignupRepository signupRepository;

    /** 저장 */
    public FuneralReserveDto saveReservation(FuneralReserveDto dto) {
        // FK 검증
        log.info("[DEBUG 예약 저장 진입] dto.userSeq={}, ownerName={}, petName={}",
                dto.getUserSeq(), dto.getOwnerName(), dto.getPetName());

        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다: " + dto.getUserSeq()));


        // DTO → Entity
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
        // flush까지 해서 실제 INSERT 보장하고 싶으면 아래 주석 해제
//         repository.flush();

        log.info("[장례 예약 등록 완료] id={}, user_seq={}, ownerName={}",
                saved.getId(),
                saved.getUser() != null ? saved.getUser().getUserSeq() : "null-user",
                saved.getOwnerName());

        return toDto(saved);
    }

    /** 수정 (본인만) */
    @Transactional
    public FuneralReserveDto updateReserve(Long id, FuneralReserveDto updated, String userSeq) {
        FuneralReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (existing.getUser() == null || !existing.getUser().getUserSeq().equals(userSeq)) {
            log.info("[권한오류] ID: {}, 저장된 user_seq={}, 요청 user_seq={}",
                    id,
                    existing.getUser() != null ? existing.getUser().getUserSeq() : "null-user",
                    userSeq);
            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
        }

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
        log.info("[장례 예약 수정 완료] ID={}, user_seq={}", id, userSeq);
        return toDto(saved);
    }

    /** 단건 조회 (최근 한 건만 쓰고 싶다면 Repository 메서드 확인) */

    public FuneralReserveDto findByUserSeq(String userSeq) {
        Optional<FuneralReserve> result = repository.findByUser_UserSeq(userSeq);
        if (result.isEmpty()) {
            log.info("[장례 예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
            return null;
        }
        return toDto(result.get());
    }

    /** 다건 조회 (마이페이지용) */

    public List<FuneralReserveDto> findAllByUserSeq(String userSeq) {
        List<FuneralReserve> list = repository.findAllByUser_UserSeq(userSeq);
        if (list.isEmpty()) {
            log.info("[장례 예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
            return Collections.emptyList();
        }
        log.info("[장례 예약 조회 완료] user_seq={}, {}건", userSeq, list.size());
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }



    /** ID 기준 조회 (수정 진입용) */
    @Transactional(readOnly = true)
    public FuneralReserveDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /** 삭제 (본인만) */
    @Transactional
    public boolean deleteReserve(Long id, String userSeq) {
        FuneralReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        if (existing.getUser() == null || !existing.getUser().getUserSeq().equals(userSeq)) {
            log.info("삭제 권한 오류 - ID: {}, stored user_seq={}, request user_seq={}",
                    id,
                    existing.getUser() != null ? existing.getUser().getUserSeq() : "null-user",
                    userSeq);
            throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
        }

        repository.delete(existing);
        repository.flush();  // 실제 반영 보장
        log.info("[장례 예약 삭제 완료] ID={}, user_seq={}", id, userSeq);
        return true;
    }

    public long countAll() {
        return repository.count();
    }

    public List<FuneralReserveDto> findRecent5() {
        return repository.findTop5ByOrderByCreated_atDesc()
                .stream()
                .map(FuneralReserveDto::fromEntity)
                .toList();
    }

    /** Entity → DTO */
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

        // ★ 중요: userSeq 세팅 (조회/렌더링/디버깅에 매우 유용)
        if (entity.getUser() != null) {
            dto.setUserSeq(entity.getUser().getUserSeq());
        }

        return dto;
    }


}
