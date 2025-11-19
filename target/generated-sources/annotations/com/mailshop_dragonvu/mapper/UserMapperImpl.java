package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.users.UserCreateDTO;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
import com.mailshop_dragonvu.dto.users.UserUpdateDTO;
import com.mailshop_dragonvu.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T18:10:42+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserCreateDTO request) {
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
    public UserResponseDTO toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDTO.UserResponseDTOBuilder userResponseDTO = UserResponseDTO.builder();

        userResponseDTO.id( user.getId() );
        userResponseDTO.email( user.getEmail() );
        userResponseDTO.fullName( user.getFullName() );
        userResponseDTO.phone( user.getPhone() );
        userResponseDTO.address( user.getAddress() );
        userResponseDTO.avatarUrl( user.getAvatarUrl() );
        userResponseDTO.emailVerified( user.getEmailVerified() );
        userResponseDTO.status( user.getStatus() );
        userResponseDTO.createdAt( user.getCreatedAt() );
        userResponseDTO.updatedAt( user.getUpdatedAt() );

        userResponseDTO.roles( mapRolesToStrings(user.getRoles()) );
        userResponseDTO.authProvider( user.getAuthProvider().name() );

        return userResponseDTO.build();
    }

    @Override
    public void updateEntity(User user, UserUpdateDTO request) {
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
