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
import vn.nbh.userservice.dto.request.RoleRequest;
import vn.nbh.userservice.dto.response.RolePageResponse;
import vn.nbh.userservice.dto.response.RoleResponse;
import vn.nbh.userservice.entity.Permission;
import vn.nbh.userservice.entity.Role;
import vn.nbh.userservice.mapper.RoleMapper;
import vn.nbh.userservice.repository.PermissionRepository;
import vn.nbh.userservice.repository.RoleRepository;
import vn.nbh.userservice.repository.UserRepository;
import vn.nbh.userservice.service.RoleService;

import java.security.Permissions;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    @Override
    public RoleResponse create(RoleRequest roleRequest) {
        Role role = roleMapper.toRole(roleRequest);
        List<Permission> permissions = permissionRepository.findAllById(roleRequest.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }


    @Override
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void delete(String roleId) {
        roleRepository.deleteById(roleId);
    }


    @Override
    public RolePageResponse findAll(String keyword, String sort, int page, int size) {
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

        Page<Role> entityPage;

        if (StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = roleRepository.searchByKeyword(keyword, pageable);
        }else{
            entityPage = roleRepository.findAll(pageable);
        }

        List<RoleResponse> roleResponseList = entityPage.stream().map(roleMapper::toRoleResponse).toList();
        RolePageResponse rolePageResponse = new RolePageResponse();
        rolePageResponse.setRoles(roleResponseList);
        rolePageResponse.setPageNumber(entityPage.getNumber());
        rolePageResponse.setPageSize(entityPage.getSize());
        rolePageResponse.setTotalElements(entityPage.getTotalElements());
        rolePageResponse.setTotalPages(entityPage.getTotalPages());
        return rolePageResponse;
    }
}
