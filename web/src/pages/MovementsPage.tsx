import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api, ApiError } from "../api";

type Movement = {
  id: number;
  productId: number;
  movementType: string;
  quantityDelta: number;
  quantityAfter: number;
  reference: string;
  createdAt: string;
};

export default function MovementsPage() {
  const { id } = useParams();
  const [rows, setRows] = useState<Movement[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        setRows(await api<Movement[]>(`/api/v1/products/${id}/movements`));
      } catch (err) {
        setError((err as ApiError).message);
      }
    }
    void load();
  }, [id]);

  return (
    <>
      <p>
        <Link to="/">Back to products</Link>
      </p>
      <h1>Stock movements</h1>
      {error ? <p className="error">{error}</p> : null}
      <div className="card">
        <table>
          <thead>
            <tr>
              <th>When</th>
              <th>Type</th>
              <th>Delta</th>
              <th>After</th>
              <th>Ref</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((m) => (
              <tr key={m.id}>
                <td>{new Date(m.createdAt).toLocaleString()}</td>
                <td>{m.movementType}</td>
                <td>{m.quantityDelta}</td>
                <td>{m.quantityAfter}</td>
                <td>{m.reference}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
