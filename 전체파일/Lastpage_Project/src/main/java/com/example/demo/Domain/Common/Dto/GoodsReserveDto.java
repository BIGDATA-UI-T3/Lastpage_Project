package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.PaymentStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;

@Data
public class GoodsReserveDto {

    private Long id;
    private String userSeq;

    // Step 1
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String ownerAddr;
    private String petName;
    private String petType;
    private String petBreed;
    private String petWeight;
    private String memo;

    private List<String> materials;

    // Step 2
    private String product;
    private String metalColor;
    private String chainLength;
    private String ringSize;
    private Integer quantity;
    private String engravingText;
    private String engravingFont;
    private String optionsMemo;

    // Step 3
    private String shipMethod;
    private String targetDate;
    private Boolean isExpress;
    private String kitAddr;
    private String kitDate;
    private String kitTime;
    private String visitDate;
    private String visitTime;
    private String trackingInfo;

    // ★ 결제상태 추가
    private PaymentStatus paymentStatus;


    public static GoodsReserveDto fromEntity(GoodsReserve g) {

        GoodsReserveDto dto = new GoodsReserveDto();
        ObjectMapper mapper = new ObjectMapper();

        dto.setId(g.getId());
        dto.setUserSeq(g.getUser().getUserSeq());

        // Step 1
        dto.setOwnerName(g.getOwnerName());
        dto.setOwnerPhone(g.getOwnerPhone());
        dto.setOwnerEmail(g.getOwnerEmail());
        dto.setOwnerAddr(g.getOwnerAddr());
        dto.setPetName(g.getPetName());
        dto.setPetType(g.getPetType());
        dto.setPetBreed(g.getPetBreed());
        dto.setPetWeight(g.getPetWeight());
        dto.setMemo(g.getMemo());

        // materials JSON → List<String>
        try {
            if (g.getMaterials() != null && !g.getMaterials().isEmpty()) {
                dto.setMaterials(
                        mapper.readValue(g.getMaterials(), new TypeReference<>() {})
                );
            }
        } catch (Exception e) {
            dto.setMaterials(null);
        }

        // Step 2
        dto.setProduct(g.getProduct());
        dto.setMetalColor(g.getMetalColor());
        dto.setChainLength(g.getChainLength());
        dto.setRingSize(g.getRingSize());
        dto.setQuantity(g.getQuantity());
        dto.setEngravingText(g.getEngravingText());
        dto.setEngravingFont(g.getEngravingFont());
        dto.setOptionsMemo(g.getOptionsMemo());

        // Step 3
        dto.setShipMethod(g.getShipMethod());
        dto.setTargetDate(g.getTargetDate());
        dto.setIsExpress(g.getIsExpress());
        dto.setKitAddr(g.getKitAddr());
        dto.setKitDate(g.getKitDate());
        dto.setKitTime(g.getKitTime());
        dto.setVisitDate(g.getVisitDate());
        dto.setVisitTime(g.getVisitTime());
        dto.setTrackingInfo(g.getTrackingInfo());

        // ★ 결제상태 매핑 추가
        dto.setPaymentStatus(g.getPaymentStatus());

        return dto;
    }
}
