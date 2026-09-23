import type { ReactNode } from "react";
import { Navigate, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProductsPage from "./pages/ProductsPage";
import SalesPage from "./pages/SalesPage";
import PurchasesPage from "./pages/PurchasesPage";
import MovementsPage from "./pages/MovementsPage";

function Guard({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  return user ? <>{children}</> : <Navigate to="/login" replace />;
}

function GuestOnly({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  return user ? <Navigate to="/" replace /> : <>{children}</>;
}

function Shell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function onLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="layout">
      <nav className="nav">
        <NavLink className="brand" to={user ? "/" : "/login"}>
          Stockly
        </NavLink>
        {user ? (
          <div className="nav-end">
            <div className="user-block">
              <span className="user-name">{user.username || user.email.split("@")[0]}</span>
              <span className="user-role">{user.role === "ADMIN" ? "Administrator" : "User"}</span>
            </div>
            <button className="logout" type="button" onClick={onLogout}>
              Sign out
            </button>
          </div>
        ) : (
          <div className="nav-end">
            <NavLink className="page-link" to="/login">
              Sign in
            </NavLink>
            <NavLink className="page-link" to="/register">
              Register
            </NavLink>
          </div>
        )}
      </nav>
      <div className="content">
      <Routes>
        <Route
          path="/login"
          element={
            <GuestOnly>
              <LoginPage />
            </GuestOnly>
          }
        />
        <Route
          path="/register"
          element={
            <GuestOnly>
              <RegisterPage />
            </GuestOnly>
          }
        />
        <Route
          path="/"
          element={
            <Guard>
              <ProductsPage />
            </Guard>
          }
        />
        <Route
          path="/sales"
          element={
            <Guard>
              <SalesPage />
            </Guard>
          }
        />
        <Route
          path="/purchases"
          element={
            <Guard>
              <PurchasesPage />
            </Guard>
          }
        />
        <Route
          path="/products/:id/movements"
          element={
            <Guard>
              <MovementsPage />
            </Guard>
          }
        />
      </Routes>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Shell />
    </AuthProvider>
  );
}
