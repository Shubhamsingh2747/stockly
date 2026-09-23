export const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const TOKEN_KEY = "stockly.token";
const USER_KEY = "stockly.user";

export type Role = "ADMIN" | "USER";

export type User = {
  id: number;
  email: string;
  role: Role;
};

export type ApiError = {
  status: number;
  message: string;
};

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUser(): User | null {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? (JSON.parse(raw) as User) : null;
}

export function setSession(token: string, user: User): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");
  const token = getToken();
  const isAuthCall = path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register");
  if (token && !isAuthCall) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers });
  } catch {
    throw {
      status: 0,
      message: "Cannot reach the API. Start it with mvn spring-boot:run in api/ (http://localhost:8080).",
    } satisfies ApiError;
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const err: ApiError = {
      status: body.status ?? response.status,
      message: body.message ?? "Request failed",
    };
    throw err;
  }
  return body as T;
}
