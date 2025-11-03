package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Value; // [추가] 1. @Value import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // [추가] 2. Model import
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FuneralController {

    // [추가] 3. application.properties에서 ncp.map.client-id 값을 읽어옵니다.
    @Value("${ncp.map.client-id}")
    private String ncpMapClientId;

    @GetMapping("/funeral")
    public String funeralServicePage(Model model) { // [수정] 4. Model 파라미터 추가

        // [추가] 5. Model에 Client ID를 "ncpMapClientId"라는 이름으로 담습니다.
        model.addAttribute("ncpMapClientId", ncpMapClientId);

        return "f_service";
    }
}
