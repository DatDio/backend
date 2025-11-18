package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.users.UserCreateRequest;
import com.mailshop_dragonvu.dto.users.UserResponse;
import com.mailshop_dragonvu.dto.users.UserUpdateRequest;
import com.mailshop_dragonvu.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-18T16:28:06+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.getEmail() );
        user.password( request.getPassword() );
        user.fullName( request.getFullName() );
        user.phone( request.getPhone() );
        user.address( request.getAddress() );

        user.authProvider( com.mailshop_dragonvu.enums.AuthProvider.LOCAL );

        return user.build();
    }

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.email( user.getEmail() );
        userResponse.fullName( user.getFullName() );
        userResponse.phone( user.getPhone() );
        userResponse.address( user.getAddress() );
        userResponse.avatarUrl( user.getAvatarUrl() );
        userResponse.emailVerified( user.getEmailVerified() );
        userResponse.status( user.getStatus() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.updatedAt( user.getUpdatedAt() );

        userResponse.roles( mapRolesToStrings(user.getRoles()) );
        userResponse.authProvider( user.getAuthProvider().name() );

        return userResponse.build();
    }

    @Override
    public void updateEntity(User user, UserUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getEmail() != null ) {
            user.setEmail( request.getEmail() );
        }
        if ( request.getFullName() != null ) {
            user.setFullName( request.getFullName() );
        }
        if ( request.getPhone() != null ) {
            user.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            user.setAddress( request.getAddress() );
        }
        if ( request.getAvatarUrl() != null ) {
            user.setAvatarUrl( request.getAvatarUrl() );
        }
    }
}
