import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, ApiError, setSession, User } from "../api";

export default function RegisterPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const result = await api<{ token: string; user: User }>("/api/v1/auth/register", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      setSession(result.token, result.user);
      navigate("/");
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  return (
    <form className="card" onSubmit={onSubmit} style={{ maxWidth: 420 }}>
      <h1>Register</h1>
      {error ? <p className="error">{error}</p> : null}
      <label>Email</label>
      <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
      <label>Password (min 8)</label>
      <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" minLength={8} required />
      <button type="submit">Create account</button>
      <p>
        <Link to="/login">Already have an account</Link>
      </p>
    </form>
  );
}
