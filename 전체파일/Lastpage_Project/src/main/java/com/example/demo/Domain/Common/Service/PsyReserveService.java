package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Repository.PsyReserveRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PsyReserveService {

    private final PsyReserveRepository repository;

    /**
     * 상담예약 등록 (DTO → Entity 변환)
     */
    @Transactional
    public PsyReserveDto saveReservation(PsyReserveDto dto) {

        if (repository.existsByConsultDateAndTime(dto.getConsultDate(), dto.getTime())) {
            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        PsyReserve entity = PsyReserve.builder()
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
        log.info("[상담예약 등록 완료] ID={}, 날짜={}, 시간={}", saved.getId(), saved.getConsultDate(), saved.getTime());

        return toDto(saved);
    }

    /**
     * 전체 예약 조회 (DTO 변환)
     */
    public List<PsyReserveDto> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 이메일 기준으로 예약 조회 (마이페이지용)
     */
    public PsyReserveDto findByEmail(String email) {
        return repository.findByEmail(email)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * ID로 예약 조회
     */
    public PsyReserveDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 날짜/시간이 이미 예약되어 있는지 확인
     */
    public boolean isDateReserved(String consultDate, String time) {
        return repository.existsByConsultDateAndTime(consultDate, time);
    }

    /**
     * 수정 시 자기 자신 예약은 제외하고 중복 체크
     */
    public boolean isDateReserved(String consultDate, String time, Long excludedId) {
        return repository.findByConsultDateAndTime(consultDate, time)
                .stream()
                .anyMatch(r -> !r.getId().equals(excludedId));
    }

    /**
     * 예약 수정 (Transactional)
     */
    @Transactional
    public PsyReserveDto updateReserve(Long id, PsyReserveDto updated) {
        PsyReserve existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (isDateReserved(updated.getConsultDate(), updated.getTime(), id)) {
            throw new IllegalStateException("해당 날짜와 시간은 이미 예약되어 있습니다.");
        }

        existing.setConsultDate(updated.getConsultDate());
        existing.setTime(updated.getTime());
        existing.setCounselor(updated.getCounselor());
        existing.setMemo(updated.getMemo());
        existing.setAddress(updated.getAddress());

        PsyReserve saved = repository.save(existing);
        log.info("[상담예약 수정 완료] ID={}, 날짜={}, 시간={}", id, saved.getConsultDate(), saved.getTime());
        return toDto(saved);
    }

    /**
     * 예약 삭제
     */
    @Transactional
    public boolean deleteReserve(Long id) {
        if (!repository.existsById(id)) {
            log.warn("[삭제 실패] 존재하지 않는 예약 ID={}", id);
            return false;
        }
        repository.deleteById(id);
        log.info("[상담예약 삭제 완료] ID={}", id);
        return true;
    }

    /**
     * Entity → DTO 변환
     */
    private PsyReserveDto toDto(PsyReserve entity) {
        PsyReserveDto dto = new PsyReserveDto();
//        dto.setId(entity.getId());
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
