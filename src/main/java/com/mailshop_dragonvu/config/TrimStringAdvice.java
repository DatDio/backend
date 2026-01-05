package com.mailshop_dragonvu.config;

import java.beans.PropertyDescriptor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * Tự động trim() tất cả các field String trong request body DTO
 * Áp dụng cho tất cả các @RequestBody trong ứng dụng
 */
@RestControllerAdvice
public class TrimStringAdvice implements RequestBodyAdvice {

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, 
                           @NonNull java.lang.reflect.Type targetType, 
                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // Áp dụng cho mọi request body
        return true;
    }

    @Override
    @NonNull
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage, 
                                           @NonNull MethodParameter parameter, 
                                           @NonNull java.lang.reflect.Type targetType, 
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    @NonNull
    public Object afterBodyRead(@NonNull Object body, 
                                @NonNull HttpInputMessage inputMessage, 
                                @NonNull MethodParameter parameter, 
                                @NonNull java.lang.reflect.Type targetType, 
                                @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        trimAllStrings(body);
        return body;
    }

    @Override
    @Nullable
    public Object handleEmptyBody(@Nullable Object body, 
                                  @NonNull HttpInputMessage inputMessage, 
                                  @NonNull MethodParameter parameter, 
                                  @NonNull java.lang.reflect.Type targetType, 
                                  @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * Dùng BeanWrapper để duyệt tất cả các property kiểu String
     * rồi gọi setter để trim. Không cần dùng setAccessible(true).
     */
    private void trimAllStrings(Object object) {
        if (object == null) {
            return;
        }
        
        BeanWrapper wrapper = new BeanWrapperImpl(object);

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            // Chỉ xử lý các property String có getter và setter
            if (pd.getPropertyType().equals(String.class)
                    && pd.getWriteMethod() != null
                    && pd.getReadMethod() != null
                    && !pd.getName().equals("class")) {

                Object currentValue = wrapper.getPropertyValue(pd.getName());
                if (currentValue instanceof String) {
                    String trimmed = ((String) currentValue).trim();
                    wrapper.setPropertyValue(pd.getName(), trimmed);
                }
            }
        }
    }
}
