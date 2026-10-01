import React, { useEffect } from 'react';
import { X } from 'lucide-react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl';
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  footer,
  maxWidth = 'md',
}) => {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.body.style.overflow = 'unset';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const maxWidthStyles = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/50 backdrop-blur-xs animate-fade-in">
      <div
        className={`w-full ${maxWidthStyles[maxWidth]} max-h-[calc(100dvh-2rem)] sm:max-h-[calc(100vh-3rem)] flex flex-col bg-white rounded-3xl shadow-2xl border border-black/[0.08] overflow-hidden transform transition-all animate-scale-up`}
      >
        {title && (
          <div className="flex items-center justify-between px-6 py-4 border-b border-black/[0.06] shrink-0 bg-white">
            <h3 className="text-lg font-bold text-memora-dark">{title}</h3>
            <button
              onClick={onClose}
              className="p-1 rounded-full text-stone-400 hover:text-stone-700 hover:bg-stone-100 transition"
              aria-label="Close modal"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        )}
        <div className="flex-1 min-h-0 overflow-y-auto p-6">{children}</div>
        {footer && (
          <div className="px-6 py-4 border-t border-black/[0.06] bg-stone-50/90 shrink-0 flex items-center justify-end gap-2.5">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
};


export default Modal;
