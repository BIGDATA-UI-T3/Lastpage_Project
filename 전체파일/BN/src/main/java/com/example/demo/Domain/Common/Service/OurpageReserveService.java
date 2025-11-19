package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.OurpageReserveDto;
import com.example.demo.Domain.Common.Entity.OurpageReserve;
import com.example.demo.Repository.OurpageReserveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OurpageReserveService {

    private final OurpageReserveRepository ourpageReserveRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 1. 전체 조회 (12자리 채워서 반환 + slotIndex 반영)
    @Transactional(readOnly = true)
    public List<OurpageReserveDto> getAllOurpages() {
        List<OurpageReserve> entities = ourpageReserveRepository.findAll();

        // 12개의 빈 자리 리스트 생성
        List<OurpageReserveDto> dtos = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            dtos.add(OurpageReserveDto.builder()
                    .slotIndex(i)
                    .occupied(false)
                    .build());
        }

        // DB에 있는 데이터를 해당 slotIndex 자리에 끼워넣기
        for (OurpageReserve entity : entities) {
            int idx = entity.getSlotIndex();
            if (idx >= 0 && idx < 12) {
                dtos.set(idx, OurpageReserveDto.builder()
                        .id(entity.getId())
                        .slotIndex(idx)
                        .userSeq(entity.getUserSeq())
                        .petName(entity.getPetName())
                        .dateRange(entity.getDateRange())
                        .message(entity.getMessage())
                        .photoUrl(entity.getPhotoPath())
                        .occupied(true)
                        .build());
            }
        }
        return dtos;
    }

    // [중요] 2. 상세 조회 (이 메서드가 없어서 에러가 났던 겁니다!)
    @Transactional(readOnly = true)
    public OurpageReserveDto findById(Long id) {
        return ourpageReserveRepository.findById(id)
                .map(entity -> OurpageReserveDto.builder()
                        .id(entity.getId())
                        .slotIndex(entity.getSlotIndex())
                        .petName(entity.getPetName())
                        .dateRange(entity.getDateRange())
                        .message(entity.getMessage())
                        .photoUrl(entity.getPhotoPath())
                        .occupied(true)
                        .build())
                .orElse(null);
    }

    // 3. 예약 저장 (String userSeq, Integer slotIndex 적용)
    public void save(String petName, String dateStart, String dateEnd, String message, MultipartFile file, String userSeq, Integer slotIndex) throws IOException {
        String savedFileName = uploadFile(file);

        OurpageReserve ourpage = OurpageReserve.builder()
                .userSeq(userSeq) // String 타입
                .slotIndex(slotIndex) // 자리 번호
                .petName(petName)
                .dateRange(dateStart + " ~ " + dateEnd)
                .message(message)
                .photoPath(savedFileName != null ? "/uploads/" + savedFileName : "/Asset/default_pet.png")
                .build();

        ourpageReserveRepository.save(ourpage);
    }

    // 4. 예약 수정 (String userSeq 적용)
    public OurpageReserveDto updateReserve(Long id, String petName, String dateStart, String dateEnd, String message, MultipartFile file, String userSeq) throws IOException {
        OurpageReserve entity = ourpageReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. ID=" + id));

        if (!entity.getUserSeq().equals(userSeq)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        if (file != null && !file.isEmpty()) {
            String savedFileName = uploadFile(file);
            entity.setPhotoPath("/uploads/" + savedFileName);
        }

        entity.setPetName(petName);
        entity.setDateRange(dateStart + " ~ " + dateEnd);
        entity.setMessage(message);

        // 수정된 DTO 반환 (필요시)
        return findById(id);
    }

    // 5. 예약 삭제 (String userSeq 적용)
    public void deleteReserve(Long id, String userSeq) {
        OurpageReserve entity = ourpageReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. ID=" + id));

        if (!entity.getUserSeq().equals(userSeq)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        ourpageReserveRepository.delete(entity);
    }

    // [공통] 파일 업로드 로직
    private String uploadFile(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String savedFileName = uuid + extension;

            File saveFile = new File(uploadPath + savedFileName);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            file.transferTo(saveFile);
            return savedFileName;
        }
        return null;
    }
}