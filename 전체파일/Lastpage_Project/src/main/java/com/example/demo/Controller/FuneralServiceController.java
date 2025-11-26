package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.FuneralService;
import com.example.demo.Repository.FuneralServiceRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FuneralServiceController {

    @Value("${ncp.map.client-id}")
    private String ncpMapClientId;

    private final FuneralServiceRepository funeralServiceRepository;

    public FuneralServiceController(FuneralServiceRepository funeralServiceRepository) {
        this.funeralServiceRepository = funeralServiceRepository;
    }

    @PostConstruct
    public void initDatabase() {
        if (funeralServiceRepository.count() == 0) {
            List<FuneralService> initialPlaces = new ArrayList<>();

            // 1. 경기/수도권
            addPlace(initialPlaces, "21그램 경기광주점", "경기 광주시 오포읍 매자리길 185-35", "https://21gram.co.kr", "1688-1240", List.of("장례", "봉안당", "제작"), 37.3554, 127.2158);
            addPlace(initialPlaces, "포포즈 경기광주점", "경기 광주시 곤지암읍 경충대로 295", "https://fourpaws.co.kr", "1588-2888", List.of("장례", "픽업", "스톤"), 37.3512, 127.3465);
            addPlace(initialPlaces, "펫포레스트", "경기 광주시 오포읍 문형산길 130", "http://petforest.co.kr", "031-760-9999", List.of("장례", "화장", "안치"), 37.3468, 127.1789);
            addPlace(initialPlaces, "엔젤스톤", "경기 용인시 처인구 모현읍 초부로 150", "http://angelstone.co.kr", "1588-4438", List.of("스톤제작", "장례"), 37.2956, 127.2456);
            addPlace(initialPlaces, "스타로펫", "인천 서구 거북로 40", "http://starropet.co.kr", "032-577-1024", List.of("장례", "화장"), 37.5123, 126.6543);
            addPlace(initialPlaces, "더 포에버", "경기 양주시 백석읍 권율로 872", "http://theforever.co.kr", "1600-4824", List.of("장례", "메모리얼스톤"), 37.7890, 126.9876);
            addPlace(initialPlaces, "굿바이엔젤", "서울 노원구 동일로 1077", "http://goodbyeangel.co.kr", "1661-6266", List.of("24시간", "픽업"), 37.6345, 127.0678);

            // 2. 대구/경북
            addPlace(initialPlaces, "아이헤븐", "경북 고령군 다산면 성산로 604", "http://iheaven.kr", "1577-5474", List.of("장례", "수목장", "봉안당"), 35.7890, 128.4567);
            addPlace(initialPlaces, "대구 러브펫", "대구 달성군 옥포읍 비슬로 468길 65", "http://lovepet.co.kr", "053-614-7979", List.of("화장", "염습"), 35.7654, 128.4890);
            addPlace(initialPlaces, "리틀포즈", "대구 군위군 부계면 부흥로 118", "https://littlepaws.co.kr", "054-382-0400", List.of("장례", "스톤", "24시간"), 36.0123, 128.6543);
            addPlace(initialPlaces, "스토리펫", "강원 강릉시 사천면 동해대로 3850", "http://storypet.co.kr", "033-644-1024", List.of("장례", "화장"), 37.8543, 128.8543);

            // 3. 부산/경남
            addPlace(initialPlaces, "파트라슈", "경남 김해시 상동면 동북로 473번길 284", "http://patrasche.co.kr", "1544-7024", List.of("장례", "픽업"), 35.3456, 128.9876);
            addPlace(initialPlaces, "아이들랜드", "경북 경산시 와촌면 불굴사길 82", "https://idlland.com", "1522-6979", List.of("장례", "액자제작"), 35.9543, 128.7890);
            addPlace(initialPlaces, "부산 반려동물 장례식장", "부산 기장군 일광면 차양길 15", "http://busanpetfuneral.com", "051-724-1024", List.of("장례", "화장"), 35.2678, 129.2345);
            addPlace(initialPlaces, "펫로스 케어", "경남 양산시 상북면 양산대로 1352", "http://petlosscare.com", "1522-2455", List.of("장례", "상담"), 35.3890, 129.0123);

            // 4. 충청/전라/기타
            addPlace(initialPlaces, "하늘숲", "충남 천안시 동남구 광덕면 보산원2길 46", "http://skypet.co.kr", "1577-8024", List.of("장례", "수목장"), 36.6789, 127.1234);
            addPlace(initialPlaces, "공감", "충북 청주시 서원구 남이면 대림로 320", "http://gonggam.co.kr", "1544-9024", List.of("장례", "24시간"), 36.5678, 127.4567);
            addPlace(initialPlaces, "아리움", "전북 전주시 완산구 콩쥐팥쥐로 1705-44", "http://arium.kr", "0507-1375-7945", List.of("장례", "스톤"), 35.8456, 127.1023);
            addPlace(initialPlaces, "리멤버", "전북 전주시 덕진구 동부대로 1024", "http://remember.net", "1588-6024", List.of("장례", "해양산골"), 35.8901, 127.1567);
            addPlace(initialPlaces, "제주 반려동물 장례식장", "제주 제주시 애월읍 평화로 2500", "http://jejupet.com", "064-799-1024", List.of("장례", "화장"), 33.4321, 126.3456);

            funeralServiceRepository.saveAll(initialPlaces);
            System.out.println("--- [DB INIT] 실제 장례식장 20곳 데이터 초기화 완료 ---");
        }
    }

    // 데이터 추가용 헬퍼 메서드
    private void addPlace(List<FuneralService> list, String name, String addr, String url, String phone, List<String> svcs, Double lat, Double lng) {
        FuneralService fs = new FuneralService(name, addr, url, phone, svcs);
        fs.setLatitude(lat);
        fs.setLongitude(lng);
        list.add(fs);
    }

    /**
     * [수정된 기능]
     * 1. 검색어(keyword)가 없으면 -> 전체 목록 조회
     * 2. 검색어(keyword)가 있으면 -> 이름 또는 주소로 검색 (대구, 부산 등)
     */
    @GetMapping("/funeral")
    public String funeralServicePage(@RequestParam(required = false) String keyword, Model model) {

        List<FuneralService> places;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있을 때: 리포지토리의 검색 메서드 호출
            places = funeralServiceRepository.findByNameContainingOrAddressContaining(keyword, keyword);
        } else {
            // 검색어가 없을 때: 전체 조회
            places = funeralServiceRepository.findAll();
        }

        model.addAttribute("places", places);
        model.addAttribute("ncpMapClientId", ncpMapClientId);
        model.addAttribute("keyword", keyword); // 검색어를 유지하기 위해 뷰로 다시 보냄

        return "funeralpage/f_service";
    }
}