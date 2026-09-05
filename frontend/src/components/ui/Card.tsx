import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'subtle' | 'dark' | 'glass';
  hover?: boolean;
}

export const Card: React.FC<CardProps> = ({
  children,
  className,
  variant = 'default',
  hover = false,
  ...props
}) => {
  const baseStyles = 'rounded-3xl border transition-all duration-200';

  const variantStyles = {
    default: 'bg-white border-black/[0.06] shadow-card',
    subtle: 'bg-[#F8F8F5] border-black/[0.04]',
    dark: 'bg-[#171F17] text-white border-white/[0.08] shadow-2xl',
    glass: 'bg-white/80 backdrop-blur-md border-white/60 shadow-card',
  };

  const hoverStyles = hover
    ? 'hover:-translate-y-0.5 hover:shadow-lg hover:border-black/[0.12] cursor-pointer'
    : '';

  return (
    <div className={twMerge(clsx(baseStyles, variantStyles[variant], hoverStyles, className))} {...props}>
      {children}
    </div>
  );
};

export default Card;
