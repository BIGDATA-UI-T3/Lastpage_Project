package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.OurpageReserveDto;
import com.example.demo.Domain.Common.Entity.OurpageReserve;
import com.example.demo.Repository.OurpageReserveRepository;
import lombok.RequiredArgsConstructor;
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

    // OS 공통 경로 (Mac·Windows·Linux 공통)
    private final String baseUploadDir =
            System.getProperty("user.home") + "/lastpage_uploads/ourpage/";

    // 1. 전체 조회
    @Transactional(readOnly = true)
    public List<OurpageReserveDto> getAllOurpages() {

        List<OurpageReserve> entities = ourpageReserveRepository.findAll();

        List<OurpageReserveDto> dtos = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            dtos.add(OurpageReserveDto.builder()
                    .slotIndex(i)
                    .occupied(false)
                    .build());
        }

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

    // 2. 단건 조회
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

    // 3. 예약 저장
    public void save(String petName, String dateStart, String dateEnd, String message,
                     MultipartFile file, String userSeq, Integer slotIndex) throws IOException {

        String savedFileName = uploadFile(file);

        OurpageReserve ourpage = OurpageReserve.builder()
                .userSeq(userSeq)
                .slotIndex(slotIndex)
                .petName(petName)
                .dateRange(dateStart + " ~ " + dateEnd)
                .message(message)
                .photoPath(savedFileName != null
                        ? "/uploads/ourpage/" + savedFileName
                        : "/Asset/default_pet.png")
                .build();

        ourpageReserveRepository.save(ourpage);
    }

    // 4. 수정
    public OurpageReserveDto updateReserve(Long id, String petName, String dateStart,
                                           String dateEnd, String message,
                                           MultipartFile file, String userSeq) throws IOException {

        OurpageReserve entity = ourpageReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        if (!entity.getUserSeq().equals(userSeq)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        if (file != null && !file.isEmpty()) {
            String savedFileName = uploadFile(file);
            entity.setPhotoPath("/uploads/ourpage/" + savedFileName);
        }

        entity.setPetName(petName);
        entity.setDateRange(dateStart + " ~ " + dateEnd);
        entity.setMessage(message);

        return findById(id);
    }

    // 5. 삭제
    public void deleteReserve(Long id, String userSeq) {

        OurpageReserve entity = ourpageReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        if (!entity.getUserSeq().equals(userSeq)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        ourpageReserveRepository.delete(entity);
    }

    // ---------------------------
    // 공통 파일 업로드 처리
    // ---------------------------
    private String uploadFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) return null;

        File dir = new File(baseUploadDir);
        if (!dir.exists()) dir.mkdirs();

        String original = file.getOriginalFilename();
        String ext = "";

        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + ext;

        File saveFile = new File(dir, filename);
        file.transferTo(saveFile);

        return filename;
    }
}
