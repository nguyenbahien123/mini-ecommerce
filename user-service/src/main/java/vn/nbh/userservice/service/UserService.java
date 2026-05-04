package vn.nbh.userservice.service;

import vn.nbh.userservice.dto.request.UserCreationRequest;
import vn.nbh.userservice.dto.request.UserUpdateRequest;
import vn.nbh.userservice.dto.response.UserPageResponse;
import vn.nbh.userservice.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    List<UserResponse> getAll();
    UserResponse updateUser(int userId, UserUpdateRequest request);
    void deleteUser(int userId);
    UserPageResponse findAll(String keyword, String sort, int page, int size);
}
