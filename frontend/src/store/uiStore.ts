import { create } from "zustand";
import { storage } from "@/lib/utils";

export interface Toast {
  id: string;
  type: "success" | "error" | "warning" | "info";
  title: string;
  description?: string;
  duration?: number;
}

interface UIState {
  modals: Record<string, boolean>;
  toasts: Toast[];
  sidebarOpen: boolean;
  mobileNavOpen: boolean;
  globalLoading: boolean;
  pageTitle: string;
}

interface UIActions {
  openModal: (modalId: string) => void;
  closeModal: (modalId: string) => void;
  toggleModal: (modalId: string) => void;
  addToast: (toast: Omit<Toast, "id">) => void;
  removeToast: (id: string) => void;
  clearToasts: () => void;
  toggleSidebar: () => void;
  setSidebarOpen: (open: boolean) => void;
  toggleMobileNav: () => void;
  setMobileNavOpen: (open: boolean) => void;
  setGlobalLoading: (loading: boolean) => void;
  setPageTitle: (title: string) => void;
}

type UIStore = UIState & UIActions;

const SIDEBAR_STORAGE_KEY = "moviebuddies_sidebar_open";

export const useUIStore = create<UIStore>((set, get) => ({
  // 초기 상태 - localStorage에서 사이드바 상태 복원
  modals: {},
  toasts: [],
  sidebarOpen:
    typeof window !== "undefined"
      ? storage.get<boolean>(SIDEBAR_STORAGE_KEY) ?? true
      : true,
  mobileNavOpen: false,
  globalLoading: false,
  pageTitle: "MovieBuddies",

  openModal: (modalId: string) => {
    set((state) => ({
      modals: { ...state.modals, [modalId]: true },
    }));
  },

  closeModal: (modalId: string) => {
    set((state) => ({
      modals: { ...state.modals, [modalId]: false },
    }));
  },

  toggleModal: (modalId: string) => {
    set((state) => ({
      modals: { ...state.modals, [modalId]: !state.modals[modalId] },
    }));
  },

  addToast: (toast: Omit<Toast, "id">) => {
    const id = Math.random().toString(36).substring(2, 9);
    const newToast: Toast = {
      ...toast,
      id,
      duration: toast.duration || 5000,
    };

    set((state) => ({
      toasts: [...state.toasts, newToast],
    }));

    setTimeout(() => {
      get().removeToast(id);
    }, newToast.duration);
  },

  removeToast: (id: string) => {
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    }));
  },

  clearToasts: () => {
    set({ toasts: [] });
  },

  toggleSidebar: () => {
    const newState = !get().sidebarOpen;
    set({ sidebarOpen: newState });
    storage.set(SIDEBAR_STORAGE_KEY, newState);
  },

  setSidebarOpen: (open: boolean) => {
    set({ sidebarOpen: open });
    storage.set(SIDEBAR_STORAGE_KEY, open);
  },

  toggleMobileNav: () => {
    set((state) => ({ mobileNavOpen: !state.mobileNavOpen }));
  },

  setMobileNavOpen: (open: boolean) => {
    set({ mobileNavOpen: open });
  },

  setGlobalLoading: (loading: boolean) => {
    set({ globalLoading: loading });
  },

  setPageTitle: (title: string) => {
    set({ pageTitle: title });
    if (typeof document !== "undefined") {
      document.title = `${title} | MovieBuddies`;
    }
  },
}));

export const showToast = {
  success: (title: string, description?: string) => {
    useUIStore.getState().addToast({ type: "success", title, description });
  },

  error: (title: string, description?: string) => {
    useUIStore.getState().addToast({ type: "error", title, description });
  },

  warning: (title: string, description?: string) => {
    useUIStore.getState().addToast({ type: "warning", title, description });
  },

  info: (title: string, description?: string) => {
    useUIStore.getState().addToast({ type: "info", title, description });
  },
};
