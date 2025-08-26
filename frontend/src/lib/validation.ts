import { z } from "zod";

// 공통 검증 규칙
const passwordSchema = z
  .string()
  .min(8, "비밀번호는 최소 8자 이상이어야 합니다")
  .regex(/^(?=.*[a-zA-Z])(?=.*\d)/, "비밀번호는 영문과 숫자를 포함해야 합니다");

const usernameSchema = z
  .string()
  .min(3, "사용자명은 최소 3자 이상이어야 합니다")
  .max(20, "사용자명은 20자를 초과할 수 없습니다")
  .regex(
    /^[a-zA-Z0-9_]+$/,
    "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다"
  );

const nicknameSchema = z
  .string()
  .min(2, "닉네임은 최소 2자 이상이어야 합니다")
  .max(20, "닉네임은 20자를 초과할 수 없습니다"); // 백엔드와 일치 (10자 -> 20자)

const emailSchema = z.string().email("올바른 이메일 형식을 입력해주세요");

// 인증 관련 스키마
export const loginSchema = z.object({
  username: usernameSchema,
  password: z.string().min(1, "비밀번호를 입력해주세요"),
});

// 백엔드 SignupRequest와 일치
export const signupSchema = z.object({
  username: usernameSchema,
  email: emailSchema,
  nickname: nicknameSchema,
  password: passwordSchema, // password1, password2가 아닌 단일 password
});

// 프론트엔드 전용 - 비밀번호 확인 포함
export const signupFormSchema = z
  .object({
    username: usernameSchema,
    email: emailSchema,
    nickname: nicknameSchema,
    password: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "비밀번호가 일치하지 않습니다",
    path: ["confirmPassword"],
  });

export const passwordChangeSchema = z
  .object({
    currentPassword: z.string().min(1, "현재 비밀번호를 입력해주세요"),
    newPassword: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "새 비밀번호가 일치하지 않습니다",
    path: ["confirmPassword"],
  });

// 사용자 관련 스키마
export const userUpdateSchema = z.object({
  nickname: nicknameSchema.optional(),
  email: emailSchema.optional(),
});

// 리뷰 관련 스키마 - 백엔드와 일치
export const reviewSchema = z.object({
  movieId: z.number().min(1, "영화를 선택해주세요"),
  content: z
    .string()
    .min(10, "리뷰는 최소 10자 이상 작성해주세요")
    .max(500, "리뷰는 500자를 초과할 수 없습니다"),
  rating: z
    .number()
    .min(1, "평점은 1점 이상이어야 합니다")
    .max(5, "평점은 5점 이하여야 합니다"),
});

// 영화 검색 스키마 - 백엔드 MovieSearchRequest와 일치
export const movieSearchSchema = z.object({
  title: z.string().optional(),
  genreId: z.number().optional(),
  actorName: z.string().optional(),
  releaseYear: z // year -> releaseYear
    .number()
    .min(1900, "1900년 이후 연도를 입력해주세요")
    .max(2026, "2026년 이전 연도를 입력해주세요")
    .optional(),
  minRating: z
    .number()
    .min(0, "최소 평점은 0점 이상이어야 합니다")
    .max(10, "최소 평점은 10점 이하여야 합니다")
    .optional(),
  maxRating: z
    .number()
    .min(0, "최대 평점은 0점 이상이어야 합니다")
    .max(10, "최대 평점은 10점 이하여야 합니다")
    .optional(),
  minRuntime: z
    .number()
    .min(0, "최소 상영시간은 0분 이상이어야 합니다")
    .optional(),
  maxRuntime: z
    .number()
    .min(0, "최대 상영시간은 0분 이상이어야 합니다")
    .optional(),
  nowPlaying: z.boolean().optional(),
});

// 친구 요청 스키마 - 백엔드와 일치
export const friendRequestSchema = z.object({
  username: usernameSchema,
});

// 채팅 관련 스키마 - 백엔드와 일치
export const chatRoomCreateSchema = z.object({
  name: z
    .string()
    .min(2, "채팅방 이름은 2자 이상이어야 합니다")
    .max(50, "채팅방 이름은 50자를 초과할 수 없습니다"),
  description: z
    .string()
    .max(255, "채팅방 설명은 255자를 초과할 수 없습니다")
    .optional(),
  maxParticipants: z
    .number()
    .min(2, "최소 참가자 수는 2명입니다")
    .max(100, "최대 참가자 수는 100명입니다")
    .optional(),
});

export const chatMessageSchema = z.object({
  content: z
    .string()
    .min(1, "메시지를 입력해주세요")
    .max(500, "메시지는 500자를 초과할 수 없습니다"),
});

// 기타 스키마들
export const searchQuerySchema = z.object({
  query: z
    .string()
    .min(1, "검색어를 입력해주세요")
    .max(100, "검색어는 100자를 초과할 수 없습니다"),
  page: z.number().min(1).optional(),
  size: z.number().min(1).max(100).optional(),
});

export const paginationSchema = z.object({
  page: z.number().min(1, "페이지는 1 이상이어야 합니다"),
  size: z
    .number()
    .min(1, "페이지 크기는 1 이상이어야 합니다")
    .max(100, "페이지 크기는 100 이하여야 합니다"),
  sort: z.string().optional(),
});

// 타입 추출
export type LoginForm = z.infer<typeof loginSchema>;
export type SignupForm = z.infer<typeof signupSchema>;
export type SignupFormWithConfirm = z.infer<typeof signupFormSchema>;
export type PasswordChangeForm = z.infer<typeof passwordChangeSchema>;
export type UserUpdateForm = z.infer<typeof userUpdateSchema>;
export type ReviewForm = z.infer<typeof reviewSchema>;
export type MovieSearchForm = z.infer<typeof movieSearchSchema>;
export type FriendRequestForm = z.infer<typeof friendRequestSchema>;
export type ChatRoomCreateForm = z.infer<typeof chatRoomCreateSchema>;
export type ChatMessageForm = z.infer<typeof chatMessageSchema>;
export type SearchQueryForm = z.infer<typeof searchQuerySchema>;
export type PaginationForm = z.infer<typeof paginationSchema>;

// 기존 검증 유틸리티 함수들 유지
export const validateForm = <T>(
  schema: z.ZodSchema<T>,
  data: unknown
):
  | { success: true; data: T }
  | { success: false; errors: Record<string, string> } => {
  try {
    const result = schema.parse(data);
    return { success: true, data: result };
  } catch (error) {
    if (error instanceof z.ZodError) {
      const errors: Record<string, string> = {};
      error.issues.forEach((issue: z.ZodIssue) => {
        if (issue.path) {
          errors[issue.path.join(".")] = issue.message;
        }
      });
      return { success: false, errors };
    }
    return {
      success: false,
      errors: { general: "검증 중 오류가 발생했습니다" },
    };
  }
};

export const isValidEmail = (email: string): boolean => {
  return emailSchema.safeParse(email).success;
};

export const getPasswordStrength = (
  password: string
): "weak" | "medium" | "strong" => {
  if (password.length < 8) return "weak";

  const hasLower = /[a-z]/.test(password);
  const hasUpper = /[A-Z]/.test(password);
  const hasDigit = /\d/.test(password);
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

  const score = [hasLower, hasUpper, hasDigit, hasSpecial].filter(
    Boolean
  ).length;

  if (score >= 3) return "strong";
  if (score >= 2) return "medium";
  return "weak";
};
