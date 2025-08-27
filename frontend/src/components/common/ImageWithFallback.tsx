"use client";

import { useState, useCallback } from "react";
import Image, { ImageProps } from "next/image";
import { User, Film, Image as ImageIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface ImageWithFallbackProps extends Omit<ImageProps, "onError"> {
  fallbackSrc?: string;
  fallbackType?: "profile" | "movie" | "generic";
  showIcon?: boolean;
  iconSize?: number;
  className?: string;
}

const fallbackIcons = {
  profile: User,
  movie: Film,
  generic: ImageIcon,
};

export function ImageWithFallback({
  src,
  alt,
  fallbackSrc,
  fallbackType = "generic",
  showIcon = true,
  iconSize = 24,
  className,
  ...props
}: ImageWithFallbackProps) {
  const [currentSrc, setCurrentSrc] = useState(src);
  const [hasError, setHasError] = useState(false);

  const handleError = useCallback(() => {
    if (currentSrc === src && fallbackSrc) {
      // 첫 번째 실패: fallback 이미지로 시도
      setCurrentSrc(fallbackSrc);
    } else {
      // 두 번째 실패 또는 fallback이 없음: 아이콘 표시
      setHasError(true);
    }
  }, [currentSrc, src, fallbackSrc]);

  const handleLoad = useCallback(() => {
    setHasError(false);
  }, []);

  if (hasError && showIcon) {
    const IconComponent = fallbackIcons[fallbackType];

    return (
      <div
        className={cn(
          "flex items-center justify-center bg-background-secondary text-text-muted",
          className
        )}
        style={{ width: props.width, height: props.height }}
      >
        <IconComponent size={iconSize} />
      </div>
    );
  }

  if (hasError && !showIcon) {
    return (
      <div
        className={cn("bg-background-secondary", className)}
        style={{ width: props.width, height: props.height }}
      />
    );
  }

  return (
    <Image
      {...props}
      src={currentSrc}
      alt={alt}
      className={className}
      onError={handleError}
      onLoad={handleLoad}
    />
  );
}
