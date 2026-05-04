package vn.nbh.userservice.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.nbh.userservice.dto.request.PermissionRequest;
import vn.nbh.userservice.dto.response.PermissionPageResponse;
import vn.nbh.userservice.dto.response.PermissionResponse;
import vn.nbh.userservice.entity.Permission;
import vn.nbh.userservice.exception.AppException;
import vn.nbh.userservice.exception.ErrorCode;
import vn.nbh.userservice.mapper.PermissionMapper;
import vn.nbh.userservice.repository.PermissionRepository;
import vn.nbh.userservice.repository.UserRepository;
import vn.nbh.userservice.service.PermissionService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionResponse create(PermissionRequest permissionRequest) {
        Permission permissions = permissionMapper.toPermission(permissionRequest);
        permissionRepository.save(permissions);
        return permissionMapper.toPermissionResponse(permissions);
    }

    @Override
    public PermissionResponse update(String permissionId, PermissionRequest permissionRequest) {
        Permission permissions = permissionRepository.findById(permissionId).orElseThrow(()-> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));
        permissionMapper.updatePermission(permissions,permissionRequest);
        permissionRepository.save(permissions);
        return permissionMapper.toPermissionResponse(permissions);
    }

    @Override
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    public void delete(String permissionId) {
        permissionRepository.deleteById(permissionId);
    }

    @Override
    public PermissionPageResponse findAll(String keyword, String sort, int page, int size) {
        Sort.Order order = new Sort.Order(Sort.Direction.ASC,"name");
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

        Page<Permission> entityPage;

        if (StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = permissionRepository.searchByKeyword(keyword, pageable);
        }else{
            entityPage = permissionRepository.findAll(pageable);
        }

        List<PermissionResponse> permissionResponseList = entityPage.stream().map(permissionMapper::toPermissionResponse).toList();
        PermissionPageResponse permissionPageResponse = new PermissionPageResponse();
        permissionPageResponse.setPermissions(permissionResponseList);
        permissionPageResponse.setPageNumber(entityPage.getNumber());
        permissionPageResponse.setTotalPages(entityPage.getTotalPages());
        permissionPageResponse.setTotalElements(entityPage.getTotalElements());
        permissionPageResponse.setPageSize(entityPage.getSize());
        return permissionPageResponse;
    }
}
