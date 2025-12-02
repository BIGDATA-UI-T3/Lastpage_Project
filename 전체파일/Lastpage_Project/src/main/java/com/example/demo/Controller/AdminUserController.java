package com.example.demo.Controller;



import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.Admin.AdminUserService;

import com.example.demo.Domain.Common.Service.EditInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 회원 목록 페이지 */
    @GetMapping("/admin/users")
    public String userListPage(Model model) {
        List<Signup> users = adminUserService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/AdminUserList";
    }


}
