import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, ApiError, type User } from "../api";
import { useAuth } from "../auth";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("admin@stockly.local");
  const [password, setPassword] = useState("Admin@123");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const result = await api<{ token: string; user: User }>("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      login(result.token, result.user);
      navigate("/");
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  return (
    <form className="card" onSubmit={onSubmit} style={{ maxWidth: 420 }}>
      <h1>Login</h1>
      <p className="muted">Demo admin is pre-filled. Register another user if you want.</p>
      {error ? <p className="error">{error}</p> : null}
      <label>Email</label>
      <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
      <label>Password</label>
      <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
      <button type="submit">Sign in</button>
      <p>
        <Link to="/register">Create an account</Link>
      </p>
    </form>
  );
}
