import React, { createContext, useContext, useState, useCallback } from 'react';
import { Award, CheckCircle, Flame, Info, Sparkles, X, XCircle } from 'lucide-react';

export type ToastType = 'success' | 'info' | 'error' | 'xp' | 'streak' | 'achievement';

export interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  xpAmount?: number;
  streakDays?: number;
}

interface ToastContextType {
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (toast: Omit<Toast, 'id'>) => {
      const id = Math.random().toString(36).substring(2, 9);
      const newToast: Toast = { ...toast, id };
      setToasts((prev) => [...prev, newToast]);

      setTimeout(() => {
        removeToast(id);
      }, 4500);
    },
    [removeToast]
  );

  return (
    <ToastContext.Provider value={{ addToast, removeToast }}>
      {children}
      {/* Toast Render Container */}
      <div className="fixed bottom-5 right-5 z-50 flex flex-col gap-2.5 max-w-sm w-full pointer-events-none px-4">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className="pointer-events-auto flex items-start gap-3 p-4 bg-white/95 backdrop-blur-md rounded-2xl border border-black/[0.08] shadow-card transition-all duration-300 animate-slide-up"
          >
            <div className="shrink-0 mt-0.5">
              {toast.type === 'streak' && (
                <div className="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                  <Flame className="w-5 h-5 fill-current" />
                </div>
              )}
              {toast.type === 'xp' && (
                <div className="w-8 h-8 rounded-full bg-memora-green-light flex items-center justify-center text-memora-green">
                  <Sparkles className="w-5 h-5" />
                </div>
              )}
              {toast.type === 'achievement' && (
                <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                  <Award className="w-5 h-5" />
                </div>
              )}
              {toast.type === 'success' && (
                <div className="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600">
                  <CheckCircle className="w-5 h-5" />
                </div>
              )}
              {toast.type === 'error' && (
                <div className="w-8 h-8 rounded-full bg-red-100 flex items-center justify-center text-red-600">
                  <XCircle className="w-5 h-5" />
                </div>
              )}
              {toast.type === 'info' && (
                <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                  <Info className="w-5 h-5" />
                </div>
              )}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-1.5">
                <h4 className="text-sm font-semibold text-memora-dark">{toast.title}</h4>
                {toast.xpAmount && (
                  <span className="text-xs font-bold text-memora-green bg-memora-green-light px-2 py-0.5 rounded-full">
                    +{toast.xpAmount} XP
                  </span>
                )}
                {toast.streakDays && (
                  <span className="text-xs font-bold text-orange-700 bg-orange-100 px-2 py-0.5 rounded-full">
                    🔥 {toast.streakDays} Days
                  </span>
                )}
              </div>
              {toast.message && (
                <p className="text-xs text-memora-text-muted mt-0.5 leading-relaxed">{toast.message}</p>
              )}
            </div>

            <button
              onClick={() => removeToast(toast.id)}
              className="text-stone-400 hover:text-stone-600 p-1 rounded-lg transition"
              aria-label="Dismiss toast"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};
