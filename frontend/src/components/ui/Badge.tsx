import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'green' | 'amber' | 'blue' | 'purple' | 'red' | 'neutral' | 'dark';
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  className,
  variant = 'green',
  size = 'md',
  ...props
}) => {
  const baseStyles = 'inline-flex items-center font-semibold rounded-full select-none';

  const sizeStyles = {
    sm: 'text-[11px] px-2.5 py-0.5 gap-1',
    md: 'text-xs px-3 py-1 gap-1.5',
  };

  const variantStyles = {
    green: 'bg-[#EAF2DE] text-[#425E16] border border-[#D5E6BE]',
    amber: 'bg-[#FEF3E2] text-[#B45309] border border-[#FDE3B7]',
    blue: 'bg-[#E6F0FA] text-[#1D4ED8] border border-[#BFDBFE]',
    purple: 'bg-[#F3E8FF] text-[#7E22CE] border border-[#E9D5FF]',
    red: 'bg-[#FEE2E2] text-[#B91C1C] border border-[#FECACA]',
    neutral: 'bg-stone-100 text-stone-700 border border-stone-200',
    dark: 'bg-[#263126] text-emerald-300 border border-emerald-900/40',
  };

  return (
    <span className={twMerge(clsx(baseStyles, sizeStyles[size], variantStyles[variant], className))} {...props}>
      {children}
    </span>
  );
};

export default Badge;
