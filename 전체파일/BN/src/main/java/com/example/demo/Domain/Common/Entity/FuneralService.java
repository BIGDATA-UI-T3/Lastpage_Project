package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;

import java.util.List;

/**
 * 반려동물 장례식장 정보를 저장하는 JPA Entity입니다.
 * DB 테이블 이름은 'funeral_place'가 됩니다.
 */
@Entity
@Table(name = "funeral_place")
public class FuneralService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 장례식장 이름

    @Column(nullable = false, length = 500) // 주소는 길 수 있으므로 length
    private String address; // 주소 (지오코딩용)

    private String homepageUrl; // 홈페이지 URL

    private String phone; // 전화번호

    // List<String>을 DB의 String 한 줄로 변환해주는 컨버터 사용
    @Convert(converter = StringListConverter.class)
    @Column(length = 1000) // 서비스 목록도 길 수 있음
    private List<String> services; // 서비스 목록

    // Geocoding으로 얻은 좌표 (나중에 캐싱용으로 사용)
    private Double latitude;    // 위도 (Y 좌표)
    private Double longitude;   // 경도 (X 좌표)

    // JPA를 위한 기본 생성자
    public FuneralService() {}

    // DB 데이터 초기화를 위한 생성자
    public FuneralService(String name, String address, String homepageUrl, String phone, List<String> services) {
        this.name = name;
        this.address = address;
        this.homepageUrl = homepageUrl;
        this.phone = phone;
        this.services = services;
    }

    // --- Services List <-> String 변환을 위한 Converter 클래스 ---
    @Converter
    public static class StringListConverter implements AttributeConverter<List<String>, String> {
        private static final String SPLIT_CHAR = ", "; // 쉼표+공백으로 구분

        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            // List<String>을 "A, B, C" 형태의 문자열로 변환하여 DB에 저장
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            return String.join(SPLIT_CHAR, attribute);
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            // DB의 "A, B, C" 문자열을 List<String>으로 변환하여 Java 객체에 로드
            if (dbData == null || dbData.trim().isEmpty()) {
                return List.of(); // 빈 리스트 반환
            }
            return List.of(dbData.split(SPLIT_CHAR));
        }
    }

    // --- Getter and Setter ---
    // (Lombok @Getter, @Setter를 사용해도 됩니다)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getHomepageUrl() { return homepageUrl; }
    public void setHomepageUrl(String homepageUrl) { this.homepageUrl = homepageUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}