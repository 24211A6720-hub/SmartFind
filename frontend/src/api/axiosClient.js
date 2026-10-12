import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

// Attach the JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("sf_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If the token is invalid/expired, log the user out
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const onAuthPage = ["/login", "/register"].includes(window.location.pathname);
    if (err.response?.status === 401 && !onAuthPage) {
      localStorage.removeItem("sf_token");
      localStorage.removeItem("sf_user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

/** Turns an axios error into a message that is safe to show to the user. */
export const errMsg = (e) =>
  e?.response?.data?.message || (e?.request ? "Cannot reach the server. Is the backend running?" : "Something went wrong.");

export default api;
