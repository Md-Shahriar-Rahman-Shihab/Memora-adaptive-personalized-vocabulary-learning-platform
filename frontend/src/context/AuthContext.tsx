import React, { createContext, useContext, useEffect, useState } from 'react';
import { UserResponse } from '../types/auth';
import { authApi } from '../api/authApi';
import { resetGuestSearchCount } from '../utils/guestSearchLimit';
import { LogoutConfirmationModal } from '../components/layout/LogoutConfirmationModal';

interface AuthContextType {
  user: UserResponse | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  requestLogout: () => void;
  confirmLogout: () => void;
  cancelLogout: () => void;
  isLogoutModalOpen: boolean;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('memora_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshUser = async () => {
    try {
      const res = await authApi.getCurrentUser();
      if (res.success && res.data) {
        setUser(res.data);
      }
    } catch {
      setUser(null);
      setToken(null);
      localStorage.removeItem('memora_token');
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('memora_token');
      if (storedToken) {
        setToken(storedToken);
        try {
          const res = await authApi.getCurrentUser();
          if (res.success && res.data) {
            setUser(res.data);
          }
        } catch {
          localStorage.removeItem('memora_token');
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const res = await authApi.login({ email, password });
      if (res.success && res.data) {
        const receivedToken = res.data.accessToken;
        localStorage.setItem('memora_token', receivedToken);
        setToken(receivedToken);
        setUser(res.data.user);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (name: string, email: string, password: string) => {
    setIsLoading(true);
    try {
      const res = await authApi.register({ name, email, password });
      if (res.success && res.data) {
        const receivedToken = res.data.accessToken;
        localStorage.setItem('memora_token', receivedToken);
        setToken(receivedToken);
        setUser(res.data.user);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState<boolean>(false);

  const requestLogout = () => {
    setIsLogoutModalOpen(true);
  };

  const cancelLogout = () => {
    setIsLogoutModalOpen(false);
  };

  const logout = () => {
    try {
      localStorage.removeItem('memora_token');
      if (typeof window !== 'undefined' && window.sessionStorage) {
        sessionStorage.clear();
      }
      resetGuestSearchCount();
    } catch {
      // Non-blocking cleanup fallback
    }
    setToken(null);
    setUser(null);
    setIsLogoutModalOpen(false);
  };

  const confirmLogout = () => {
    logout();
    if (typeof window !== 'undefined') {
      window.location.replace('/login');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        requestLogout,
        confirmLogout,
        cancelLogout,
        isLogoutModalOpen,
        refreshUser,
      }}
    >
      {children}
      <LogoutConfirmationModal
        isOpen={isLogoutModalOpen}
        onClose={cancelLogout}
        onConfirm={confirmLogout}
      />
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
