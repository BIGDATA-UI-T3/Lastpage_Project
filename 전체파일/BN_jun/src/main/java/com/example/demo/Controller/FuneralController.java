package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.FuneralPlace;
import com.example.demo.Repository.FuneralPlaceRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class FuneralController {

    // application.properties의 ncp.map.client-id 값을 주입받음
    @Value("${ncp.map.client-id}")
    private String ncpMapClientId;

    // DB에 접근하기 위한 Repository
    private final FuneralPlaceRepository funeralPlaceRepository;

    // 생성자 주입
    public FuneralController(FuneralPlaceRepository funeralPlaceRepository) {
        this.funeralPlaceRepository = funeralPlaceRepository;
    }

    /**
     * 서버가 시작될 때 1번만 실행됨 (DB 데이터 초기화)
     * DB가 비어있을 때 5곳의 장례식장 정보를 저장합니다.
     */
    @PostConstruct
    public void initDatabase() {
        if (funeralPlaceRepository.count() == 0) {
            List<FuneralPlace> initialPlaces = List.of(
                    new FuneralPlace(
                            "라스트페이지 서울 본점",
                            // [수정] 네이버에서 확실히 인식하는 주소(강남역)로 변경
                            "서울특별시 강남구 강남대로 396",
                            "https://www.lastpage-seoul.com", // 홈페이지 URL
                            "02-1111-2222",
                            List.of("기본 장례", "스톤 제작", "24시간")
                    ),
                    new FuneralPlace(
                            "라스트페이지 경기 용인점",
                            "경기도 용인시 수지구 포은대로 499",
                            "https://www.lastpage-yongin.com",
                            "031-3333-4444",
                            List.of("기본 장례", "메모리얼 액자")
                    ),
                    new FuneralPlace(
                            "라스트페이지 부산 해운대점",
                            "부산광역시 해운대구 마린시티2로 38",
                            "https://www.lastpage-busan.com",
                            "051-5555-6666",
                            List.of("기본 장례", "해양 산골", "24시간")
                    ),
                    new FuneralPlace(
                            "라스트페이지 대구 수성점",
                            "대구광역시 수성구 동대구로 380",
                            "https://www.lastpage-daegu.com",
                            "053-7777-8888",
                            List.of("기본 장례", "스톤 제작")
                    ),
                    new FuneralPlace(
                            "라스트페이지 광주 상무점",
                            "광주광역시 서구 상무대로 773",
                            "https://www.lastpage-gwangju.com",
                            "062-9999-0000",
                            List.of("기본 장례", "추모 영상 제작")
                    )
            );
            // DB에 5곳 정보 저장
            funeralPlaceRepository.saveAll(initialPlaces);
            System.out.println("--- [DB INIT] 장례식장 5곳 데이터가 H2 DB에 초기 저장되었습니다. ---");
        }
    }

    /**
     * /funeral URL 요청을 처리합니다.
     */
    @GetMapping("/funeral")
    public String funeralServicePage(Model model) {

        // 1. DB에서 모든 장례식장 데이터를 조회
        List<FuneralPlace> places = funeralPlaceRepository.findAll();

        // 2. HTML(템플릿)로 장례식장 목록(places) 전달
        model.addAttribute("places", places);

        // 3. HTML(템플릿)로 Naver Client ID 전달
        model.addAttribute("ncpMapClientId", ncpMapClientId);

        return "f_service"; // templates/f_service.html 파일을 보여줌
    }
}

