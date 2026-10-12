import api from "./axiosClient";

// every backend response looks like { success, message, data } - we return only data
const d = (promise) => promise.then((r) => r.data.data);
const seg = (kind) => (kind === "found" ? "found-items" : "lost-items");
const clean = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, v]) => v !== "" && v != null));

export const authApi = {
  register: (body) => d(api.post("/auth/register", body)),
  login: (body) => d(api.post("/auth/login", body)),
  me: () => d(api.get("/auth/me")),
};

export const userApi = {
  update: (id, body) => d(api.put(`/users/${id}`, body)),
};

export const itemApi = {
  create: (kind, body) => d(api.post(`/${seg(kind)}`, body)),
  list: (kind, params) => d(api.get(`/${seg(kind)}`, { params: clean(params) })),
  mine: (kind) => d(api.get(`/${seg(kind)}/mine`)),
  get: (kind, id) => d(api.get(`/${seg(kind)}/${id}`)),
  update: (kind, id, body) => d(api.put(`/${seg(kind)}/${id}`, body)),
  remove: (kind, id) => d(api.delete(`/${seg(kind)}/${id}`)),
};

export const matchApi = {
  list: () => d(api.get("/matches")),
  get: (id) => d(api.get(`/matches/${id}`)),
  run: (kind, id) => d(api.post(`/matches/run/${id}`, null, { params: { type: kind.toUpperCase() } })),
};

export const claimApi = {
  create: (body) => d(api.post("/claims", body)),
  list: (status) => d(api.get("/claims", { params: clean({ status }) })),
  approve: (id, note) => d(api.put(`/claims/${id}/approve`, { note })),
  reject: (id, note) => d(api.put(`/claims/${id}/reject`, { note })),
};

export const notificationApi = {
  list: () => d(api.get("/notifications")),
  unreadCount: () => d(api.get("/notifications/unread-count")),
  read: (id) => d(api.put(`/notifications/${id}/read`)),
  readAll: () => d(api.put("/notifications/read-all")),
};

export const adminApi = {
  stats: () => d(api.get("/admin/stats")),
  users: (q) => d(api.get("/admin/users", { params: clean({ q }) })),
  activity: (id) => d(api.get(`/admin/users/${id}/activity`)),
  setEnabled: (id, enabled) => d(api.put(`/admin/users/${id}/status`, { enabled })),
  deleteUser: (id) => d(api.delete(`/admin/users/${id}`)),
  setItemStatus: (kind, id, status) => d(api.put(`/admin/${seg(kind)}/${id}/status`, { status })),
  claims: (status) => d(api.get("/admin/claims", { params: clean({ status }) })),
  matches: () => d(api.get("/admin/matches")),
  closeCase: (matchId) => d(api.put(`/admin/cases/${matchId}/close`)),
};
