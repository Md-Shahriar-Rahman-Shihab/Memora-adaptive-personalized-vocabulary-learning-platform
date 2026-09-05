import React from 'react';
import { Button } from './Button';

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description: string;
  actionText?: string;
  onAction?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  actionText,
  onAction,
}) => {
  return (
    <div className="text-center py-12 px-6 rounded-3xl bg-stone-50/70 border border-black/[0.04] flex flex-col items-center max-w-lg mx-auto">
      {icon && <div className="mb-4 text-stone-400">{icon}</div>}
      <h3 className="text-lg font-bold text-memora-dark mb-1.5">{title}</h3>
      <p className="text-sm text-memora-text-muted mb-6 leading-relaxed">{description}</p>
      {actionText && onAction && (
        <Button variant="primary" size="md" onClick={onAction}>
          {actionText}
        </Button>
      )}
    </div>
  );
};

export default EmptyState;
