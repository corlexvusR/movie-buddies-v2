import { useAuthStore } from "@/store/authStore";
import type { UserResponse } from "@/types/auth";

// 호환성을 위한 별칭
export type User = UserResponse;

/**
 * 인증 관련 커스텀 훅
 */
export const useAuth = () => {
  const {
    isAuthenticated,
    user,
    loading,
    error,
    login,
    signup,
    logout,
    refreshAuthToken,
    fetchUser,
    updateUser,
    clearError,
    setLoading,
  } = useAuthStore();

  return {
    // 상태
    isAuthenticated,
    user,
    isLoading: loading,
    error,

    // 액션
    login,
    signup,
    logout,
    refreshToken: refreshAuthToken,
    fetchUser,
    updateUser,
    clearError,
    setLoading,

    // 계산된 값들
    username: user?.username,
    nickname: user?.nickname,
    email: user?.email,
    profileImage: user?.profileImageUrl,
  };
};
