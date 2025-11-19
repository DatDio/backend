package com.mailshop_dragonvu.controller.admin;

import com.mailshop_dragonvu.utils.Constants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminCategoryController")
@RequestMapping("/admin" + Constants.API_PATH.CATEGORIES)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {
}
