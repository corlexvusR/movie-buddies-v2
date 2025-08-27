/**
 * 토스트 컨테이너 컴포넌트
 */

"use client";

import { useUIStore } from "@/store/uiStore";
import { CheckCircle, XCircle, AlertTriangle, Info, X } from "lucide-react";
import { cn } from "@/lib/utils";

const toastIcons = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
};

const toastStyles = {
  success: "bg-green-600 border-green-500 text-white",
  error: "bg-red-600 border-red-500 text-white",
  warning: "bg-yellow-600 border-yellow-500 text-white",
  info: "bg-blue-600 border-blue-500 text-white",
};

export function ToastContainer() {
  const { toasts, removeToast } = useUIStore();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2 max-w-sm">
      {toasts.map((toast) => {
        const Icon = toastIcons[toast.type];

        return (
          <div
            key={toast.id}
            className={cn(
              "p-4 rounded-lg border shadow-lg animate-slide-up",
              toastStyles[toast.type]
            )}
          >
            <div className="flex items-start space-x-3">
              <Icon className="h-5 w-5 mt-0.5 flex-shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="font-medium">{toast.title}</p>
                {toast.description && (
                  <p className="mt-1 text-sm opacity-90">{toast.description}</p>
                )}
              </div>
              <button
                onClick={() => removeToast(toast.id)}
                className="ml-2 flex-shrink-0 opacity-70 hover:opacity-100 transition-opacity"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>
        );
      })}
    </div>
  );
}
