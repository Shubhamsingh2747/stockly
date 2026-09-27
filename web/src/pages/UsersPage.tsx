import { FormEvent, useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { api, ApiError, ROLE_OPTIONS, type Role, type User } from "../api";
import { useAuth } from "../auth";

type ManagedUser = User & { enabled: boolean; createdAt?: string };

export default function UsersPage() {
  const { user: actor } = useAuth();
  const [users, setUsers] = useState<ManagedUser[]>([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    role: "USER" as Role,
  });
  const [resetId, setResetId] = useState<number | null>(null);
  const [resetPassword, setResetPassword] = useState("");

  async function load() {
    setError("");
    try {
      setUsers(await api<ManagedUser[]>("/api/v1/users"));
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  if (actor?.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  async function createUser(event: FormEvent) {
    event.preventDefault();
    setError("");
    setNotice("");
    try {
      await api("/api/v1/users", { method: "POST", body: JSON.stringify(form) });
      setForm({ username: "", email: "", password: "", role: "USER" });
      setNotice("User created.");
      await load();
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  async function patchUser(id: number, body: { role?: Role; enabled?: boolean }) {
    setError("");
    setNotice("");
    try {
      await api(`/api/v1/users/${id}`, { method: "PATCH", body: JSON.stringify(body) });
      await load();
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  async function submitReset(event: FormEvent) {
    event.preventDefault();
    if (resetId == null) {
      return;
    }
    setError("");
    setNotice("");
    try {
      await api(`/api/v1/users/${resetId}/reset-password`, {
        method: "POST",
        body: JSON.stringify({ password: resetPassword }),
      });
      setResetId(null);
      setResetPassword("");
      setNotice("Password updated.");
    } catch (err) {
      setError((err as ApiError).message);
    }
  }

  return (
    <>
      <h1>Users</h1>
      <p>
        <Link className="btn secondary" to="/">
          Back to products
        </Link>
      </p>
      {error ? <p className="error">{error}</p> : null}
      {notice ? <p className="muted">{notice}</p> : null}

      <form className="card" onSubmit={createUser}>
        <h2>Create user</h2>
        <div className="row">
          <div>
            <label>Username</label>
            <input
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              minLength={3}
              required
            />
          </div>
          <div>
            <label>Email</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>
          <div>
            <label>Password</label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              minLength={8}
              required
            />
          </div>
          <div>
            <label>Role</label>
            <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as Role })}>
              {ROLE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
        <button type="submit">Create</button>
      </form>

      {resetId != null ? (
        <form className="card" onSubmit={submitReset}>
          <h2>Reset password</h2>
          <p className="muted">User id {resetId}</p>
          <label>New password</label>
          <input
            type="password"
            value={resetPassword}
            onChange={(e) => setResetPassword(e.target.value)}
            minLength={8}
            required
          />
          <button type="submit">Save password</button>
          <button className="btn-edit" type="button" onClick={() => setResetId(null)}>
            Cancel
          </button>
        </form>
      ) : null}

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th className="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>
                  <select
                    className="role-select"
                    value={u.role}
                    aria-label={`Role for ${u.username}`}
                    onChange={(e) => {
                      const role = e.target.value as Role;
                      if (role !== u.role) {
                        void patchUser(u.id, { role });
                      }
                    }}
                  >
                    {ROLE_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </td>
                <td>{u.enabled ? "Active" : "Disabled"}</td>
                <td className="col-actions">
                  <div className="actions">
                    <button
                      className={u.enabled ? "danger" : "btn-edit"}
                      type="button"
                      onClick={() => void patchUser(u.id, { enabled: !u.enabled })}
                    >
                      {u.enabled ? "Disable" : "Enable"}
                    </button>
                    <button className="btn-edit" type="button" onClick={() => setResetId(u.id)}>
                      Reset password
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
