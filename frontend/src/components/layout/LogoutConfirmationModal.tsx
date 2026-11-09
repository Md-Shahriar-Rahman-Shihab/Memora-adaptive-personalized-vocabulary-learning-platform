import React from 'react';
import { LogOut, AlertCircle } from 'lucide-react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';

interface LogoutConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  isLoggingOut?: boolean;
}

/**
 * Accessible confirmation dialog for user log out.
 * Provides clear Cancel and Log Out actions with touch-friendly sizing (>=44px).
 */
export const LogoutConfirmationModal: React.FC<LogoutConfirmationModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  isLoggingOut = false,
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Confirm Log Out"
      maxWidth="sm"
      footer={
        <div className="flex items-center justify-end gap-3 w-full sm:w-auto">
          <Button
            variant="outline"
            size="md"
            onClick={onClose}
            disabled={isLoggingOut}
            className="flex-1 sm:flex-initial min-h-[44px]"
          >
            Cancel
          </Button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isLoggingOut}
            className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-full text-sm font-semibold bg-red-600 text-white hover:bg-red-700 active:scale-[0.98] transition-all shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:opacity-50 min-h-[44px] cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
            <span>{isLoggingOut ? 'Logging out...' : 'Log Out'}</span>
          </button>
        </div>
      }
    >
      <div className="flex flex-col items-center sm:items-start text-center sm:text-left gap-3.5 py-1">
        <div className="w-12 h-12 rounded-2xl bg-red-50 border border-red-100 flex items-center justify-center text-red-600 shrink-0">
          <AlertCircle className="w-6 h-6" />
        </div>
        <div>
          <h4 className="text-base font-bold text-memora-dark">
            Are you sure you want to log out?
          </h4>
          <p className="text-xs text-memora-text-muted mt-1 leading-relaxed">
            You will need to sign in again to access your personalized learning path, vocabulary reviews, and streak.
          </p>
        </div>
      </div>
    </Modal>
  );
};

export default LogoutConfirmationModal;

