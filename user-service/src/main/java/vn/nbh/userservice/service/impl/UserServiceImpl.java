package vn.nbh.userservice.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.nbh.userservice.constant.PredefinedRole;
import vn.nbh.userservice.dto.request.UserCreationRequest;
import vn.nbh.userservice.dto.request.UserUpdateRequest;
import vn.nbh.userservice.dto.response.UserPageResponse;
import vn.nbh.userservice.dto.response.UserResponse;
import vn.nbh.userservice.entity.Role;
import vn.nbh.userservice.entity.User;
import vn.nbh.userservice.exception.AppException;
import vn.nbh.userservice.exception.ErrorCode;
import vn.nbh.userservice.mapper.UserMapper;
import vn.nbh.userservice.repository.RoleRepository;
import vn.nbh.userservice.repository.UserRepository;
import vn.nbh.userservice.service.UserService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findById(PredefinedRole.USER_ROLE).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setRoles(role);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }


    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }


    @Override
    public UserResponse updateUser(int userId, UserUpdateRequest request) {
        User users = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(users, request);
        users.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findById(request.getRoles()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        users.setRoles(role);
        userRepository.save(users);
        return userMapper.toUserResponse(users);
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
    }


    @Override
    public UserPageResponse findAll(String keyword, String sort, int page, int size) {
        Sort.Order order = new Sort.Order(Sort.Direction.ASC,"id");
        if(StringUtils.hasLength(sort)){
            Pattern pattern = Pattern.compile("^(\\w+):(asc|desc)$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sort);
            if(matcher.find()){
                String columnName = matcher.group(1);
                if(matcher.group(2).equalsIgnoreCase("asc")){
                    order = new Sort.Order(Sort.Direction.ASC,columnName);
                }else{
                    order = new Sort.Order(Sort.Direction.DESC,columnName);
                }
            }
        }

        int pageNo = 0;
        if(page > 0){
            pageNo = page - 1;
        }

        Pageable pageable = PageRequest.of(pageNo,size,Sort.by(order));

        Page<User> entityPage;

        if (StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = userRepository.searchByKeyword(keyword, pageable);
        }else{
            entityPage = userRepository.findAll(pageable);
        }

        List<UserResponse> userResponses = entityPage.stream().map(userMapper::toUserResponse).toList();

        UserPageResponse userPageResponse = new UserPageResponse();
        userPageResponse.setPageNumber(entityPage.getNumber());
        userPageResponse.setPageSize(entityPage.getSize());
        userPageResponse.setTotalElements(entityPage.getTotalElements());
        userPageResponse.setTotalPages(entityPage.getTotalPages());
        userPageResponse.setUsers(userResponses);

        return userPageResponse;
    }
}
