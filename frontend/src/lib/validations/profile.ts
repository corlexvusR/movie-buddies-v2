"use client"; // 클라이언트 전용

import { z } from "zod";

export const profileImageSchema = z.object({
  file: z
    .instanceof(File)
    .refine(
      (file) => file.size <= 5 * 1024 * 1024,
      "파일 크기는 5MB 이하여야 합니다"
    )
    .refine(
      (file) =>
        ["image/jpeg", "image/jpg", "image/png", "image/webp"].includes(
          file.type
        ),
      "지원되는 이미지 형식: JPG, PNG, WebP"
    ),
});
