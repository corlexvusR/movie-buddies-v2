package com.moviebuddies.service;

import com.moviebuddies.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 파일 관리 서비스
 *
 * 사용자 프로필 이미지와 채팅 이미지의 업로드, 삭제, 유효성 검사를 담당
 * 안전한 파일 처리와 저장 공간 관리를 위한 다양한 기능 제공
 *
 * 주요 기능:
 * - 프로필 이미지 업로드/삭제
 * - 채팅 이미지 업로드 (날짜별 디렉토리 구조)
 * - 이미지 파일 유효성 검사
 * - 파일 URL 생성
 */
@Slf4j
@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-dir}")
    private String profileDir;

    @Value("${file.chat-dir}")
    private String chatDir;

    /**
     * 최대 파일 크기 (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 허용되는 이미지 파일 확장자
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};

    /**
     * 프로필 이미지 업로드
     * 
     * 사용자 프로필용 이미지를 업로드하고 고유한 파일명으로 저장
     * UUID를 사용하여 파일명 충돌을 방지
     * 
     * @param file 업로드할 이미지 파일
     * @return 저장된 파일명 (UUID + 확장자)
     * @throws BusinessException 파일 업로드에 실패한 경우
     */
    public String uploadProfileImage(MultipartFile file) {
        log.info("프로필 이미지 업로드 시작 - 파일명: {}", file.getOriginalFilename());

        validateImageFile(file);

        try {
            // 프로필 이미지 디렉토리 생성
            Path uploadPath = Paths.get(profileDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + extension;
            
            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("프로필 이미지 업로드 완료 - 저장 파일명: {}", fileName);
            return fileName;

        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패", e);
            throw BusinessException.internalServerError("파일 업로드에 실패했습니다.");
        }
    }

    /**
     * 채팅 이미지 업로드
     *
     * 채팅에서 사용할 이미지를 날짜별 디렉토리 구조로 업로드
     * 관리 편의성과 성능을 위해 년/월 형태의 디렉토리를 생성
     *
     * @param file 업로드할 이미지 파일
     * @return 상대 경로 (년/월/파일명)
     * @throws BusinessException 파일 업로드에 실패한 경우
     */
    public String uploadChatImage(MultipartFile file) {
        log.info("채팅 이미지 업로드 시작 - 파일명: {}", file.getOriginalFilename());

        validateImageFile(file);

        try {
            // 날짜별 디렉토리 생성 (예: 2025/01)
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path uploadPath = Paths.get(chatDir, dateStr);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + extension;

            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = dateStr + "/" + fileName;
            log.info("채팅 이미지 업로드 완료 - 저장 경로: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("채팅 이미지 업로드 실패", e);
            throw BusinessException.internalServerError("파일 업로드에 실패했습니다.");
        }
    }

    /**
     * 파일 삭제
     *
     * 지정된 파일을 삭제
     * 기본 이미지는 삭제하지 않으며, 파일 경로에 따라 프로필 이미지와 채팅 이미지를 구분하여 처리
     *
     * @param fileName 삭제할 파일명 또는 상대 경로
     */
    public void deleteFile(String fileName) {
        // 기본 이미지는 삭제하지 않음
        if (fileName == null || fileName.contains("default")) {
            return;
        }

        try {
            Path filePath;
            if (fileName.contains("/")) {
                // 채팅 이미지 (상대 경로 포함)
                filePath = Paths.get(chatDir, fileName);
            } else {
                // 프로필 이미지 (파일명만)
                filePath = Paths.get(profileDir, fileName);
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", fileName);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileName, e);
        }
    }

    /**
     * 이미지 파일 유효성 검사
     *
     * 업로드되는 파일의 크기, 확장자, 존재 여부를 검사
     * 보안과 안정성을 위해 엄격한 검증을 수행
     *
     * @param file 검사할 파일
     * @throws BusinessException 파일이 유효하지 않은 경우
     */
    private void validateImageFile(MultipartFile file) {
        
        // 파일 존재 여부 확인
        if (file.isEmpty()) {
            throw BusinessException.badRequest("파일이 비어있습니다.");
        }
        
        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 유효성 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw BusinessException.badRequest("파일명이 유효하지 않습니다.");
        }

        // 확장자 확인
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null) {
            throw BusinessException.badRequest("파일 확장자가 없습니다.");
        }

        // 허용된 이미지 형식 확인
        boolean isValidExtension = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(extension)) {
                isValidExtension = true;
                break;
            }
        }
        if (!isValidExtension) {
            throw BusinessException.badRequest("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif만 허용)");
        }
    }

    /**
     * 파일 URL 생성
     *
     * 파일명과 타입을 기반으로 접근 가능한 URL을 생성
     * 파일이 없는 경우 기본 이미지 URL을 반환
     *
     * @param fileName 파일명
     * @param type 파일 타입 (profile, chat 등)
     * @return 파일 접근 URL
     */
    public String getFileUrl(String fileName, String type) {
        if (fileName == null) {
            return "/api/v1/files/" + type + "/default.jpg";
        }
        return "/api/v1/files/" + type + "/" + fileName;
    }
}
