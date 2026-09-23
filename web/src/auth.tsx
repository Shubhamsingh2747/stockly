import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { clearSession, getUser, setSession, type User } from "./api";

type AuthContextValue = {
  user: User | null;
  login: (token: string, user: User) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getUser());

  const value = useMemo<AuthContextValue>(
      () => ({
        user,
        login(token, nextUser) {
          setSession(token, nextUser);
          setUser(nextUser);
        },
        logout() {
          clearSession();
          setUser(null);
        },
      }),
      [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
