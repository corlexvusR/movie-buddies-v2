"use client";

import React, { forwardRef, useState, ReactElement } from "react";
import { ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

interface SelectProps {
  value?: string;
  onValueChange?: (value: string) => void;
  placeholder?: string;
  children: React.ReactNode;
  disabled?: boolean;
  className?: string;
}

interface SelectItemProps {
  value: string;
  children: React.ReactNode;
  disabled?: boolean;
}

// Select 컨텍스트
const SelectContext = React.createContext<{
  value?: string;
  onSelect: (value: string) => void;
  onOpenChange: (open: boolean) => void;
} | null>(null);

const Select = forwardRef<HTMLButtonElement, SelectProps>(
  (
    {
      value,
      onValueChange,
      placeholder = "선택하세요",
      children,
      disabled,
      className,
    },
    ref
  ) => {
    const [isOpen, setIsOpen] = useState(false);

    const handleSelect = (selectedValue: string) => {
      onValueChange?.(selectedValue);
      setIsOpen(false);
    };

    // 선택된 값에 해당하는 children의 텍스트를 찾는 함수
    const getSelectedText = () => {
      if (!value) return placeholder;

      const childrenArray = React.Children.toArray(children);
      const selectedChild = childrenArray.find((child) => {
        if (React.isValidElement(child) && typeof child.type !== "string") {
          const childProps = child.props as SelectItemProps;
          return childProps.value === value;
        }
        return false;
      }) as ReactElement<SelectItemProps> | undefined;

      return selectedChild?.props.children || value;
    };

    return (
      <SelectContext.Provider
        value={{ value, onSelect: handleSelect, onOpenChange: setIsOpen }}
      >
        <div className="relative">
          <button
            ref={ref}
            type="button"
            className={cn(
              "flex h-10 w-full items-center justify-between rounded-lg border px-3 py-2 text-sm",
              "bg-background-secondary border-border-secondary text-text-primary",
              "focus:outline-none focus:ring-2 focus:ring-accent-primary focus:border-transparent",
              "disabled:cursor-not-allowed disabled:opacity-50",
              className
            )}
            onClick={() => !disabled && setIsOpen(!isOpen)}
            disabled={disabled}
            aria-expanded={isOpen}
            aria-haspopup="listbox"
          >
            <span className={value ? "text-text-primary" : "text-text-muted"}>
              {getSelectedText()}
            </span>
            <ChevronDown
              className={cn(
                "h-4 w-4 transition-transform",
                isOpen && "rotate-180"
              )}
            />
          </button>

          {isOpen && !disabled && (
            <>
              <div
                className="fixed inset-0 z-40"
                onClick={() => setIsOpen(false)}
              />
              <div className="absolute top-full left-0 z-50 mt-1 w-full min-w-[8rem] rounded-lg border border-border-primary bg-background-secondary shadow-lg">
                <div className="p-1 max-h-60 overflow-auto">{children}</div>
              </div>
            </>
          )}
        </div>
      </SelectContext.Provider>
    );
  }
);

const SelectItem = ({ value, children, disabled }: SelectItemProps) => {
  const context = React.useContext(SelectContext);
  if (!context) {
    throw new Error("SelectItem must be used within Select");
  }

  const { value: selectedValue, onSelect } = context;
  const isSelected = selectedValue === value;

  return (
    <button
      type="button"
      className={cn(
        "w-full px-2 py-1.5 text-sm text-left rounded transition-colors",
        "hover:bg-background-tertiary focus:bg-background-tertiary focus:outline-none",
        isSelected && "bg-accent-primary text-white hover:bg-accent-primary",
        disabled && "opacity-50 cursor-not-allowed"
      )}
      onClick={() => !disabled && onSelect(value)}
      disabled={disabled}
    >
      {children}
    </button>
  );
};

Select.displayName = "Select";
SelectItem.displayName = "SelectItem";

export { Select, SelectItem };
