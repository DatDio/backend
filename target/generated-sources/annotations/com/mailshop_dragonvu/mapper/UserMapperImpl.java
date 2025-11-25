package com.mailshop_dragonvu.mapper;

import com.mailshop_dragonvu.dto.users.UserCreateDTO;
import com.mailshop_dragonvu.dto.users.UserResponseDTO;
import com.mailshop_dragonvu.dto.users.UserUpdateDTO;
import com.mailshop_dragonvu.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-25T21:28:23+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserEntity toEntity(UserCreateDTO request) {
        if ( request == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.email( request.getEmail() );
        userEntity.password( request.getPassword() );
        userEntity.fullName( request.getFullName() );
        userEntity.phone( request.getPhone() );
        userEntity.address( request.getAddress() );

        userEntity.authProvider( com.mailshop_dragonvu.enums.AuthProvider.LOCAL );

        return userEntity.build();
    }

    @Override
    public UserResponseDTO toResponse(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserResponseDTO.UserResponseDTOBuilder userResponseDTO = UserResponseDTO.builder();

        userResponseDTO.id( userEntity.getId() );
        userResponseDTO.email( userEntity.getEmail() );
        userResponseDTO.fullName( userEntity.getFullName() );
        userResponseDTO.phone( userEntity.getPhone() );
        userResponseDTO.address( userEntity.getAddress() );
        userResponseDTO.avatarUrl( userEntity.getAvatarUrl() );
        userResponseDTO.emailVerified( userEntity.getEmailVerified() );
        if ( userEntity.getStatus() != null ) {
            userResponseDTO.status( userEntity.getStatus().name() );
        }
        userResponseDTO.createdAt( userEntity.getCreatedAt() );
        userResponseDTO.updatedAt( userEntity.getUpdatedAt() );

        userResponseDTO.roles( mapRolesToStrings(userEntity.getRoles()) );
        userResponseDTO.authProvider( userEntity.getAuthProvider().name() );

        return userResponseDTO.build();
    }

    @Override
    public void updateEntity(UserEntity userEntity, UserUpdateDTO request) {
        if ( request == null ) {
            return;
        }

        if ( request.getEmail() != null ) {
            userEntity.setEmail( request.getEmail() );
        }
        if ( request.getFullName() != null ) {
            userEntity.setFullName( request.getFullName() );
        }
        if ( request.getPhone() != null ) {
            userEntity.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            userEntity.setAddress( request.getAddress() );
        }
        if ( request.getAvatarUrl() != null ) {
            userEntity.setAvatarUrl( request.getAvatarUrl() );
        }
    }
}
