import { useEffect, useMemo, useState } from "react";
import type { Route } from "./+types/provider-parking";
import {
  addParkingCatalogEntryForProvider,
  createParkingSpaceForProvider,
  deleteParkingSpaceForProvider,
  listParkingCatalogForProvider,
  listParkingSpacesForProvider,
  updateParkingSpaceForProvider,
  type ParkingCatalogEntry,
  type ParkingFacility,
  type ParkingSpaceUpsertRequest,
} from "../utils/api";

const DEFAULT_FORM: ParkingSpaceUpsertRequest = {
  name: "",
  address: "",
  city: "Montreal",
  latitude: 45.5019,
  longitude: -73.5674,
  pricePerHour: 3.5,
  rating: 4,
  totalSpots: 100,
  covered: false,
  openTwentyFourHours: false,
  evCharging: false,
  security: false,
};

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Parking Management | SUMMS" },
    { name: "description", content: "Create, update, and delete parking spaces." },
  ];
}

export default function ProviderParkingPage() {
  const [catalogEntries, setCatalogEntries] = useState<ParkingCatalogEntry[]>([]);
  const [spaces, setSpaces] = useState<ParkingFacility[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [form, setForm] = useState<ParkingSpaceUpsertRequest>(DEFAULT_FORM);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);

  const modeLabel = selectedId === null ? "Create Parking Space" : "Edit Parking Space";

  useEffect(() => {
    void loadSpaces();
  }, []);

  const sortedSpaces = useMemo(
    () => [...spaces].sort((a, b) => a.name.localeCompare(b.name)),
    [spaces],
  );

  const sortedCatalog = useMemo(
    () => [...catalogEntries].sort((a, b) => a.name.localeCompare(b.name)),
    [catalogEntries],
  );

  const facilityById = useMemo(() => {
    const map = new Map<number, ParkingFacility>();
    sortedSpaces.forEach((space) => {
      map.set(space.facilityId, space);
    });
    return map;
  }, [sortedSpaces]);

  async function loadSpaces() {
    setLoading(true);
    setError(null);
    try {
      const [spaceData, catalogData] = await Promise.all([
        listParkingSpacesForProvider(),
        listParkingCatalogForProvider(),
      ]);

      setSpaces(spaceData);
      setCatalogEntries(catalogData);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to load parking spaces.");
      setSpaces([]);
      setCatalogEntries([]);
    } finally {
      setLoading(false);
    }
  }

  function resetForm() {
    setForm(DEFAULT_FORM);
    setSelectedId(null);
  }

  function startEdit(space: ParkingFacility) {
    setSelectedId(space.facilityId);
    setForm({
      name: space.name,
      address: space.address,
      city: space.city,
      latitude: Number(space.latitude ?? 0),
      longitude: Number(space.longitude ?? 0),
      pricePerHour: Number(space.pricePerHour),
      rating: Number(space.rating),
      totalSpots: Number(space.totalSpots),
      covered: Boolean(space.covered),
      openTwentyFourHours: Boolean(space.openTwentyFourHours),
      evCharging: Boolean(space.evCharging),
      security: Boolean(space.security),
    });
    setNotice(null);
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setNotice(null);

    try {
      if (selectedId === null) {
        await createParkingSpaceForProvider(form);
        setNotice("Parking space created successfully.");
      } else {
        await updateParkingSpaceForProvider(selectedId, form);
        setNotice("Parking space updated successfully.");
      }

      resetForm();
      await loadSpaces();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to save parking space.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(id: number) {
    const confirmed = window.confirm("Delete this parking space?");
    if (!confirmed) {
      return;
    }

    setError(null);
    setNotice(null);
    try {
      await deleteParkingSpaceForProvider(id);
      if (selectedId === id) {
        resetForm();
      }
      setNotice("Parking space deleted successfully.");
      await loadSpaces();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to delete parking space.");
    }
  }

  async function handleAddFromCatalog(terrainCode: string) {
    setError(null);
    setNotice(null);

    try {
      await addParkingCatalogEntryForProvider(terrainCode);
      setNotice(`Parking space ${terrainCode} added successfully.`);
      await loadSpaces();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Unable to add parking space from catalog.");
    }
  }

  return (
    <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
      <header className="mb-4 border-b border-[#253047] pb-3">
        <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Parking Management</h1>
        <p className="text-sm text-gray-300">Add, edit, and remove city parking spaces from the preloaded dataset.</p>
      </header>

      {error && <div className="mb-3 rounded-lg border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">{error}</div>}
      {notice && <div className="mb-3 rounded-lg border border-green-500/70 bg-green-500/20 px-4 py-2 text-sm text-green-200">{notice}</div>}

      <section className="grid gap-4 lg:grid-cols-[1.15fr_1.85fr]">
        <article className="rounded-xl border border-[#2a354a] bg-[#06142b] p-4">
          <h2 className="mb-3 text-lg font-semibold text-white">{modeLabel}</h2>
          <form className="space-y-3" onSubmit={handleSubmit}>
            <TextField label="Name" value={form.name} onChange={(value) => setForm((prev) => ({ ...prev, name: value }))} />
            <TextField label="Address" value={form.address} onChange={(value) => setForm((prev) => ({ ...prev, address: value }))} />
            <TextField label="City" value={form.city} onChange={(value) => setForm((prev) => ({ ...prev, city: value }))} />

            <div className="grid gap-3 sm:grid-cols-2">
              <NumberField label="Latitude" value={form.latitude} onChange={(value) => setForm((prev) => ({ ...prev, latitude: value }))} step={0.0001} />
              <NumberField label="Longitude" value={form.longitude} onChange={(value) => setForm((prev) => ({ ...prev, longitude: value }))} step={0.0001} />
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              <NumberField label="Price/Hour" value={form.pricePerHour} onChange={(value) => setForm((prev) => ({ ...prev, pricePerHour: value }))} step={0.01} />
              <NumberField label="Rating" value={form.rating} onChange={(value) => setForm((prev) => ({ ...prev, rating: value }))} step={0.1} />
              <NumberField label="Total Spots" value={form.totalSpots} onChange={(value) => setForm((prev) => ({ ...prev, totalSpots: value }))} step={1} />
            </div>

            <div className="grid gap-2 sm:grid-cols-2">
              <CheckboxField label="Covered" checked={form.covered} onChange={(checked) => setForm((prev) => ({ ...prev, covered: checked }))} />
              <CheckboxField label="Open 24 Hours" checked={form.openTwentyFourHours} onChange={(checked) => setForm((prev) => ({ ...prev, openTwentyFourHours: checked }))} />
              <CheckboxField label="EV Charging" checked={form.evCharging} onChange={(checked) => setForm((prev) => ({ ...prev, evCharging: checked }))} />
              <CheckboxField label="Security" checked={form.security} onChange={(checked) => setForm((prev) => ({ ...prev, security: checked }))} />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="submit"
                disabled={submitting}
                className="rounded-lg bg-cyan-600 px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-cyan-500 disabled:opacity-50"
              >
                {submitting ? "Saving..." : selectedId === null ? "Create" : "Update"}
              </button>
              <button
                type="button"
                onClick={resetForm}
                className="rounded-lg border border-gray-600 px-4 py-2 text-sm font-semibold text-gray-200 hover:bg-gray-800"
              >
                Clear
              </button>
            </div>
          </form>
        </article>

        <article className="rounded-xl border border-[#2a354a] bg-[#06142b] p-4">
          <h2 className="mb-3 text-lg font-semibold text-white">Current Parking Spaces</h2>

          {loading ? (
            <p className="text-sm text-gray-400">Loading parking spaces...</p>
          ) : sortedCatalog.length === 0 ? (
            <p className="text-sm text-gray-400">No parking catalog entries found.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="px-2 py-2 text-left text-gray-300">Name</th>
                    <th className="px-2 py-2 text-left text-gray-300">City</th>
                    <th className="px-2 py-2 text-left text-gray-300">Price</th>
                    <th className="px-2 py-2 text-left text-gray-300">Status</th>
                    <th className="px-2 py-2 text-left text-gray-300">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {sortedCatalog.map((entry) => {
                    const addedSpace = entry.addedFacilityId
                      ? facilityById.get(entry.addedFacilityId)
                      : undefined;

                    return (
                    <tr key={entry.terrainCode} className="border-b border-gray-800">
                      <td className="px-2 py-2">
                        <p className="font-medium text-white">{entry.name}</p>
                        <p className="text-xs text-gray-400">{entry.address}</p>
                        <p className="text-xs text-gray-500">Terrain: {entry.terrainCode}</p>
                      </td>
                      <td className="px-2 py-2 text-gray-300">{entry.city}</td>
                      <td className="px-2 py-2 text-gray-300">${entry.pricePerHour.toFixed(2)}/h</td>
                      <td className="px-2 py-2">
                        {entry.added ? (
                          <span className="rounded border border-green-500/60 bg-green-500/20 px-2 py-1 text-xs font-semibold text-green-300">
                            Added
                          </span>
                        ) : (
                          <span className="text-xs text-gray-500">
                            -
                          </span>
                        )}
                      </td>
                      <td className="px-2 py-2">
                        {!entry.added ? (
                          <button
                            type="button"
                            onClick={() => handleAddFromCatalog(entry.terrainCode)}
                            className="rounded border border-cyan-500 px-2 py-1 text-xs text-cyan-300 hover:bg-cyan-500/20"
                          >
                            Add
                          </button>
                        ) : (
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => {
                                if (addedSpace) {
                                  startEdit(addedSpace);
                                }
                              }}
                              className="rounded border border-cyan-500 px-2 py-1 text-xs text-cyan-300 hover:bg-cyan-500/20"
                              disabled={!addedSpace}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                if (addedSpace) {
                                  void handleDelete(addedSpace.facilityId);
                                }
                              }}
                              className="rounded border border-red-500 px-2 py-1 text-xs text-red-300 hover:bg-red-500/20"
                              disabled={!addedSpace}
                            >
                              Delete
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  )})}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </section>
    </main>
  );
}

function TextField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block text-gray-300">{label}</span>
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-lg border border-gray-600 bg-gray-900 px-3 py-2 text-white"
      />
    </label>
  );
}

function NumberField({
  label,
  value,
  onChange,
  step,
}: {
  label: string;
  value: number;
  onChange: (value: number) => void;
  step: number;
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block text-gray-300">{label}</span>
      <input
        type="number"
        value={value}
        step={step}
        onChange={(event) => onChange(Number(event.target.value))}
        className="w-full rounded-lg border border-gray-600 bg-gray-900 px-3 py-2 text-white"
      />
    </label>
  );
}

function CheckboxField({
  label,
  checked,
  onChange,
}: {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
}) {
  return (
    <label className="flex items-center gap-2 rounded-lg border border-gray-700 px-3 py-2 text-sm text-gray-200">
      <input
        type="checkbox"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
      {label}
    </label>
  );
}
