import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { authApi } from "../api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem("sf_user")); } catch { return null; }
  });
  const [loading, setLoading] = useState(!!localStorage.getItem("sf_token"));

  const store = (data) => {
    localStorage.setItem("sf_token", data.token);
    localStorage.setItem("sf_user", JSON.stringify(data.user));
    setUser(data.user);
    return data.user;
  };

  const login = async (email, password) => store(await authApi.login({ email, password }));
  const register = async (body) => store(await authApi.register(body));

  const logout = useCallback(() => {
    localStorage.removeItem("sf_token");
    localStorage.removeItem("sf_user");
    setUser(null);
  }, []);

  const updateUser = (u) => {
    localStorage.setItem("sf_user", JSON.stringify(u));
    setUser(u);
  };

  // On page refresh, confirm the stored token is still valid
  useEffect(() => {
    if (!localStorage.getItem("sf_token")) { setLoading(false); return; }
    authApi.me().then(updateUser).catch(logout).finally(() => setLoading(false));
  }, [logout]);

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser, isAdmin: user?.role === "ADMIN" }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
