import { cn } from "@/lib/utils";

interface LoadingProps {
  size?: "sm" | "md" | "lg" | "xl";
  variant?: "spinner" | "dots" | "pulse" | "skeleton";
  className?: string;
  text?: string;
}

const sizeClasses = {
  sm: "w-4 h-4",
  md: "w-8 h-8",
  lg: "w-12 h-12",
  xl: "w-16 h-16",
};

export function Loading({
  size = "md",
  variant = "spinner",
  className,
  text,
}: LoadingProps) {
  if (variant === "spinner") {
    return (
      <div
        className={cn("flex flex-col items-center justify-center", className)}
      >
        <svg
          className={cn("animate-spin text-accent-primary", sizeClasses[size])}
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 0 1 4 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        {text && <p className="mt-2 text-sm text-text-muted">{text}</p>}
      </div>
    );
  }

  if (variant === "dots") {
    return (
      <div
        className={cn("flex items-center justify-center space-x-1", className)}
      >
        {[0, 1, 2].map((i) => (
          <div
            key={i}
            className={cn(
              "bg-accent-primary rounded-full animate-pulse",
              size === "sm"
                ? "w-2 h-2"
                : size === "md"
                ? "w-3 h-3"
                : size === "lg"
                ? "w-4 h-4"
                : "w-5 h-5"
            )}
            style={{
              animationDelay: `${i * 0.2}s`,
              animationDuration: "1.4s",
            }}
          />
        ))}
        {text && <span className="ml-2 text-sm text-text-muted">{text}</span>}
      </div>
    );
  }

  if (variant === "pulse") {
    return (
      <div className={cn("animate-pulse", className)}>
        <div
          className={cn("bg-background-tertiary rounded", sizeClasses[size])}
        />
        {text && <p className="mt-2 text-sm text-text-muted">{text}</p>}
      </div>
    );
  }

  if (variant === "skeleton") {
    return (
      <div className={cn("animate-pulse space-y-3", className)}>
        <div className="bg-background-tertiary rounded h-4 w-3/4" />
        <div className="bg-background-tertiary rounded h-4 w-1/2" />
        <div className="bg-background-tertiary rounded h-4 w-5/6" />
        {text && <p className="mt-2 text-sm text-text-muted">{text}</p>}
      </div>
    );
  }

  return null;
}

// LoadingSpinner는 Loading의 별칭으로 사용
export function LoadingSpinner({
  size = "md",
  className,
}: {
  size?: "sm" | "md" | "lg";
  className?: string;
}) {
  return <Loading size={size} variant="spinner" className={className} />;
}
