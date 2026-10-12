import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { itemApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, Loader, Modal, StatusBadge, fmtDate, fmtDateTime } from "../../components/UI";
import { useAuth } from "../../context/AuthContext";

export default function ItemDetails() {
  const { kind, id } = useParams();
  const { user, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [item, setItem] = useState(null);
  const [error, setError] = useState("");
  const [confirm, setConfirm] = useState(false);

  useEffect(() => { itemApi.get(kind, id).then(setItem).catch((e) => setError(errMsg(e))); }, [kind, id]);

  const remove = async () => {
    try { await itemApi.remove(kind, id); navigate("/my-reports", { state: { flash: "Report deleted." } }); }
    catch (e) { setError(errMsg(e)); setConfirm(false); }
  };

  if (!item && !error) return <Loader />;
  if (!item) return <Alert type="error">{error}</Alert>;

  const owner = item.ownerId === user.id;
  const editable = owner && !["CLAIMED", "CLOSED"].includes(item.status);

  return (
    <>
      <div className="page-head row-between">
        <div><h1>{item.itemName}</h1><StatusBadge value={item.status} /> <StatusBadge value={item.type} /></div>
        <div className="row">
          {owner && <Link to="/matches" className="btn btn-amber">View matches</Link>}
          {editable && <Link to={`/items/${kind}/${id}/edit`} className="btn btn-outline">Edit</Link>}
          {(owner || isAdmin) && <button className="btn btn-danger" onClick={() => setConfirm(true)}>Delete</button>}
        </div>
      </div>
      <Alert type="error">{error}</Alert>
      <div className="card">
        {item.imageUrl && <img className="thumb" style={{ maxHeight: 280, marginBottom: "1rem" }} src={item.imageUrl} alt={item.itemName} onError={(e) => (e.target.style.display = "none")} />}
        <div className="form-grid">
          <div><strong>Category</strong><p>{item.category}</p></div>
          <div><strong>Color</strong><p>{item.color}</p></div>
          <div><strong>{kind === "lost" ? "Location lost" : "Location found"}</strong><p>{item.location}</p></div>
          <div><strong>{kind === "lost" ? "Date lost" : "Date found"}</strong><p>{fmtDate(item.date)}</p></div>
          <div><strong>Reported by</strong><p>{item.ownerName}</p></div>
          <div><strong>Reported on</strong><p>{fmtDateTime(item.createdAt)}</p></div>
        </div>
        <strong>Description</strong><p>{item.description}</p>
        {item.identifyingDetails && <><strong>Identifying details (private)</strong><p>{item.identifyingDetails}</p></>}
        {item.contactInfo && <><strong>Contact</strong><p>{item.contactInfo}</p></>}
        {!owner && !isAdmin && <p className="small muted">Contact details and identifying details are private. They are shared only after an admin approves a claim.</p>}
      </div>
      {confirm && (
        <Modal title="Delete this report?" onClose={() => setConfirm(false)}>
          <p>This also removes its matches and claims. This cannot be undone.</p>
          <div className="row"><button className="btn btn-danger" onClick={remove}>Yes, delete</button><button className="btn btn-ghost" onClick={() => setConfirm(false)}>Cancel</button></div>
        </Modal>
      )}
    </>
  );
}
