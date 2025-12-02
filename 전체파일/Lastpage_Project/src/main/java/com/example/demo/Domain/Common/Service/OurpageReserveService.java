package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.OurpageReserveDto;
import com.example.demo.Domain.Common.Entity.OurpageReserve;
import com.example.demo.Repository.OurpageReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OurpageReserveService {

    private final OurpageReserveRepository ourpageReserveRepository;

    // [중요] application.properties에서 경로를 가져옵니다.
    // ${user.home}/lastpage_uploads/ (맥/윈도우 공통 홈 디렉토리)
    @Value("${file.upload.root}")
    private String rootPath;

    // ourpage/
    @Value("${file.upload.ourpage}")
    private String ourpagePath;

    // 1. 전체 조회
    @Transactional(readOnly = true)
    public List<OurpageReserveDto> getAllOurpages() {

        List<OurpageReserve> entities = ourpageReserveRepository.findAll();
        List<OurpageReserveDto> dtos = new ArrayList<>();

        // 1. 12개의 빈 슬롯 생성 (기본 화면 구성)
        for (int i = 0; i < 12; i++) {
            dtos.add(OurpageReserveDto.builder()
                    .slotIndex(i)
                    .occupied(false)
                    .build());
        }

        // 2. DB 데이터로 채우기
        for (OurpageReserve entity : entities) {
            int idx = entity.getSlotIndex();

            // [참고] 만약 0, 11번 데이터를 강제로 숨기고 싶다면 아래 주석을 해제하세요.
            // if (idx == 0 || idx == 11) continue;

            if (idx >= 0 && idx < 12) {
                dtos.set(idx, OurpageReserveDto.builder()
                        .id(entity.getId())
                        .slotIndex(idx)
                        .userSeq(entity.getUserSeq())
                        .petName(entity.getPetName())
                        .dateRange(entity.getDateRange())
                        .message(entity.getMessage())
                        // DB에 저장된 웹 경로(/uploads/ourpage/...)를 그대로 사용
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

        // 파일 저장 후 URL 반환 (예: /uploads/ourpage/uuid.jpg)
        String savedPhotoUrl = uploadFile(file);

        OurpageReserve ourpage = OurpageReserve.builder()
                .userSeq(userSeq)
                .slotIndex(slotIndex)
                .petName(petName)
                .dateRange(dateStart + " ~ " + dateEnd)
                .message(message)
                // 파일이 없으면 기본 이미지, 있으면 저장된 URL 사용
                .photoPath(savedPhotoUrl != null ? savedPhotoUrl : "/Asset/default_pet.png")
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

        // 새 파일이 들어오면 업로드하고 경로 업데이트
        if (file != null && !file.isEmpty()) {
            String savedPhotoUrl = uploadFile(file);
            entity.setPhotoPath(savedPhotoUrl);
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

        // (선택사항) 여기서 실제 파일 삭제 로직을 추가할 수도 있음

        ourpageReserveRepository.delete(entity);
    }

    // ---------------------------------------------------------
    // [핵심] 공통 파일 업로드 처리 (application.properties 경로 사용)
    // ---------------------------------------------------------
    private String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 1. 물리적 저장 경로 생성 (C:/Users/.../lastpage_uploads/ourpage/)
        String saveDirectoryPath = rootPath + ourpagePath;

        File dir = new File(saveDirectoryPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs(); // 폴더가 없으면 생성
            log.info("Ourpage 폴더 생성: {} (성공: {})", saveDirectoryPath, created);
        }

        // 2. 파일명 생성 (UUID)
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;

        // 3. 파일 저장
        File saveFile = new File(dir, filename);
        file.transferTo(saveFile);
        log.info("Ourpage 파일 저장됨: {}", saveFile.getAbsolutePath());

        // 4. 웹 접근 URL 반환 (/uploads/ourpage/파일명.jpg)
        // WebConfig에서 "/uploads/**" 요청을 rootPath로 연결해두었으므로 이 경로로 접근 가능
        return "/uploads/" + ourpagePath + filename;
    }
}