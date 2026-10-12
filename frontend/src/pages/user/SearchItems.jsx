import { useEffect, useState } from "react";
import { itemApi } from "../../api";
import { errMsg } from "../../api/axiosClient";
import { Alert, EmptyState, Loader } from "../../components/UI";
import ItemCard from "../../components/ItemCard";

const EMPTY = { name: "", category: "", color: "", location: "", date: "", status: "" };

export default function SearchItems() {
  const [kind, setKind] = useState("lost");
  const [filters, setFilters] = useState(EMPTY);
  const [items, setItems] = useState(null);
  const [error, setError] = useState("");

  const search = (f = filters, k = kind) => {
    setItems(null); setError("");
    itemApi.list(k, f).then(setItems).catch((e) => { setError(errMsg(e)); setItems([]); });
  };

  useEffect(() => { search(EMPTY, kind); setFilters(EMPTY); /* eslint-disable-next-line */ }, [kind]);

  const change = (e) => setFilters({ ...filters, [e.target.name]: e.target.value });
  const statuses = kind === "lost" ? ["LOST", "MATCHED", "CLAIMED", "CLOSED"] : ["FOUND", "MATCHED", "CLAIMED", "CLOSED"];

  return (
    <>
      <div className="page-head"><h1>Search items</h1><p className="muted">Browse every lost and found report.</p></div>
      <div className="tabs">
        <button className={`tab ${kind === "lost" ? "active" : ""}`} onClick={() => setKind("lost")}>Lost items</button>
        <button className={`tab ${kind === "found" ? "active" : ""}`} onClick={() => setKind("found")}>Found items</button>
      </div>

      <form className="card mb" onSubmit={(e) => { e.preventDefault(); search(); }}>
        <div className="form-grid" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(170px, 1fr))" }}>
          <div className="field"><label>Item name</label><input name="name" value={filters.name} onChange={change} /></div>
          <div className="field"><label>Category</label><input name="category" value={filters.category} onChange={change} /></div>
          <div className="field"><label>Color</label><input name="color" value={filters.color} onChange={change} /></div>
          <div className="field"><label>Location</label><input name="location" value={filters.location} onChange={change} /></div>
          <div className="field"><label>Date</label><input type="date" name="date" value={filters.date} onChange={change} /></div>
          <div className="field"><label>Status</label>
            <select name="status" value={filters.status} onChange={change}><option value="">Any</option>{statuses.map((s) => <option key={s}>{s}</option>)}</select>
          </div>
        </div>
        <div className="row">
          <button className="btn">Search</button>
          <button type="button" className="btn btn-ghost" onClick={() => { setFilters(EMPTY); search(EMPTY); }}>Clear</button>
        </div>
      </form>

      <Alert type="error">{error}</Alert>
      {items === null ? <Loader /> : items.length === 0 ? <EmptyState title="No items found">Try removing some filters.</EmptyState> : (
        <div className="grid grid-3">{items.map((i) => <ItemCard key={i.id} item={i} />)}</div>
      )}
    </>
  );
}
