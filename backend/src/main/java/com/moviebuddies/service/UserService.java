package com.moviebuddies.service;

import com.moviebuddies.dto.request.PasswordChangeRequest;
import com.moviebuddies.dto.request.UserUpdateRequest; // 존재하지 않음
import com.moviebuddies.dto.response.UserResponse;
import com.moviebuddies.entity.User;
import com.moviebuddies.exception.BusinessException;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 관리 서비스
 *
 * 사용자 프로필 조회, 수정, 검색, 삭제 등의 사용자 관리 기능 제공
 * 파일 업로드를 통한 프로필 이미지 관리와 소프트 삭제 패턴 지원
 *
 * 주요 기능:
 * - 사용자 정보 조회 (ID, 사용자명)
 * - 사용자 검색 및 페이징
 * - 프로필 정보 수정
 * - 비밀번호 변경
 * - 프로필 이미지 업로드/삭제
 * - 계정 비활성화 (소프트 삭제)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용
public class UserService {

    private final UserRepository userRepository;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보 DTO
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
        return UserResponse.from(user);
    }

    /**
     * 사용자명으로 사용자 정보 조회
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보 DTO
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "username", username));
        return UserResponse.from(user);
    }

    /**
     * 사용자 검색
     * 
     * 검색어가 있으면 닉네임, 사용자명, 이메일로 부분 일치 검색을 수행하고, 검색어가 없으면 모든 활성 사용자를 반환
     * 검색어가 없으면 모든 활성 사용자를 반환
     * 현재 사용자는 검색 결과에서 제외
     * 
     * @param query 검색어 (닉네임, 사용자명, 이메일로 검색)
     * @param excludeUserId 검색 결과에서 제외할 사용자 ID (현재 사용자)
     * @param pageable 페이징 정보
     * @return 검색된 사용자 목록 (페이징)
     */
    public Page<UserResponse> searchUsers(String query, Long excludeUserId, Pageable pageable) {
        log.info("사용자 검색 - 검색어: {}, 제외 사용자: {}", query, excludeUserId);

        Page<User> users;
        if (query != null && !query.trim().isEmpty()) {
            // 검색어가 있는 경우 부분 일치 검색
            users = userRepository.searchUsers(query.trim(), excludeUserId, pageable);
        } else {
            // 검색어가 없는 경우 모든 활성 사용자 조회
            users = userRepository.findAllActiveUsersExcludingUser(excludeUserId, pageable);
        }

        return users.map(UserResponse::from);
    }

    /**
     * 사용자 프로필 정보 수정
     * 
     * 닉네임과 이메일의 중복 여부를 확인한 후 프로필을 업데이트
     * null이 아닌 필드만 수정되며, 기존 값과 동일한 경우 중복 검사를 생략
     * 
     * @param userId 수정할 사용자 ID
     * @param request 수정할 프로필 정보
     * @return 수정된 사용자 정보 DTO
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws BusinessException 닉네임 또는 이메일이 중복된 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("사용자 프로필 수정 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
        
        // 닉네임 중복 확인 (기존 값과 다른 경우에만)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw BusinessException.conflict("이미 사용 중인 닉네임입니다.");
            }
        }

        // 이메일 중복 확인 (기존 값과 다른 경우에만)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw BusinessException.conflict("이미 사용 중인 이메일입니다.");
            }
        }

        // 프로필 정보 업데이트 (null이 아닌 값만 업데이트)
        user.updateProfile(request.getNickname(), request.getEmail(), null);
        User updatedUser = userRepository.save(user);

        log.info("사용자 프로필 수정 완료 - 사용자 ID: {}", userId);
        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 비밀번호 변경
     *
     * 보안을 위해 현재 비밀번호를 먼저 확인 후 새 비밀번호로 변경
     * 새 비밀번호와 확인 비밀번호의 일치 여부도 검증
     *
     * @param userId 비밀번호를 변경할 사용자 ID
     * @param request 비밀번호 변경 요청 정보
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws BusinessException 현재 비밀번호가 틀렸거나 새 비밀번호가 일치하는 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public void changePassword(Long userId, PasswordChangeRequest request) {

        log.info("비밀번호 변경 요청 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 1. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw BusinessException.badRequest("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호와 확인 비밀번호 일치 여부 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw BusinessException.badRequest("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호와 현재 비밀번호 동일한지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw BusinessException.badRequest("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }
        
        // 4. 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료 - 사용자 ID: {}", userId);
    }


    /**
     * 프로필 이미지 업데이트
     *
     * 기존 프로필 이미지를 삭제하고 새로운 이미지를 업로드
     * 기본 이미지는 삭제하지 않음
     *
     * @param userId 이미지를 업데이트할 사용자 ID
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 URL
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws BusinessException 파일 업로드에 실패한 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public String updateProfileImage(Long userId, MultipartFile file) {
        log.info("프로필 이미지 업데이트 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 기존 이미지 삭제(기본 이미지가 아닌 경우)
        if (user.getProfileImage() != null && !user.getProfileImage().contains("default")) {
            fileService.deleteFile(user.getProfileImage());
        }

        // 새 이미지 업로드
        String fileName = fileService.uploadProfileImage(file);
        user.setProfileImage(fileName);
        userRepository.save(user);

        String imageUrl = user.getProfileImageUrl();
        log.info("프로필 이미지 업데이트 완료 - 사용자 ID: {}, 이미지 URL: {}", userId, imageUrl);

        return imageUrl;
    }

    /**
     * 사용자 계정 삭제 (소프트 삭제)
     *
     * 실제로 데이터를 삭제하지 않고 계정을 비활성화 상태로 변경
     * 프로필 이미지는 실제로 삭제하여 저장 공간을 절약
     *
     * @param userId 삭제할 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 적용
    public void deleteUser(Long userId) {
        log.info("사용자 계정 삭제 - 사용자 ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 프로필 이미지 삭제 (기본 이미지가 아닌 경우)
        if (user.getProfileImage() != null && !user.getProfileImage().contains("default")) {
            fileService.deleteFile(user.getProfileImage());
        }

        // 사용자 비활성화 (소프트 삭제)
        user.setIsActive(false);
        userRepository.save(user);

        log.info("사용자 계정 삭제 완료 - 사용자 ID: {}", userId);
    }
}
