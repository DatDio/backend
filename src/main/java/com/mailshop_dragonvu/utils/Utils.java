package com.mailshop_dragonvu.utils;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static Sort generatedSort(String input) {
        // Nếu input là null hoặc rỗng, trả về sắp xếp mặc định theo "id" giảm dần
        if (input == null || input.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        // Tách các cặp trường và hướng sắp xếp
        String[] sortParams = input.split(",");

        // Danh sách các Sort.Order
        List<Sort.Order> orders = new ArrayList<>();

        // Duyệt qua các cặp trường và hướng sắp xếp
        for (int i = 0; i < sortParams.length; i += 2) {
            String field = sortParams[i].trim(); // Trường cần sắp xếp
            String direction = (i + 1 < sortParams.length) ? sortParams[i + 1].trim() : "asc"; // Hướng sắp xếp, mặc
            // định là "asc"

            // Thêm Sort.Order tương ứng vào danh sách
            if ("desc".equalsIgnoreCase(direction)) {
                orders.add(Sort.Order.desc(field));
            } else {
                orders.add(Sort.Order.asc(field));
            }
        }

        // Trả về Sort với các Sort.Order đã tạo
        return Sort.by(orders);
    }
}
