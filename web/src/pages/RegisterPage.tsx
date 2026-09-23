import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, ApiError, type User } from "../api";
import { useAuth } from "../auth";

export default function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const result = await api<{ token: string; user: User }>("/api/v1/auth/register", {
        method: "POST",
        body: JSON.stringify({ username, email, password }),
      });
      login(result.token, result.user);
      navigate("/");
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  return (
    <div className="auth-wrap">
    <form className="card" onSubmit={onSubmit}>
      <h1>Register</h1>
      {error ? <p className="error">{error}</p> : null}
      <label>Username</label>
      <input value={username} onChange={(e) => setUsername(e.target.value)} minLength={3} maxLength={32} required />
      <label>Email</label>
      <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
      <label>Password (min 8)</label>
      <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" minLength={8} required />
      <button type="submit">Create account</button>
      <p>
        <Link to="/login">Already have an account</Link>
      </p>
    </form>
    </div>
  );
}
