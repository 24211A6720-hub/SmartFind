import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { itemApi, matchApi, claimApi, notificationApi } from "../../api";
import { Loader, StatCard, fmtDateTime } from "../../components/UI";

export default function Dashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);

  useEffect(() => {
    Promise.all([itemApi.mine("lost"), itemApi.mine("found"), matchApi.list(), claimApi.list(), notificationApi.list()])
      .then(([lost, found, matches, claims, notifs]) => setData({ lost, found, matches, claims, notifs }))
      .catch(() => setData({ lost: [], found: [], matches: [], claims: [], notifs: [] }));
  }, []);

  if (!data) return <Loader />;
  const unread = data.notifs.filter((n) => !n.read);

  return (
    <>
      <div className="page-head">
        <h1>Welcome, {user.name}</h1>
        <p className="muted">Here is what is happening with your reports.</p>
      </div>

      <div className="grid grid-stats mb">
        <StatCard label="Lost reports" value={data.lost.length} tone="red" />
        <StatCard label="Found reports" value={data.found.length} tone="green" />
        <StatCard label="Possible matches" value={data.matches.length} tone="amber" />
        <StatCard label="Pending claims" value={data.claims.filter((c) => c.status === "PENDING").length} />
        <StatCard label="Unread notifications" value={unread.length} tone="amber" />
      </div>

      <div className="card mb">
        <h2>Quick actions</h2>
        <div className="row">
          <Link className="btn" to="/report/lost">Report lost item</Link>
          <Link className="btn btn-outline" to="/report/found">Report found item</Link>
          <Link className="btn btn-outline" to="/search">Search items</Link>
          <Link className="btn btn-outline" to="/matches">View matches</Link>
          <Link className="btn btn-ghost" to="/my-reports">My reports</Link>
          <Link className="btn btn-ghost" to="/my-claims">My claims</Link>
          <Link className="btn btn-ghost" to="/profile">Profile</Link>
        </div>
      </div>

      <div className="card">
        <div className="row-between"><h2>Latest notifications</h2><Link to="/notifications" className="small">See all</Link></div>
        {data.notifs.length === 0 ? <p className="muted">Nothing yet. Matches will appear here.</p> :
          data.notifs.slice(0, 4).map((n) => (
            <div key={n.id} className={`notif ${n.read ? "read" : "unread"}`} style={{ cursor: "default" }}>
              <span className="dot" /><div>{n.message}<div className="small muted">{fmtDateTime(n.createdAt)}</div></div>
            </div>
          ))}
      </div>
    </>
  );
}
