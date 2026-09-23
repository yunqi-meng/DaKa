package com.daka.server.controller;

import com.daka.server.dto.ApiResponse;
import com.daka.server.dto.MediaDto;
import com.daka.server.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "mp4", "mov", "m4v");

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private final UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public MediaController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ApiResponse<MediaDto> upload(@RequestHeader("Authorization") String token,
                                        @RequestParam("file") MultipartFile file) throws IOException {
        userService.getUserByToken(token);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件超过大小限制");
        }

        // 只取原始文件名的扩展名,文件名本身用 UUID,杜绝路径遍历
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型: " + ext);
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "." + ext;
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        MediaDto dto = new MediaDto();
        dto.setId(UUID.randomUUID().toString());
        dto.setRemoteUrl("/uploads/" + fileName);
        dto.setMediaType(Set.of("mp4", "mov", "m4v").contains(ext) ? 2 : 1);
        return ApiResponse.success(dto);
    }
}
