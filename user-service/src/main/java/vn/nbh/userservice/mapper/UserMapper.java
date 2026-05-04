package vn.nbh.userservice.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.nbh.userservice.dto.request.UserCreationRequest;
import vn.nbh.userservice.dto.request.UserUpdateRequest;
import vn.nbh.userservice.dto.response.UserResponse;
import vn.nbh.userservice.entity.User;

@Mapper
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
