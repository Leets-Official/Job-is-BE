package com.leets7th.job_is_be.domain.user.enums;

/**
 * 업로드 허용 파일 형식 (확장자 기준)
 */
public enum ResumeFileFormat {
    PDF,
    DOCX,
    HWP,
    HWPX;

    public static ResumeFileFormat fromExtension(String extension) {
        return valueOf(extension.toUpperCase());
    }
}
