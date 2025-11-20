package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.FuneralService;
import com.example.demo.Repository.FuneralServiceRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class FuneralServiceController {

    // application.properties의 ncp.map.client-id 값을 주입받음
    @Value("${ncp.map.client-id}")
    private String ncpMapClientId;

    // DB에 접근하기 위한 Repository
    private final FuneralServiceRepository funeralServiceRepository;

    // 생성자 주입
    public FuneralServiceController(FuneralServiceRepository funeralServiceRepository) {
        this.funeralServiceRepository = funeralServiceRepository;
    }

    /**
     * 서버가 시작될 때 1번만 실행됨 (DB 데이터 초기화)
     * DB가 비어있을 때 5곳의 장례식장 정보를 저장합니다.
     */
    @PostConstruct
    public void initDatabase() {
        if (funeralServiceRepository.count() == 0) {
            List<FuneralService> initialPlaces = List.of(
                    new FuneralService(
                            "리틀포즈 반려동물 장례식장",
                            "대구 군위군 부계면 부흥로 118",
                            "https.littlepaws.co.kr",
                            "054-382-0400",
                            List.of("기본 장례", "스톤 제작", "24시간")
                    ),
                    new FuneralService(
                            "아이들랜드",
                            "경북 경산시 와촌면 불굴사길 82",
                            "httpshttps.아이들랜드.com/",
                            "1522-6979",
                            List.of("기본 장례", "메모리얼 액자")
                    ),
                    new FuneralService(
                            "리멤버 반려동물장례식장",
                            "경기 용인시 처인구 남사읍 원암로 535",
                            "http://www.리멤버.net",
                            "080-200-5004",
                            List.of("기본 장례", "해양 산골", "24시간")
                    ),
                    new FuneralService(
                            "전주 반려동물 장례식장 아리움",
                            "전북 전주시 완산구 콩쥐팥쥐로 1705-44",
                            "http://www.arium.kr",
                            "0507-1375-7945",
                            List.of("기본 장례", "스톤 제작")
                    ),
                    new FuneralService(
                            "21그램 반려동물장례식장 경기광주1호점",
                            "경기 광주시 매자리길 185-35",
                            "https.21gram.co.kr/",
                            "1688-1240",
                            List.of("기본 장례", "추모 영상 제작")
                    )
            );
            // DB에 5곳 정보 저장
            funeralServiceRepository.saveAll(initialPlaces);
            System.out.println("--- [DB INIT] 장례식장 5곳 데이터가 H2 DB에 초기 저장되었습니다. ---");
        }
    }

    /**
     * /funeral URL 요청을 처리합니다.
     */
    @GetMapping("/funeral")
    public String funeralServicePage(Model model) {

        // 1. DB에서 모든 장례식장 데이터를 조회
        List<FuneralService> places = funeralServiceRepository.findAll();

        // 2. HTML(템플릿)로 장례식장 목록(places) 전달
        model.addAttribute("places", places);

        // 3. HTML(템플릿)로 Naver Client ID 전달
        model.addAttribute("ncpMapClientId", ncpMapClientId);

        return "funeralpage/f_service";
    }
}

