import type { ReactNode } from "react";
import { Navigate, NavLink, Route, Routes } from "react-router-dom";
import { clearSession, getUser } from "./api";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProductsPage from "./pages/ProductsPage";
import SalesPage from "./pages/SalesPage";
import PurchasesPage from "./pages/PurchasesPage";
import MovementsPage from "./pages/MovementsPage";

function Guard({ children }: { children: ReactNode }) {
  return getUser() ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  const user = getUser();

  function logout() {
    clearSession();
    window.location.href = "/login";
  }

  return (
    <div className="layout">
      <nav className="nav">
        <NavLink className="brand" to="/">
          Stockly
        </NavLink>
        {user ? (
          <>
            <NavLink className="page-link" to="/">
              Products
            </NavLink>
            <NavLink className="page-link" to="/sales">
              Sales orders
            </NavLink>
            <NavLink className="page-link" to="/purchases">
              Purchase orders
            </NavLink>
            <span className="muted">
              {user.email} ({user.role})
            </span>
            <button className="secondary" type="button" onClick={logout}>
              Log out
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login">Login</NavLink>
            <NavLink to="/register">Register</NavLink>
          </>
        )}
      </nav>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
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
  );
}
