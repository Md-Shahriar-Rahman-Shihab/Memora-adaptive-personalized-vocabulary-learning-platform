import React from 'react';
import { AlertCircle } from 'lucide-react';
import { Button } from './Button';

interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'Something went wrong',
  message = 'We encountered an error loading this information. Please try again.',
  onRetry,
}) => {
  return (
    <div className="text-center py-10 px-6 rounded-3xl bg-red-50/50 border border-red-100 flex flex-col items-center max-w-md mx-auto my-6">
      <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center text-red-600 mb-4">
        <AlertCircle className="w-6 h-6" />
      </div>
      <h3 className="text-base font-bold text-red-950 mb-1">{title}</h3>
      <p className="text-xs text-red-700/80 mb-5 leading-relaxed">{message}</p>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry} className="border-red-200 hover:bg-red-50">
          Try Again
        </Button>
      )}
    </div>
  );
};

export default ErrorState;
