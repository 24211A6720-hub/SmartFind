import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { itemApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader, StatusBadge, fmtDate } from "../../components/UI";

export default function MyReports() {
  const location = useLocation();
  const [kind, setKind] = useState("lost");
  const [items, setItems] = useState(null);
  const [error, setError] = useState("");
  const flash = location.state?.flash;

  useEffect(() => { setItems(null); itemApi.mine(kind).then(setItems).catch((e) => { setError(errMsg(e)); setItems([]); }); }, [kind]);

  return (
    <>
      <div className="page-head row-between">
        <h1>My reports</h1>
        <div className="row"><Link className="btn" to="/report/lost">Report lost</Link><Link className="btn btn-outline" to="/report/found">Report found</Link></div>
      </div>
      <Alert type="success">{flash}</Alert>
      <Alert type="error">{error}</Alert>
      <div className="tabs">
        <button className={`tab ${kind === "lost" ? "active" : ""}`} onClick={() => setKind("lost")}>Lost</button>
        <button className={`tab ${kind === "found" ? "active" : ""}`} onClick={() => setKind("found")}>Found</button>
      </div>
      {items === null ? <Loader /> : items.length === 0 ? <EmptyState title={`No ${kind} reports yet`}>{`Your ${kind} reports will be listed here.`}</EmptyState> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Item</th><th>Category</th><th>Location</th><th>Date</th><th>Status</th><th /></tr></thead>
          <tbody>{items.map((i) => (
            <tr key={i.id}>
              <td><strong>{i.itemName}</strong><div className="small muted">{i.color}</div></td>
              <td>{i.category}</td><td>{i.location}</td><td>{fmtDate(i.date)}</td>
              <td><StatusBadge value={i.status} /></td>
              <td><Link className="btn btn-sm btn-outline" to={`/items/${kind}/${i.id}`}>Open</Link></td>
            </tr>))}
          </tbody>
        </table></div>
      )}
    </>
  );
}
