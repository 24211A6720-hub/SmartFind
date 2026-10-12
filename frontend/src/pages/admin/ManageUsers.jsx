import { useEffect, useState } from "react";
import { adminApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader, Modal, StatusBadge, fmtDate } from "../../components/UI";
import { useAuth } from "../../context/AuthContext";

export default function ManageUsers() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState(null);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [activity, setActivity] = useState(null);
  const [toDelete, setToDelete] = useState(null);

  const load = (query = q) => adminApi.users(query).then(setUsers).catch((e) => { setError(errMsg(e)); setUsers([]); });
  useEffect(() => { load(""); /* eslint-disable-next-line */ }, []);

  const toggle = async (u) => {
    setError(""); setSuccess("");
    try { await adminApi.setEnabled(u.id, !u.enabled); setSuccess(`${u.name} ${u.enabled ? "disabled" : "enabled"}.`); load(); }
    catch (e) { setError(errMsg(e)); }
  };
  const remove = async () => {
    setError(""); setSuccess("");
    try { await adminApi.deleteUser(toDelete.id); setSuccess("User deleted."); load(); }
    catch (e) { setError(errMsg(e)); }
    setToDelete(null);
  };
  const showActivity = (u) => adminApi.activity(u.id).then(setActivity).catch((e) => setError(errMsg(e)));

  return (
    <>
      <div className="page-head"><h1>Manage users</h1></div>
      <Alert type="error" onClose={() => setError("")}>{error}</Alert>
      <Alert type="success" onClose={() => setSuccess("")}>{success}</Alert>
      <form className="row mb" onSubmit={(e) => { e.preventDefault(); load(); }}>
        <input style={{ maxWidth: 320 }} placeholder="Search by name or email" value={q} onChange={(e) => setQ(e.target.value)} />
        <button className="btn">Search</button>
        <button type="button" className="btn btn-ghost" onClick={() => { setQ(""); load(""); }}>Clear</button>
      </form>
      {!users ? <Loader /> : users.length === 0 ? <EmptyState title="No users found" /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Status</th><th>Joined</th><th /></tr></thead>
          <tbody>{users.map((u) => (
            <tr key={u.id}>
              <td><strong>{u.name}</strong></td><td>{u.email}</td><td>{u.phone}</td>
              <td><StatusBadge value={u.role} /></td>
              <td><span className={`badge ${u.enabled ? "APPROVED" : "REJECTED"}`}>{u.enabled ? "Active" : "Disabled"}</span></td>
              <td>{fmtDate(u.createdAt)}</td>
              <td><div className="row">
                <button className="btn btn-sm btn-outline" onClick={() => showActivity(u)}>Activity</button>
                {u.id !== me.id && <button className="btn btn-sm btn-ghost" onClick={() => toggle(u)}>{u.enabled ? "Disable" : "Enable"}</button>}
                {u.id !== me.id && <button className="btn btn-sm btn-danger" onClick={() => setToDelete(u)}>Delete</button>}
              </div></td>
            </tr>))}
          </tbody>
        </table></div>
      )}

      {activity && (
        <Modal wide title={`Activity: ${activity.user.name}`} onClose={() => setActivity(null)}>
          <p className="muted small">{activity.user.email}</p>
          <h3>Lost reports ({activity.lostItems.length})</h3>
          {activity.lostItems.length === 0 ? <p className="muted small">None</p> : <ul>{activity.lostItems.map((i) => <li key={i.id}>{i.itemName} <StatusBadge value={i.status} /></li>)}</ul>}
          <h3>Found reports ({activity.foundItems.length})</h3>
          {activity.foundItems.length === 0 ? <p className="muted small">None</p> : <ul>{activity.foundItems.map((i) => <li key={i.id}>{i.itemName} <StatusBadge value={i.status} /></li>)}</ul>}
          <h3>Claims ({activity.claims.length})</h3>
          {activity.claims.length === 0 ? <p className="muted small">None</p> : <ul>{activity.claims.map((c) => <li key={c.id}>{c.lostItemName} <StatusBadge value={c.status} /></li>)}</ul>}
        </Modal>
      )}
      {toDelete && (
        <Modal title={`Delete ${toDelete.name}?`} onClose={() => setToDelete(null)}>
          <p>Users who already have reports or claims cannot be deleted. Disable them instead.</p>
          <div className="row"><button className="btn btn-danger" onClick={remove}>Delete user</button><button className="btn btn-ghost" onClick={() => setToDelete(null)}>Cancel</button></div>
        </Modal>
      )}
    </>
  );
}
