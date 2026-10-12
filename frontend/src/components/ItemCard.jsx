import { Link } from "react-router-dom";
import { StatusBadge, fmtDate } from "./UI";

export default function ItemCard({ item }) {
  const kind = item.type.toLowerCase();
  return (
    <div className="card item-card">
      {item.imageUrl && <img src={item.imageUrl} alt={item.itemName} onError={(e) => (e.target.style.display = "none")} />}
      <div className="row-between">
        <h3>{item.itemName}</h3>
        <StatusBadge value={item.status} />
      </div>
      <div className="meta">
        <span>{item.category}</span><span>{item.color}</span>
        <span>{item.location}</span><span>{fmtDate(item.date)}</span>
      </div>
      <p className="muted small" style={{ margin: 0 }}>{item.description.length > 110 ? item.description.slice(0, 110) + "..." : item.description}</p>
      <div><Link to={`/items/${kind}/${item.id}`} className="btn btn-sm btn-outline">View details</Link></div>
    </div>
  );
}
