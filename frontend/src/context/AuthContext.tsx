import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Company, AuthResponse } from '../types';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  company: Company | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: { email: string; password: string }) => Promise<void>;
  register: (data: { fullName: string; email: string; password: string; companyName: string }) => Promise<void>;
  logout: () => Promise<void>;
  updateCompany: (company: Company) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [company, setCompany] = useState<Company | null>(() => {
    const saved = localStorage.getItem('company');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadUser() {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const authData = await api.auth.me();
          setUser(authData.user);
          setCompany(authData.company);
          localStorage.setItem('user', JSON.stringify(authData.user));
          localStorage.setItem('company', JSON.stringify(authData.company));
        } catch {
          // Token expired or invalid
          setUser(null);
          setCompany(null);
          setToken(null);
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          localStorage.removeItem('company');
        }
      }
      setIsLoading(false);
    }
    loadUser();
  }, []);

  const handleAuthSuccess = (res: AuthResponse) => {
    setToken(res.token);
    setUser(res.user);
    setCompany(res.company);
    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify(res.user));
    localStorage.setItem('company', JSON.stringify(res.company));
  };

  const login = async (data: { email: string; password: string }) => {
    const res = await api.auth.login(data);
    handleAuthSuccess(res);
  };

  const register = async (data: { fullName: string; email: string; password: string; companyName: string }) => {
    const res = await api.auth.register(data);
    handleAuthSuccess(res);
  };

  const logout = async () => {
    try {
      await api.auth.logout();
    } catch {
      // Ignore network errors on logout
    } finally {
      setUser(null);
      setCompany(null);
      setToken(null);
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('company');
    }
  };

  const updateCompany = (newCompany: Company) => {
    setCompany(newCompany);
    localStorage.setItem('company', JSON.stringify(newCompany));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        company,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        updateCompany,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
