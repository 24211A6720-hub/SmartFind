import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { adminApi, itemApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader, Modal, StatusBadge, fmtDate } from "../../components/UI";

/** Shared admin table for lost or found reports. */
export default function ManageItems({ kind }) {
  const [items, setItems] = useState(null);
  const [filters, setFilters] = useState({ name: "", status: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [toDelete, setToDelete] = useState(null);

  const statuses = [kind === "lost" ? "LOST" : "FOUND", "MATCHED", "CLAIMED", "CLOSED"];
  const load = (f = filters) => { setItems(null); itemApi.list(kind, f).then(setItems).catch((e) => { setError(errMsg(e)); setItems([]); }); };
  useEffect(() => { load({ name: "", status: "" }); setFilters({ name: "", status: "" }); /* eslint-disable-next-line */ }, [kind]);

  const changeStatus = async (item, status) => {
    setError(""); setSuccess("");
    try { await adminApi.setItemStatus(kind, item.id, status); setSuccess("Status updated."); load(); }
    catch (e) { setError(errMsg(e)); }
  };
  const remove = async () => {
    setError(""); setSuccess("");
    try { await itemApi.remove(kind, toDelete.id); setSuccess("Report removed."); load(); }
    catch (e) { setError(errMsg(e)); }
    setToDelete(null);
  };

  return (
    <>
      <div className="page-head"><h1>Manage {kind} reports</h1></div>
      <Alert type="error" onClose={() => setError("")}>{error}</Alert>
      <Alert type="success" onClose={() => setSuccess("")}>{success}</Alert>
      <form className="row mb" onSubmit={(e) => { e.preventDefault(); load(); }}>
        <input style={{ maxWidth: 260 }} placeholder="Search by item name" value={filters.name} onChange={(e) => setFilters({ ...filters, name: e.target.value })} />
        <select style={{ maxWidth: 160 }} value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
          <option value="">Any status</option>{statuses.map((s) => <option key={s}>{s}</option>)}
        </select>
        <button className="btn">Filter</button>
      </form>
      {!items ? <Loader /> : items.length === 0 ? <EmptyState title="No reports found" /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Item</th><th>Reported by</th><th>Category</th><th>Location</th><th>Date</th><th>Status</th><th /></tr></thead>
          <tbody>{items.map((i) => (
            <tr key={i.id}>
              <td><strong>{i.itemName}</strong><div className="small muted">{i.color}</div></td>
              <td>{i.ownerName}</td><td>{i.category}</td><td>{i.location}</td><td>{fmtDate(i.date)}</td>
              <td><select value={i.status} onChange={(e) => changeStatus(i, e.target.value)} aria-label="Change status" style={{ minWidth: 120 }}>
                {statuses.map((s) => <option key={s}>{s}</option>)}</select></td>
              <td><div className="row">
                <Link className="btn btn-sm btn-outline" to={`/items/${kind}/${i.id}`}>View</Link>
                <button className="btn btn-sm btn-danger" onClick={() => setToDelete(i)}>Remove</button>
              </div></td>
            </tr>))}
          </tbody>
        </table></div>
      )}
      {toDelete && (
        <Modal title="Remove this report?" onClose={() => setToDelete(null)}>
          <p><strong>{toDelete.itemName}</strong> and its matches and claims will be deleted.</p>
          <div className="row"><button className="btn btn-danger" onClick={remove}>Remove</button><button className="btn btn-ghost" onClick={() => setToDelete(null)}>Cancel</button></div>
        </Modal>
      )}
    </>
  );
}
