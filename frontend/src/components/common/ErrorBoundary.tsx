"use client";

import React, { Component, ErrorInfo, ReactNode } from "react";
import { AlertTriangle, RefreshCw, Home, Bug } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  showDetails?: boolean;
  enableReporting?: boolean;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  errorId: string | null;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      errorId: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return {
      hasError: true,
      error,
      errorId: `error_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo,
    });

    // 에러 로깅
    console.error("ErrorBoundary caught an error:", error, errorInfo);

    // 외부 에러 핸들러 호출
    this.props.onError?.(error, errorInfo);

    // 에러 리포팅 (선택사항)
    if (this.props.enableReporting) {
      this.reportError(error, errorInfo);
    }
  }

  private reportError = async (error: Error, errorInfo: ErrorInfo) => {
    try {
      // 실제 에러 리포팅 서비스 연동 (예: Sentry, LogRocket 등)
      const errorReport = {
        message: error.message,
        stack: error.stack,
        componentStack: errorInfo.componentStack,
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent,
        url: window.location.href,
        userId: localStorage.getItem("userId") || "anonymous",
      };

      // 여기서 실제 리포팅 API 호출
      console.log("Error report would be sent:", errorReport);
    } catch (reportingError) {
      console.error("Failed to report error:", reportingError);
    }
  };

  private handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      errorId: null,
    });
  };

  private handleReload = () => {
    window.location.reload();
  };

  private handleGoHome = () => {
    window.location.href = "/";
  };

  private copyErrorDetails = () => {
    const { error, errorInfo, errorId } = this.state;
    const errorDetails = {
      errorId,
      message: error?.message,
      stack: error?.stack,
      componentStack: errorInfo?.componentStack,
      timestamp: new Date().toISOString(),
      url: window.location.href,
    };

    navigator.clipboard.writeText(JSON.stringify(errorDetails, null, 2));
    alert("에러 정보가 클립보드에 복사되었습니다.");
  };

  render() {
    if (this.state.hasError) {
      // 커스텀 fallback이 제공된 경우
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // 기본 에러 UI
      return (
        <div className="min-h-screen flex items-center justify-center p-4 bg-background-primary">
          <Card className="max-w-lg w-full">
            <CardHeader className="text-center">
              <div className="flex justify-center mb-4">
                <div className="p-3 bg-red-100 dark:bg-red-900/20 rounded-full">
                  <AlertTriangle className="h-8 w-8 text-red-500" />
                </div>
              </div>
              <CardTitle className="text-xl font-semibold text-text-primary">
                오류가 발생했습니다!
              </CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
              <p className="text-text-secondary text-center">
                예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.
              </p>

              {this.props.showDetails && this.state.error && (
                <details className="bg-background-secondary p-3 rounded border">
                  <summary className="cursor-pointer text-sm font-medium text-text-primary mb-2">
                    기술적 상세 정보
                  </summary>
                  <div className="text-xs text-text-muted font-mono">
                    <p>
                      <strong>Error ID:</strong> {this.state.errorId}
                    </p>
                    <p>
                      <strong>Message:</strong> {this.state.error.message}
                    </p>
                    {this.state.error.stack && (
                      <div className="mt-2">
                        <strong>Stack Trace:</strong>
                        <pre className="mt-1 overflow-x-auto text-xs">
                          {this.state.error.stack}
                        </pre>
                      </div>
                    )}
                  </div>
                </details>
              )}

              <div className="flex flex-col sm:flex-row gap-2">
                <Button
                  onClick={this.handleRetry}
                  className="flex-1 flex items-center justify-center space-x-2"
                >
                  <RefreshCw className="w-4 h-4" />
                  <span>다시 시도</span>
                </Button>

                <Button
                  variant="outline"
                  onClick={this.handleReload}
                  className="flex-1 flex items-center justify-center space-x-2"
                >
                  <RefreshCw className="w-4 h-4" />
                  <span>새로고침</span>
                </Button>

                <Button
                  variant="outline"
                  onClick={this.handleGoHome}
                  className="flex-1 flex items-center justify-center space-x-2"
                >
                  <Home className="w-4 h-4" />
                  <span>홈으로</span>
                </Button>
              </div>

              {this.props.showDetails && (
                <div className="text-center">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={this.copyErrorDetails}
                    className="text-xs"
                  >
                    <Bug className="w-3 h-3 mr-1" />
                    에러 정보 복사
                  </Button>
                </div>
              )}

              <p className="text-xs text-text-muted text-center">
                문제가 계속 발생한다면 고객지원팀에 문의해주세요.
              </p>
            </CardContent>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

// 함수형 컴포넌트용 HOC
export function withErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<Props, "children">
) {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${
    Component.displayName || Component.name
  })`;

  return WrappedComponent;
}

// 에러 리포팅 훅 (선택사항)
export function useErrorReporting() {
  const reportError = React.useCallback((error: Error, context?: string) => {
    console.error("Manual error report:", error, context);

    // 실제 에러 리포팅 로직
    try {
      const errorReport = {
        message: error.message,
        stack: error.stack,
        context,
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent,
        url: window.location.href,
      };

      // API 호출 또는 외부 서비스 연동
      console.log("Error would be reported:", errorReport);
    } catch (reportingError) {
      console.error("Failed to report error:", reportingError);
    }
  }, []);

  return { reportError };
}
