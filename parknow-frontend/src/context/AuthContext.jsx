import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('parknow_jwt') || '');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      fetchUserProfile();
    } else {
      setLoading(false);
    }
  }, [token]);

  const fetchUserProfile = async () => {
    try {
      const res = await api.get('/users/me');
      if (res.success) {
        setUser(res.data);
      }
    } catch (err) {
      console.error('Failed to fetch profile:', err.message);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    if (res.success && res.data.accessToken) {
      const jwt = res.data.accessToken;
      localStorage.setItem('parknow_jwt', jwt);
      setToken(jwt);
      setUser({
        id: res.data.userId,
        email: res.data.email,
        fullName: res.data.fullName,
        roles: res.data.roles,
      });
      return res.data;
    }
    throw new Error(res.message || 'Login failed');
  };

  const register = async (fullName, email, password, phoneNumber) => {
    const res = await api.post('/auth/register', { fullName, email, password, phoneNumber });
    if (res.success && res.data.accessToken) {
      const jwt = res.data.accessToken;
      localStorage.setItem('parknow_jwt', jwt);
      setToken(jwt);
      setUser({
        id: res.data.userId,
        email: res.data.email,
        fullName: res.data.fullName,
        roles: res.data.roles,
      });
      return res.data;
    }
    throw new Error(res.message || 'Registration failed');
  };

  const logout = () => {
    localStorage.removeItem('parknow_jwt');
    setToken('');
    setUser(null);
  };

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAdmin, refreshProfile: fetchUserProfile }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
