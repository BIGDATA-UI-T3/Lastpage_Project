package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommunityPageController {

    /** 커뮤니티 메인 페이지 */
    @GetMapping("/communitypage/Community")
    public String communityPage() {
        // templates/communitypage/Community.html 렌더링
        return "communitypage/Community";
    }
}
